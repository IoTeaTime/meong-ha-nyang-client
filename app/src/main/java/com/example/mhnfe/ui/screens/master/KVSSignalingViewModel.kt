package com.example.mhnfe.ui.screens.master

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.amazonaws.services.kinesisvideo.model.CreateSignalingChannelRequest
import com.amazonaws.services.kinesisvideo.model.DescribeSignalingChannelRequest
import com.amazonaws.services.kinesisvideo.model.GetSignalingChannelEndpointRequest
import com.amazonaws.services.kinesisvideo.model.ResourceEndpointListItem
import com.amazonaws.services.kinesisvideo.model.ResourceNotFoundException
import com.amazonaws.services.kinesisvideo.model.SingleMasterChannelEndpointConfiguration
import com.amazonaws.services.kinesisvideosignaling.AWSKinesisVideoSignalingClient
import com.amazonaws.services.kinesisvideosignaling.model.GetIceServerConfigRequest
import com.amazonaws.services.kinesisvideosignaling.model.IceServer
import com.example.mhnfe.data.signaling.okhttp.SignalingServiceWebSocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.Queue
import java.util.UUID
import java.util.concurrent.Executors

data class WebRtcConfigData(
    val channelArn: String,
    val streamArn: String,
    val webrtcEndpoint: String,
    val wssEndpoint: String
)
data class KvsEndpointData(
    val webrtcEndpoint: String,
    val mWssEndpoint: String
)

class KVSSignalingViewModel : ViewModel() {

    private val _endpointData = MutableLiveData<KvsEndpointData>()
    val endpointData: LiveData<KvsEndpointData> get() = _endpointData

    private val _configData = MutableStateFlow<WebRtcConfigData?>(null)
    val configData: StateFlow<WebRtcConfigData?> = _configData

    // MutableStateFlow를 private val로 선언
    private val _uiState = MutableStateFlow<WebRTCUiState>(WebRTCUiState.Initial)

    // 외부에 노출할 불변 StateFlow
    val uiState: StateFlow<WebRTCUiState> = _uiState.asStateFlow()

    //region은 서울로 고정
    private val region = Region.getRegion(Regions.AP_NORTHEAST_2)
    private val regionName = Regions.AP_NORTHEAST_2.getName()

    // 내부 상태를 위한 변수들
    private var channelArn: String? = null
    private val endpointList = mutableListOf<ResourceEndpointListItem>()
    private val iceServerList = mutableListOf<IceServer>()
    private var streamArn: String? = null

    //webRTC관련 변수
    private val clientId = UUID.randomUUID().toString()
    private val printStatsExecutor = Executors.newSingleThreadScheduledExecutor()
    private val peerConnectionFoundMap = mutableMapOf<String, PeerConnection>()
    private val pendingIceCandidatesMap = mutableMapOf<String, Queue<IceCandidate>>()

    private var localPeer: PeerConnection? = null
    private var videoSource: VideoSource? = null
    private var videoCapturer: VideoCapturer? = null
    private var client: SignalingServiceWebSocketClient? = null


    private fun getCredentialsProvider(): AWSCredentialsProvider {
        return AWSMobileClient.getInstance()
    }

    // ViewModel이 소멸될 때 호출되는 메서드
    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    // 리소스 정리를 위한 메서드
    private fun cleanup() {
        printStatsExecutor.shutdownNow()
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        videoSource?.dispose()
        localPeer?.dispose()
        client?.disconnect()

        peerConnectionFoundMap.clear()
        pendingIceCandidatesMap.clear()
    }

    fun updateSignalingChannelInfo(
        channelName: String,
        role: ChannelRole,
    ) {
        viewModelScope.launch(Dispatchers.IO) {  // Dispatchers.IO로 변경
            try {
                withContext(Dispatchers.Main) {
                    _uiState.update { WebRTCUiState.Loading }
                }

                Log.d(TAG, "Starting channel update with name: $channelName, role: $role")
                val awsKinesisVideoClient = getAwsKinesisVideoClient()
                Log.d(TAG, "AWS Kinesis Video Client initialized")

                // Describe or Create Signaling Channel
                try {
                    val describeResult = awsKinesisVideoClient.describeSignalingChannel(
                        DescribeSignalingChannelRequest().apply {
                            this.channelName = channelName
                        }
                    )
                    channelArn = describeResult.channelInfo.channelARN
                    Log.d(TAG, "Channel exists - ARN: $channelArn")
                } catch (e: ResourceNotFoundException) {
                    if (role == ChannelRole.MASTER) {
                        val createResult = awsKinesisVideoClient.createSignalingChannel(
                            CreateSignalingChannelRequest().apply {
                                this.channelName = channelName
                            }
                        )
                        channelArn = createResult.channelARN
                        Log.d(TAG, "Created new channel - ARN: $channelArn")
                    } else {
                        Log.e(TAG, "Channel doesn't exist and viewer can't create one")
                        withContext(Dispatchers.Main) {
                            _uiState.update { WebRTCUiState.Error("Signaling Channel $channelName doesn't exist!") }
                        }
                        return@launch
                    }
                }

                // Get Signaling Channel Endpoints
                Log.d(TAG, "Getting channel endpoints for ARN: $channelArn")
                val endpointResult = awsKinesisVideoClient.getSignalingChannelEndpoint(
                    GetSignalingChannelEndpointRequest().apply {
                        this.channelARN = channelArn
                        this.singleMasterChannelEndpointConfiguration =
                            SingleMasterChannelEndpointConfiguration()
                                .withProtocols("WSS", "HTTPS")
                                .withRole(role)
                    }
                )

                endpointResult.resourceEndpointList.forEach { endpoint ->
                    Log.d(TAG, "Endpoint - Protocol: ${endpoint.protocol}, Endpoint: ${endpoint.resourceEndpoint}")
                }

                val webrtcEndpoint = endpointResult.resourceEndpointList.find { it.protocol == "HTTPS" }?.resourceEndpoint ?: ""
                var mWssEndpoint = endpointResult.resourceEndpointList.find { it.protocol == "WSS" }?.resourceEndpoint ?: ""

                Log.d(TAG, "WebRTC Endpoint: $webrtcEndpoint")
                Log.d(TAG, "WSS Endpoint: $mWssEndpoint")


                // 스텝 3: ICE 서버 설정 가져오기
                try {
                    val awsKinesisVideoSignalingClient =
                        getAwsKinesisVideoSignalingClient(webrtcEndpoint)
                    val getIceServerConfigResult =
                        awsKinesisVideoSignalingClient.getIceServerConfig(
                            GetIceServerConfigRequest().apply {
                                this.channelARN = channelArn
                                this.clientId = role.name
                            }
                        )

                    // ICE 서버 리스트 저장
                    iceServerList.clear()
                    iceServerList.addAll(getIceServerConfigResult.iceServerList)

                    // 성공 상태 업데이트
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            WebRTCUiState.Success(
                                channelArn = channelArn ?: "",
                                endpointList = endpointList,
                                iceServerList = iceServerList,
                                role = role
                            )
                        }
                        Log.d(TAG, "WebRtcViewModel 설정 초기화 완료")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "ICE 서버 설정 가져오기 실패", e)
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            WebRTCUiState.Error("ICE 서버 설정 가져오기 실패: ${e.localizedMessage}")
                        }
                    }
                    return@launch
                }


            } catch (e: Exception) {
                Log.e(TAG, "Operation failed", e)
                withContext(Dispatchers.Main) {
                    _uiState.update { WebRTCUiState.Error("Operation failed: ${e.localizedMessage}") }
                }
            }
        }
    }




    private fun getAwsKinesisVideoClient(): AWSKinesisVideoClient {
        return AWSKinesisVideoClient(
            getCredentialsProvider().credentials
        ).apply {
            setRegion(region)
            setSignerRegionOverride(regionName)
            setServiceNameIntern("kinesisvideo")
        }
    }

    private fun getAwsKinesisVideoSignalingClient(endpoint: String): AWSKinesisVideoSignalingClient {
        return AWSKinesisVideoSignalingClient(
            getCredentialsProvider().credentials
        ).apply {
            setRegion(region)
            setSignerRegionOverride(regionName)
            setServiceNameIntern("kinesisvideo")
            setEndpoint(endpoint)
        }
    }
}

sealed class WebRTCUiState {
    object Initial : WebRTCUiState()
    object Loading : WebRTCUiState()
    object StorageSessionJoined : WebRTCUiState()
    object Connected : WebRTCUiState() // WebRTC 연결 성공
    data class StreamAdded(val mediaStream: MediaStream) : WebRTCUiState()
    data class MessageReceived(val message: String) : WebRTCUiState()
    data class Success(
        val channelArn: String,
        val endpointList: List<ResourceEndpointListItem>,
        val iceServerList: List<IceServer>,
        val role: ChannelRole
    ) : WebRTCUiState()
    data class Error(val message: String) : WebRTCUiState()
    companion object {
        private const val TAG = "WebRTCViewModel"
    }
}