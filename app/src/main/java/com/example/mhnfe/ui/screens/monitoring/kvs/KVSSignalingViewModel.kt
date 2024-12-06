package com.example.mhnfe.ui.screens.monitoring.kvs


import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.AudioManager
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.PixelCopy
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSSessionCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.amazonaws.services.kinesisvideo.model.CreateSignalingChannelRequest
import com.amazonaws.services.kinesisvideo.model.DescribeSignalingChannelRequest
import com.amazonaws.services.kinesisvideo.model.GetSignalingChannelEndpointRequest
import com.amazonaws.services.kinesisvideo.model.ListSignalingChannelsRequest
import com.amazonaws.services.kinesisvideo.model.ResourceEndpointListItem
import com.amazonaws.services.kinesisvideo.model.ResourceNotFoundException
import com.amazonaws.services.kinesisvideo.model.SingleMasterChannelEndpointConfiguration
import com.amazonaws.services.kinesisvideosignaling.AWSKinesisVideoSignalingClient
import com.amazonaws.services.kinesisvideosignaling.model.GetIceServerConfigRequest
import com.amazonaws.services.kinesisvideosignaling.model.IceServer
import com.amazonaws.services.kinesisvideowebrtcstorage.AWSKinesisVideoWebRTCStorageClient
import com.amazonaws.services.kinesisvideowebrtcstorage.model.JoinStorageSessionRequest
import com.example.mhnfe.data.signaling.SignalingListener
import com.example.mhnfe.data.signaling.model.Event
import com.example.mhnfe.data.signaling.model.Message
import com.example.mhnfe.data.signaling.okhttp.SignalingServiceWebSocketClient
import com.example.mhnfe.domain.webrtc.KinesisVideoPeerConnection
import com.example.mhnfe.domain.webrtc.KinesisVideoSdpObserver
import com.example.mhnfe.utils.AwsV4Signer
import com.example.mhnfe.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.webrtc.ApplicationContextProvider.getApplicationContext
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoFrame
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.URI
import java.util.Date
import java.util.LinkedList
import java.util.Optional
import java.util.Queue
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume


enum class ConnectionEvent {
    ConnectionFailed,
    ConnectionSuccess
    // 필요한 다른 상태들 추가
}

data class WebRtcConfigData(
    val channelArn: String,
    val webrtcEndpoint: String,
    val wssEndpoint: String
)
data class KvsEndpointData(
    val webrtcEndpoint: String,
    val mWssEndpoint: String
)

data class WebRtcConfig(
    val channelName: String,
    val channelArn: String,
    val webrtcEndpoint: String,
    val mWssEndpoint: String,
    val isMaster: Boolean,
    val isFrontCamera: Boolean,
    val isAudioEnabled: Boolean = true
)


sealed class KvsSignalingState {
    object Initial : KvsSignalingState()
    object Loading : KvsSignalingState()
    data class Success(
        val channelArn: String,
        val streamArn: String,
        val webrtcEndpoint: String,
        val wssEndpoint: String,
        val iceServers: List<IceServer>,
        val isMaster: Boolean
    ) : KvsSignalingState()
    data class Error(val message: String) : KvsSignalingState()
}


class KVSSignalingViewModel : ViewModel() {
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val _localView = MutableStateFlow<SurfaceViewRenderer?>(null)
    val localView: StateFlow<SurfaceViewRenderer?> = _localView

    private val _remoteView = MutableStateFlow<SurfaceViewRenderer?>(null)
    val remoteView: StateFlow<SurfaceViewRenderer?> = _remoteView

    lateinit var videoCapturer: VideoCapturer
    lateinit var localVideoTrack: VideoTrack
    lateinit var videoSource: VideoSource

    private val _isViewsInitialized = MutableStateFlow(false)
    val isViewsInitialized: StateFlow<Boolean> = _isViewsInitialized



    private val _signalingState = MutableStateFlow<KvsSignalingState>(KvsSignalingState.Initial)
    val signalingState: StateFlow<KvsSignalingState> = _signalingState.asStateFlow()

    private val _webRtcConfig = MutableStateFlow<WebRtcConfig?>(null)
    val webRtcConfig: StateFlow<WebRtcConfig?> = _webRtcConfig.asStateFlow()

    private val _endpointData = MutableLiveData<KvsEndpointData>()
    val endpointData: LiveData<KvsEndpointData> get() = _endpointData


    private val _configData = MutableStateFlow<WebRtcConfigData?>(null)
    val configData: StateFlow<WebRtcConfigData?> = _configData

    private val _uiState = MutableStateFlow<WebRTCUiState>(WebRTCUiState.Loading)
    val uiState: StateFlow<WebRTCUiState> = _uiState.asStateFlow()

    //region은 서울로 고정
    private val region = Region.getRegion(Regions.AP_NORTHEAST_2)
    private val regionName = "ap-northeast-2"

    // 내부 상태를 위한 변수들
    private var channelArn: String? = null
    private val endpointList = mutableListOf<ResourceEndpointListItem>()
    private val iceServerList = mutableListOf<IceServer>()
    private var streamArn: String? = null

    //webRTC관련 변수
    private val clientId = UUID.randomUUID().toString()
    private var printStatsExecutor = Executors.newSingleThreadScheduledExecutor()
    private val peerConnectionFoundMap = mutableMapOf<String, PeerConnection>()
    private val pendingIceCandidatesMap = mutableMapOf<String, Queue<IceCandidate>>()

    private var localPeer: PeerConnection? = null
    private var client: SignalingServiceWebSocketClient? = null

    // WebRTC 연결 관련 변수들
    private var master: Boolean = false  // master/viewer 구분
    private var mCreds: AWSCredentials? = null  // AWS 자격증명
    private var mChannelArn: String? = null  // 채널 ARN
    private var mWssEndpoint: String? = null  // WebSocket 엔드포인트
    private var webrtcEndpoint: String? = null  // WebRTC 엔드포인트
    private var dataEndpoint: String? = null  // ICE 엔드포인트
    private var mStreamArn: String? = null  // 스트림 ARN
    private var mRegion: String = "ap-northeast-2" // 리전
    private var gotException: Boolean = false  // 예외 발생 여부
    private var recipientClientId: String? = null  // 수신자 클라이언트 ID

    // Peer Connection 관련
    private val masterLocalPeer = mutableMapOf<String, PeerConnection>()  // 마스터의 로컬 피어 맵
    private var peerConnectionFactory: PeerConnectionFactory? = null  // 피어 커넥션 팩토리

    private var mClientId = UUID.randomUUID().toString()



    // UI 업데이트를 위한 StateFlow
    private val _localVideoTrackState = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrackState = _localVideoTrackState.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack = _remoteVideoTrack.asStateFlow()

    private val _localAudioTrack = MutableStateFlow<AudioTrack?>(null)
    val localAudioTrack = _localAudioTrack.asStateFlow()

    private var audioManager: AudioManager? = null







    private fun getCredentialsProvider(): AWSCredentialsProvider {
        return AWSMobileClient.getInstance()
    }

    private val peerIceServers = mutableListOf<PeerConnection.IceServer>()


    fun updateSignalingChannelInfo(channelName: String, role: ChannelRole, context: Context, eglBase: EglBase.Context)
            = viewModelScope.launch(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) {
                _uiState.update { WebRTCUiState.Loading }
            }

            Log.d(TAG, "Starting channel update with name: $channelName, role: $role")
            val awsKinesisVideoClient = getAwsKinesisVideoClient()
            Log.d(TAG, "AWS Kinesis Video Client initialized")


            val request = ListSignalingChannelsRequest()
            val response = awsKinesisVideoClient.listSignalingChannels(request)

            // 채널 정보 출력
            response.channelInfoList.forEach { channel ->
                Log.d("AWS", "Channel Name: ${channel.channelName}")
            }

            // Describe or Create Signaling Channel
            try {
                val describeResult = awsKinesisVideoClient.describeSignalingChannel(
                    DescribeSignalingChannelRequest().apply {
                        this.channelName = channelName
                    }
                )
                channelArn = describeResult.channelInfo.channelARN

                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                        .createInitializationOptions()
                )
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
                when (endpoint.protocol) {
                    "WEBRTC" -> webrtcEndpoint = endpoint.resourceEndpoint
                    "WSS" -> mWssEndpoint = endpoint.resourceEndpoint
                    "HTTPS" -> dataEndpoint = endpoint.resourceEndpoint
                }
            }

            // 채널 ARN 저장
            mChannelArn = channelArn

            val webrtcEndpoint =
                endpointResult.resourceEndpointList.find { it.protocol == "WEBRTC" }?.resourceEndpoint
                    ?: ""
            var mWssEndpoint =
                endpointResult.resourceEndpointList.find { it.protocol == "WSS" }?.resourceEndpoint
                    ?: ""
            var dataEndpoint =
                endpointResult.resourceEndpointList.find { it.protocol == "HTTPS" }?.resourceEndpoint
                    ?: ""
            Log.d(TAG, "WebRTC Endpoint: $webrtcEndpoint")
            Log.d(TAG, "WSS Endpoint: $mWssEndpoint")
            Log.d(TAG, "dataEndpoint Endpoint: $dataEndpoint")


            // 스텝 3: ICE 서버 설정 가져오기
            try {
                val awsKinesisVideoSignalingClient =
                    getAwsKinesisVideoSignalingClient(dataEndpoint)
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

                // ICE 서버 정보를 분리하여 리스트로 저장
                val userNames = ArrayList<String>()
                val passwords = ArrayList<String>()
                val urisList = ArrayList<String>()

                getIceServerConfigResult.iceServerList.forEach { iceServer ->
                    userNames.add(iceServer.username)
                    passwords.add(iceServer.password)
                    urisList.addAll(iceServer.uris)
                }

                // 성공 상태 업데이트
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        WebRTCUiState.Success(
                            channelArn = channelArn ?: "",
                            endpointList = endpointResult.resourceEndpointList,
                            iceServerList = iceServerList,
                            role = role
                        )
                    }

                    // 엔드포인트 데이터 업데이트
                    _endpointData.value = KvsEndpointData(
                        webrtcEndpoint = webrtcEndpoint,
                        mWssEndpoint = mWssEndpoint
                    )

                    Log.d(TAG, "WebRtcViewModel 설정 초기화 완료")
                }

                initializeWebRTC(
                    isMaster = role == ChannelRole.MASTER,
                    userNames = userNames,
                    passwords = passwords,
                    urisList = urisList,
                    context = context,
                    rootEglBase = eglBase
                )
                Log.d(TAG, "initWsConnection Start")

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



    private fun initializeWebRTC(
        isMaster: Boolean,
        userNames: List<String>,
        passwords: List<String>,
        urisList: List<String>,
        context : Context,
        rootEglBase : EglBase.Context?
    ) {

        // Initialize WebRTC
        Log.d("initializeWebRTC", "initializeWebRTC Start")


        val videoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase)
        val videoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase,
            true,
            true
        )

        Log.d("initializeWebRTC", "videoEncoderFactory End")
        try {
            Log.d("initializeWebRTC", "videoEncoderFactory End://")
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(videoDecoderFactory)
                .setVideoEncoderFactory(videoEncoderFactory)
                //뷰어에서 음성전송 받기
                .setAudioDeviceModule(
                    JavaAudioDeviceModule.builder(getApplicationContext())
                    .createAudioDeviceModule())
                .createPeerConnectionFactory()

        } catch(e: Exception) {
            Log.e("initializeWebRTC", "PeerConnectionFactory.builder ERRor")
        }
        Log.d("initializeWebRTC", "PeerConnectionFactory.builder End///"+ peerConnectionFactory.toString())

        // Setup video source and capturer
        videoSource = peerConnectionFactory?.createVideoSource(false)!!
        videoCapturer = createVideoCapturer()!!

        if (videoCapturer == null) {
            Log.e("initCamera", "Failed to create video capturer")
            return
        }

        if (videoSource == null) {
            Log.e("initCamera", "Failed to create video videoSource")
            return
        }

        Log.d("initCamera", "videoCapturer.builder End")
        val surfaceTextureHelper = SurfaceTextureHelper.create(
            "WebRTC-STH",
            rootEglBase
        )

// 캡처러 초기화
        try {
            videoCapturer.initialize(
                surfaceTextureHelper,
                context,
                videoSource?.capturerObserver
            )
        } catch(e: Exception) {
            Log.e("initCamera", "Failed to initialize capturer", e)
            return
        }
        if (videoCapturer.isScreencast) {
            Log.e("initCamera", "Failed to create video initialize")
        }

        Log.d("initializeWebRTC", "videoCapturer.initialize End")
        localVideoTrack = peerConnectionFactory?.createVideoTrack(
            "VideoTrack",
            videoSource
        )!!

        Log.d("initializeWebRTC", "localVideoTrack.initialize End")
        setupPeerConnection(userNames, passwords, urisList)

        videoSource = peerConnectionFactory?.createVideoSource(false)!!

        // 오디오 관련 코드 추가
        try {
            // AudioManager 초기화 및 설정
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // 오디오 제약조건 설정
            val audioConstraints = MediaConstraints()

            // 오디오 소스 생성
            val audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
            // 오디오 트랙 생성
            if (audioSource != null) {
                val audioTrack = peerConnectionFactory?.createAudioTrack("AudioTrack", audioSource)
                audioTrack?.setEnabled(true)
                _localAudioTrack.value = audioTrack
                Log.d(TAG, "Audio track created successfully")
            }

            // 오디오 설정
            audioManager?.apply {
                try {
                    mode = AudioManager.MODE_IN_COMMUNICATION
                    isSpeakerphoneOn = true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to configure AudioManager", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Audio initialization failed", e)
        }

        // Start capturing
        videoCapturer?.startCapture(1280, 720, 30)
        localVideoTrack?.setEnabled(true)
        Log.d("initCamera", "create video startCapture")

    }


    private fun setupPeerConnection(
        userNames: List<String>,
        passwords: List<String>,
        urisList: List<String>
    ) {
        peerIceServers.clear()

        // Add STUN server
        val stun = PeerConnection.IceServer.builder(
            "stun:stun.kinesisvideo.${mRegion}.amazonaws.com:443"
        ).createIceServer()

        Log.d("initializeWebRTC", "stun.builder End")
        peerIceServers.add(stun)

        // Add TURN servers
        if (urisList != null && userNames != null && passwords != null) {
            // 처음 두 개의 TURN 서버만 처리 (turn과 turns)
            val turnServers = urisList.take(2)  // 첫 두 개만 가져오기

            turnServers.forEachIndexed { i, uri ->
                val turnServer = uri.toString()
                val iceServer = PeerConnection.IceServer.builder(
                    turnServer.replace("[", "").replace("]", "")
                )
                    .setUsername(userNames[i])
                    .setPassword(passwords[i])
                    .createIceServer()

                Log.d(TAG, "IceServer details (TURN) = $iceServer")
                peerIceServers.add(iceServer)
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


    private val isCameraFacingFront: Boolean = true

    fun createVideoCapturer(): VideoCapturer? {
        Logging.d(TAG, "Create camera")
        return createCameraCapturer(Camera1Enumerator(false))
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        Logging.d(TAG, "Enumerating cameras")

        for (deviceName in deviceNames) {
            val isDesiredCamera = if (isCameraFacingFront) {
                enumerator.isFrontFacing(deviceName)
            } else {
                enumerator.isBackFacing(deviceName)
            }

            if (isDesiredCamera) {
                Logging.d(TAG, "Camera created")
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    Log.d(TAG, "Created  capturer")
                    return videoCapturer
                }
            }
        }

        return null
    }


    //webRTC코드
    fun initWsConnection(
        roles: String,
    ) {

        var getMaster: Boolean = false
        Log.e(TAG, "initWsConnection 1")
        val localRenderer = _localView.value
        val remoteRenderer = _remoteView.value
        Log.e(TAG, "initWsConnection 2")
        Log.e(TAG, "initWsConnection"+ localRenderer.toString())
        Log.e(TAG, "initWsConnection" + remoteRenderer.toString())
        if (localRenderer == null || remoteRenderer == null) {
            Log.e(TAG, "Surface views not initialized")
            return
        }
        Log.e(TAG, "initWsConnection 3")
        if (peerConnectionFactory == null) {
            Log.e(TAG, "PeerConnectionFactory not initialized")
            return
        }
        Log.e(TAG, "initWsConnection 4")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "masterEndpoint::$mWssEndpoint")
                Log.d(TAG, "masterEndpoint::$Constants.CHANNEL_ARN_QUERY_PARAM")
                Log.d(TAG, "masterEndpoint::$mChannelArn")
                if(roles != "VIEWER") {
                    getMaster = true
                }

                // Master endpoint 생성
                val masterEndpoint =
                    "$mWssEndpoint?${Constants.CHANNEL_ARN_QUERY_PARAM}=$mChannelArn"
                Log.d("initWsConnection", "mWssEndpoint: $mWssEndpoint")
                Log.d(
                    "initWsConnection",
                    "Constants.CHANNEL_ARN_QUERY_PARAM: ${Constants.CHANNEL_ARN_QUERY_PARAM}"
                )
                Log.d("initWsConnection", "Constants.mChannelArn: $mChannelArn")
                Log.d("initWsConnection", "masterEndpoint: $masterEndpoint")

                // Viewer endpoint 생성
                val viewerEndpoint =
                    "$mWssEndpoint?${Constants.CHANNEL_ARN_QUERY_PARAM}=$mChannelArn&${Constants.CLIENT_ID_QUERY_PARAM}=$mClientId"
                Log.d(
                    "initWsConnection",
                    "Constants.CLIENT_ID_QUERY_PARAM: ${Constants.CLIENT_ID_QUERY_PARAM}"
                )
                Log.d("initWsConnection", "viewerEndpoint: $viewerEndpoint")

                // 크레덴셜 가져오기
                withContext(Dispatchers.Main) {
                    mCreds = getCredentialsProvider().credentials
                }

                Log.d("initWsConnection", "viewerEndpoint: ${mCreds.toString()}")
                Log.d("signalingListener::", "Received SDP Offer getSignedUri master:  "+getMaster)
                // URI 서명
                val signedUri = if (getMaster) {
                    getSignedUri(masterEndpoint)
                } else {
                    getSignedUri(viewerEndpoint)
                }

                if (signedUri == null) {
                    gotException = true
                    return@launch
                }

                val userState = AWSMobileClient.getInstance().isSignedIn
                Log.d("AWSMobileClient", "User State22: $userState")

                Log.d(TAG, "wsHost:: "+signedUri.toString())
                val wsHost = signedUri.toString()
                // Step 10. Create Signaling Client Event Listeners
                Log.d("signalingListener::", "viewer createSdpOffer start")
                val signalingListener = object : SignalingListener() {
                    private val isMaster = getMaster

                    override fun onSdpOffer(offerEvent: Event) {
                        Log.d("signalingListener::", "Received SDP Offer: Setting Remote Description ")
                        val sdp = Event.parseOfferEvent(offerEvent)
                        var peerConnection: PeerConnection? = null
                        Log.d("signalingListener::", "Received SDP Offer:  "+offerEvent.senderClientId)

                        if (isMaster) {
                            Log.d("signalingListener::", "Received SDP Offer master:  "+master)
                            if (!masterLocalPeer.containsKey(offerEvent.senderClientId)) {
                                createLocalPeerConnection(offerEvent.senderClientId, isMaster)
                            }
                            Log.d("signalingListener::", "Received SDP Offer createLocalPeerConnection:  "+offerEvent.senderClientId)
                            peerConnection = masterLocalPeer[offerEvent.senderClientId]

                            Log.d("signalingListener::", "Received SDP Offer createLocalPeerConnection end:  "+peerConnection.toString())
                        } else {
                            peerConnection = localPeer
                        }
                        Log.d("signalingListener::", "Received SDP peerConnection start:  ")
                        peerConnection?.let { peer ->
                            peer.setRemoteDescription(
                                KinesisVideoSdpObserver(),
                                SessionDescription(SessionDescription.Type.OFFER, sdp)
                            )
                            recipientClientId = offerEvent.senderClientId
                            Log.d(
                                TAG,
                                "Received SDP offer for client ID: $recipientClientId. Creating answer"
                            )

                            createSdpAnswer(recipientClientId!!, isMaster)

                            if (isMaster && webrtcEndpoint != null) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Media is being recorded to $mStreamArn",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.i(TAG, "Media is being recorded to $mStreamArn")
                                }
                            }
                        }
                    }

                    override fun onSdpAnswer(answerEvent: Event) {
                        Log.d("signalingListener::", "SDP answer received from signaling")

                        val sdp = Event.parseSdpEvent(answerEvent)
                        val sdpAnswer = SessionDescription(SessionDescription.Type.ANSWER, sdp)

                        localPeer?.let { peer ->
                            peer.setRemoteDescription(object : KinesisVideoSdpObserver() {
                                override fun onCreateFailure(error: String) {
                                    super.onCreateFailure(error)
                                }
                            }, sdpAnswer)
                            Log.d("signalingListener::", "Answer Client ID: ${answerEvent.senderClientId}")
                            peerConnectionFoundMap[answerEvent.senderClientId] = peer
                            // Check if ICE candidates are available in the queue and add the candidate
                            handlePendingIceCandidates(answerEvent.senderClientId)
                        }
                    }

                    override fun onIceCandidate(message: Event) {
                        Log.d("signalingListener::", "Received ICE candidate from remote")
                        val iceCandidate = Event.parseIceCandidate(message)
                        if (iceCandidate != null) {
                            checkAndAddIceCandidate(message, iceCandidate)
                        } else {
                            Log.e(TAG, "Invalid ICE candidate: $message")
                        }
                    }

                    override fun onError(errorMessage: Event) {
                        Log.e("signalingListener::", "Received error message: $errorMessage")
                    }

                    override fun onException(e: Exception) {
                        Log.e("signalingListener::", "Signaling client returned exception: ${e.message}")
                        gotException = true
                    }
                }

                // Step 11. Create SignalingServiceWebSocketClient
                try {
                    Log.d( "signalingListener::","Client wsHost:: ${wsHost}")
                    client = SignalingServiceWebSocketClient(
                        wsHost,
                        signalingListener,
                        Executors.newFixedThreadPool(10)
                    )

                    Log.d( "signalingListener::","Client connection ${if (client!!.isOpen()) "Successful" else "Failed"}")
                } catch (e: Exception) {
                    Log.e("signalingListener::", "Exception with websocket client: $e")
                    gotException = true
                    return@launch
                }

                if (isValidClient()) {
                    Log.d("signalingListener::", "Client connected to Signaling service ${client!!.isOpen()}")
                    Log.d("signalingListener::", "Client connected to Signaling mRegion ${mRegion}")
                    Log.d("signalingListener::","Client connected to Signaling service ${webrtcEndpoint}")
                    if (master) {
                        // If webrtc endpoint is non-null ==> Ingest media was checked
                        if (false) {
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    val storageClient = AWSKinesisVideoWebRTCStorageClient(
                                        getCredentialsProvider().credentials
                                    ).apply {
                                        setRegion(Region.getRegion(mRegion))
                                        setSignerRegionOverride(mRegion)
                                        setServiceNameIntern("kinesisvideo")
                                        setEndpoint(webrtcEndpoint)
                                    }

                                    Log.i(TAG, "mChannelArn: $mChannelArn")
                                    storageClient.joinStorageSession(
                                        JoinStorageSessionRequest().withChannelArn(mChannelArn)
                                    )
                                    Log.i(TAG, "Join storage session request sent!")
                                } catch (ex: Exception) {
                                    Log.e(TAG, "Error sending join storage session request!", ex)
                                }
                            }
                        }
                    } else {
                        Log.d(
                            TAG,
                            "Signaling service is connected: Sending offer as viewer to remote peer"
                        )
                        Log.d("signalingListener::", "viewer createSdpOffer start")
                        createSdpOffer(getMaster)
                    }
                } else {
                    Log.e(TAG, "Error in connecting to signaling service")
                    gotException = true
                }

            } catch (e: Exception) {
                Log.e(TAG, "WebSocket connection failed", e)
                gotException = true
            }
        }
    }

    private fun isValidClient(): Boolean {
        return client != null && client!!.isOpen()
    }

    private val _frameData = MutableStateFlow<Bitmap?>(null)
    val frameData = _frameData.asStateFlow()

    private var lastFrameTime = 0L
    private val frameInterval = 5000L // 1초 간격


    private fun convertI420ToBitmap(buffer: VideoFrame.I420Buffer) {
        try {
            val width = buffer.width
            val height = buffer.height

            val ySize = width * height
            val uvSize = ySize / 4

            val nv21 = ByteArray(ySize + uvSize * 2)

            // Y 데이터 복사
            buffer.dataY.get(nv21, 0, ySize)
            Log.d(TAG, "Copied Y data")

            // U와 V 데이터를 NV21 포맷으로 인터리빙
            val uBuffer = buffer.dataU
            val vBuffer = buffer.dataV
            var pos = ySize
            for (i in 0 until uvSize) {
                nv21[pos++] = vBuffer.get(i)
                nv21[pos++] = uBuffer.get(i)
            }
            Log.d(TAG, "Copied UV data")

            // YuvImage로 변환
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            Log.d(TAG, "Created YuvImage")

            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap?.let {
                viewModelScope.launch(Dispatchers.Main) {
                    _frameData.value = it
                    Log.d(TAG, "Posted I420 bitmap: ${it.width}x${it.height}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting I420 to Bitmap", e)
        }
    }
    private fun convertNV21ToBitmap(buffer: VideoFrame.Buffer, rotation: Int) {
        try {
            Log.d(TAG, "Converting frame: ${buffer.width}x${buffer.height}, rotation: $rotation")

            // 먼저 I420로 변환
            val i420Buffer = buffer.toI420()
            Log.d(TAG, "Converted to I420Buffer: ${i420Buffer!!.width}x${i420Buffer.height}")

            try {
                val width = i420Buffer.width
                val height = i420Buffer.height

                // YUV 데이터 준비
                val ySize = width * height
                val uvSize = ySize / 4
                val nv21 = ByteArray(ySize + uvSize * 2)

                // Y 데이터 복사
                i420Buffer.dataY.get(nv21, 0, ySize)
                Log.d(TAG, "Copied Y data")

                // U와 V 데이터를 NV21 포맷으로 인터리빙
                val uBuffer = i420Buffer.dataU
                val vBuffer = i420Buffer.dataV
                var pos = ySize
                for (i in 0 until uvSize) {
                    nv21[pos++] = vBuffer.get(i)
                    nv21[pos++] = uBuffer.get(i)
                }
                Log.d(TAG, "Copied UV data")

                // YuvImage로 변환
                val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
                Log.d(TAG, "Created YuvImage")

                val out = ByteArrayOutputStream()
                yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
                Log.d(TAG, "Compressed to JPEG")

                val imageBytes = out.toByteArray()
                var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: throw Exception("Failed to decode bitmap")
                Log.d(TAG, "Decoded bitmap: ${bitmap.width}x${bitmap.height}")

                // 회전 처리
                if (rotation != 0) {
                    val matrix = Matrix()
                    matrix.postRotate(rotation.toFloat())
                    bitmap = Bitmap.createBitmap(
                        bitmap,
                        0, 0,
                        bitmap.width, bitmap.height,
                        matrix,
                        true
                    )
                    Log.d(TAG, "Applied rotation: $rotation")
                }

                Log.d(TAG, "Successfully created bitmap: ${bitmap.width}x${bitmap.height}")
                viewModelScope.launch(Dispatchers.Main) {
                    _frameData.value = bitmap
                    Log.d(TAG, "Posted bitmap to StateFlow")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error converting YUV to Bitmap", e)
            } finally {
                i420Buffer.release()  // 중요: 메모리 누수 방지
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in frame conversion", e)
        }
    }



    fun initializeSurfaceViews(context: Context, eglBaseContext: EglBase.Context, role: ChannelRole) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                // 기존 view가 있다면 정리
                _localView.value?.release()
                _remoteView.value?.release()

                val localRenderer = SurfaceViewRenderer(context).apply {
                    init(eglBaseContext, null)
                    setEnableHardwareScaler(true)
                    setMirror(true)
                }

                val remoteRenderer = SurfaceViewRenderer(context).apply {
                    init(eglBaseContext, null)
                    setEnableHardwareScaler(true)
                    setMirror(false)
                }

                _localView.value = localRenderer
                _remoteView.value = remoteRenderer
                _isViewsInitialized.value = true

                //AI 연결 + 오디오 트랙을 위한 프레임 처리
                if (role == ChannelRole.MASTER) {  // MASTER 역할 체크 추가
                    try {
                        videoSource = peerConnectionFactory?.createVideoSource(false)!!
                        videoCapturer = createVideoCapturer() ?: throw Exception("Failed to create video capturer")

                        val surfaceTextureHelper = SurfaceTextureHelper.create("WebRTC-STH", eglBaseContext)
                        // Observer 생성
                        val observer = object : CapturerObserver {
                            override fun onFrameCaptured(frame: VideoFrame) {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastFrameTime >= frameInterval) {
                                    lastFrameTime = currentTime
                                    Log.d(TAG, "Frame captured with timestamp: $currentTime")

                                    try {
                                        val buffer = frame.buffer
                                        Log.d(TAG, "Got buffer: ${buffer?.javaClass?.simpleName}")

                                        when (buffer) {
                                            is VideoFrame.I420Buffer -> {
                                                convertI420ToBitmap(buffer)
                                            }
                                            else -> {
                                                convertNV21ToBitmap(buffer, frame.rotation)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error in frame processing", e)
                                    }
                                }
                                videoSource?.capturerObserver?.onFrameCaptured(frame)
                            }

                            override fun onCapturerStarted(success: Boolean) {
                                Log.d(TAG, "Capturer started: $success")
                            }

                            override fun onCapturerStopped() {
                                Log.d(TAG, "Capturer stopped")
                            }
                        }

                        // 초기화
                        videoCapturer.initialize(surfaceTextureHelper, context, observer)
                        videoCapturer.startCapture(1280, 720, 30)

                        localVideoTrack = peerConnectionFactory?.createVideoTrack("local_track", videoSource)!!
                        localVideoTrack?.setEnabled(true)
                        localVideoTrack?.addSink(localRenderer)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to initialize video components", e)
                    }
                    // 로컬 트랙이 있으면 렌더러에 연결
                    localVideoTrack?.addSink(localRenderer)
                }
                Log.d(TAG, "initWsConnection ${role.name}")
                initWsConnection(role.name)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize surface views", e)
                _isViewsInitialized.value = false
            }
        }
    }



    private fun createLocalPeerConnection(clientId: String , isMaster : Boolean) {
        // RTCConfiguration 설정
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
        }
        Log.d("signalingListener::", "createLocalPeerConnection start" + master.toString())


        // Step 8. Create RTCPeerConnection
        if (isMaster) {

            Log.d(TAG,"Received SDP Offer createLocalPeerConnection start")
            val masterConnection = peerConnectionFactory?.createPeerConnection(
                rtcConfig,
                object : KinesisVideoPeerConnection() {
                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        super.onIceCandidate(iceCandidate)
                        val message = createIceCandidateMessage(iceCandidate)
                        Log.d(TAG, "Sending IceCandidate to remote peer $iceCandidate")
                        client?.sendIceCandidate(message)  /* Send to Peer */
                    }

                    override fun onAddStream(mediaStream: MediaStream) {
                        super.onAddStream(mediaStream)
                        Log.d(TAG, "Adding remote video stream (and audio) to the view")
                        addRemoteStreamToVideoView(mediaStream)
                    }

                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                        super.onIceConnectionChange(iceConnectionState)
                        viewModelScope.launch(Dispatchers.Main) {
                            when (iceConnectionState) {
                                PeerConnection.IceConnectionState.FAILED -> {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Connection to peer failed!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                PeerConnection.IceConnectionState.CONNECTED -> {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Connected to peer!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                else -> {}
                            }
                        }
                    }
                }
            )

            Log.d(TAG,"Received SDP Offer masterConnection"+masterConnection.toString())
            if(masterConnection != null) {
                masterLocalPeer[clientId] = masterConnection
                printStatsExecutor.scheduleWithFixedDelay({
                    localPeer?.getStats { rtcStatsReport ->
                        val statsMap = rtcStatsReport.statsMap
                        statsMap.forEach { (key, value) ->
                            Log.d(TAG, "Received SDP Offer: $key, $value")
                        }
                    }
                }, 0, 10, TimeUnit.SECONDS)

                addStreamToLocalPeer(masterConnection, isMaster)
            }
        } else {
            localPeer = peerConnectionFactory?.createPeerConnection(
                rtcConfig,
                object : KinesisVideoPeerConnection() {
                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        super.onIceCandidate(iceCandidate)
                        val message = createIceCandidateMessage(iceCandidate)
                        Log.d(TAG, "Sending IceCandidate to remote peer $iceCandidate")
                        client?.sendIceCandidate(message)  /* Send to Peer */
                    }

                    override fun onAddStream(mediaStream: MediaStream) {
                        super.onAddStream(mediaStream)
                        Log.d(TAG, "Adding remote video stream (and audio) to the view")
                        addRemoteStreamToVideoView(mediaStream)
                    }

                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                        super.onIceConnectionChange(iceConnectionState)
                        viewModelScope.launch(Dispatchers.Main) {
                            when (iceConnectionState) {
                                PeerConnection.IceConnectionState.FAILED -> {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Connection to peer failed!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                PeerConnection.IceConnectionState.CONNECTED -> {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Connected to peer!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                else -> {}
                            }
                        }
                    }
                }
            )

            localPeer?.let { peer ->
                printStatsExecutor.scheduleWithFixedDelay({
                    peer.getStats { rtcStatsReport ->
                        val statsMap = rtcStatsReport.statsMap
                        statsMap.forEach { (key, value) ->
                            Log.d(TAG, "Stats: $key, $value")
                        }
                    }
                }, 0, 10, TimeUnit.SECONDS)

                addStreamToLocalPeer(peer, isMaster)
            }
        }

    }

    private fun getSignedUri(endpoint: String): URI? {
        // AWS 자격증명 추출
        val accessKey = mCreds?.awsAccessKeyId ?: ""
        val secretKey = mCreds?.awsSecretKey ?: ""

        // 세션 토큰 추출 (Optional 처리를 Kotlin 스타일로 변경)
        val sessionToken = Optional.of(mCreds!!)
            .filter { creds: AWSCredentials? -> creds is AWSSessionCredentials }
            .map { awsCredentials: AWSCredentials -> awsCredentials as AWSSessionCredentials }
            .map { obj: AWSSessionCredentials -> obj.sessionToken }
            .orElse("");

        // 로그 출력
        Log.e(TAG, "accessKey: $accessKey")
        Log.e(TAG, "secretKey: $secretKey")
        Log.e(TAG, "sessionToken: $sessionToken")

        // 자격증명 유효성 검사
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    getApplicationContext(),
                    "Failed to fetch credentials!",
                    Toast.LENGTH_LONG
                ).show()
            }
            return null
        }
        Log.e(TAG, "getSignedUri: $endpoint")
        Log.e(TAG, "getSignedUri: $accessKey")
        Log.e(TAG, "getSignedUri: $secretKey")
        Log.e(TAG, "getSignedUri: $sessionToken")
        Log.e(TAG, "getSignedUri: $mWssEndpoint")
        Log.e(TAG, "getSignedUri: $mRegion")
        Log.e(TAG, "getSignedUri: "+Date().time)

        // URI 서명
        return try {
            AwsV4Signer.sign(
                URI.create(endpoint),
                accessKey,
                secretKey,
                sessionToken,
                URI.create(mWssEndpoint ?: ""),
                mRegion,
                Date().time
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign URI", e)
            null
        }
    }

    private fun createSdpAnswer(clientId: String, isMaster: Boolean) {
        val sdpMediaConstraints = MediaConstraints().apply {
            if (isMaster) {
                // 마스터는 오디오를 보내기만 함
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
            } else {
                // 뷰어는 오디오를 받기만 함
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            }
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        Log.d("signalingListener::", "viewer createSdpAnswer createLocalPeerConnection")
        Log.d(TAG, "createSdpAnswer received: clientId=$clientId")

        val peerConnection = masterLocalPeer[clientId]

        peerConnection?.let { peer ->
            peer.createAnswer(object : KinesisVideoSdpObserver() {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    Log.d(TAG, "Creating answer: success")
                    super.onCreateSuccess(sessionDescription)

                    peer.setLocalDescription(KinesisVideoSdpObserver(), sessionDescription)

                    val answer = Message.createAnswerMessage(
                        sessionDescription = sessionDescription,
                        master = master,
                        recipientClientId = recipientClientId
                    )
                    client?.sendSdpAnswer(answer)

                    recipientClientId?.let { recipient ->
                        peerConnectionFoundMap[recipient] = peer
                        handlePendingIceCandidates(recipient)
                    }
                }

                override fun onCreateFailure(error: String) {
                    super.onCreateFailure(error)

                    // Device is unable to support the requested media format
                    if (error.contains("ERROR_CONTENT")) {
                        Log.e(TAG, "No supported codec is present in the offer!")
                    }
                    gotException = true
                }
            }, sdpMediaConstraints)
        } ?: run {
            Log.e(TAG, "No peerConnection!")
        }
    }

    private fun handlePendingIceCandidates(clientId: String) {
        // Add any pending ICE candidates from the queue for the client ID
        Log.d(TAG, "Pending ice candidates found? ${pendingIceCandidatesMap[clientId]}")

        val pendingIceCandidatesQueueByClientId = pendingIceCandidatesMap[clientId]

        while (pendingIceCandidatesQueueByClientId?.isNotEmpty() == true) {
            val iceCandidate = pendingIceCandidatesQueueByClientId.peek()

            peerConnectionFoundMap[clientId]?.let { peer ->
                iceCandidate?.let { candidate ->
                    val addIce = peer.addIceCandidate(candidate)
                    Log.d(
                        TAG,
                        "Added ice candidate after SDP exchange $candidate ${if (addIce) "Successfully" else "Failed"}"
                    )
                }
            }

            pendingIceCandidatesQueueByClientId.remove()
        }

        // After sending pending ICE candidates, the client ID's peer connection need not be tracked
        pendingIceCandidatesMap.remove(clientId)
    }

    private fun checkAndAddIceCandidate(message: Event, iceCandidate: IceCandidate) {
        // If answer/offer is not received, it means peer connection is not found.
        // Hold the received ICE candidates in the map.
        // Once the peer connection is found, add them directly instead of adding it to the queue.
        val senderClientId = message.senderClientId

        if (!peerConnectionFoundMap.containsKey(senderClientId)) {
            Log.d(
                TAG,
                "SDP exchange is not complete. Ice candidate $iceCandidate added to pending queue"
            )

            // If the entry for the client ID already exists (in case of subsequent ICE candidates),
            // update the queue, otherwise create new queue
            val pendingIceCandidatesQueueByClientId = pendingIceCandidatesMap[senderClientId]
                ?: LinkedList<IceCandidate>().also {
                    pendingIceCandidatesMap[senderClientId] = it
                }

            // Add the candidate to the queue
            pendingIceCandidatesQueueByClientId.add(iceCandidate)
        }
        // This is the case where peer connection is established and ICE candidates are received
        // for the established connection
        else {
            Log.d(TAG, "Peer connection found already")
            // Remote sent us ICE candidates, add to local peer connection
            peerConnectionFoundMap[senderClientId]?.let { peer ->
                val addIce = peer.addIceCandidate(iceCandidate)
                Log.d(
                    TAG,
                    "Added ice candidate $iceCandidate ${if (addIce) "Successfully" else "Failed"}"
                )
            }
        }
    }

    private fun addStreamToLocalPeer(inputPeer: PeerConnection,  isMaster : Boolean) {
        peerConnectionFactory?.let { factory ->
            val stream = factory.createLocalMediaStream("KvsLocalMediaStream")

            // 직접 VideoTrack 참조 사용
            localVideoTrack?.let { videoTrack ->
                if (!stream.addTrack(videoTrack)) {
                    Log.e(TAG, "Add video track failed")
                }

                stream.videoTracks?.firstOrNull()?.let { firstVideoTrack ->
                    inputPeer.addTrack(firstVideoTrack, listOf(stream.id))
                    // UI 업데이트를 위한 상태 업데이트
                    _localVideoTrackState.value = firstVideoTrack
                }
                if (isMaster) {
                    // 오디오 트랙 추가
                    try {

                        val audioConstraints = MediaConstraints().apply {
                            mandatory.add(MediaConstraints.KeyValuePair("echoCancellation", "true"))
                            mandatory.add(MediaConstraints.KeyValuePair("noiseSuppression", "true"))
                            mandatory.add(MediaConstraints.KeyValuePair("autoGainControl", "true"))
                        }
                        val audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
                        val audioTrack =
                            peerConnectionFactory?.createAudioTrack("audio_track", audioSource)

                        audioTrack?.let { track ->
                            track.setEnabled(true)
                            val audioSender = inputPeer.addTrack(track)
                            Log.d(
                                TAG,
                                "Audio track added to peer connection: ${audioSender != null}"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add audio track", e)
                    }
                }
            }
        }
    }

    private fun addRemoteStreamToVideoView(stream: MediaStream) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val remoteVideoTrack = stream.videoTracks.firstOrNull()
                val remoteAudioTrack = stream.audioTracks.firstOrNull()

                remoteAudioTrack?.let {AudioTrack ->
                    remoteAudioTrack.setEnabled(true)
                    audioManager?.setMode(AudioManager.MODE_IN_COMMUNICATION)
                    audioManager?.setSpeakerphoneOn(true)

                }
                Log.d(TAG, "Remote stream received: ${stream.id}")
                Log.d(TAG, "Audio tracks count: ${stream.audioTracks?.size}")
                Log.d(TAG, "Video tracks count: ${stream.videoTracks?.size}")
                // 오디오 트랙 로깅 추가
                stream.audioTracks?.forEach { audioTrack ->
                    Log.d(TAG, "Remote audio track found: ${audioTrack.id()}, enabled: ${audioTrack.enabled()}")
                    audioTrack.setEnabled(true)
                } ?: Log.e(TAG, "No audio tracks in remote stream")


                remoteVideoTrack?.let { videoTrack ->
                    Log.d(TAG, "remoteVideoTrackId=${videoTrack.id()} videoTrackState=${videoTrack.state()}")
                    _remoteView.value?.let { renderer ->
                        try {
                            videoTrack.addSink(renderer)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error adding sink to remote video track", e)
                        }
                    } ?: Log.e(TAG, "Remote renderer is null")
                } ?: Log.e(TAG, "Remote video track is null")

            } catch (e: Exception) {
                Log.e(TAG, "Error in setting remote stream", e)
            }
        }
    }

    private fun createIceCandidateMessage(iceCandidate: IceCandidate): Message {
        val sdpMid = iceCandidate.sdpMid
        val sdpMLineIndex = iceCandidate.sdpMLineIndex
        val sdp = iceCandidate.sdp

        // JSON 형식의 메시지 페이로드 생성
        val messagePayload = """
       {
           "candidate":"$sdp",
           "sdpMid":"$sdpMid",
           "sdpMLineIndex":$sdpMLineIndex
       }
   """.trimIndent()

        // master일 경우 빈 문자열, 아닐 경우 mClientId 사용
        val senderClientId = if (master) "" else mClientId

        return Message(
            "ICE_CANDIDATE",
            recipientClientId,
            senderClientId,
            String(
                Base64.encode(
                    messagePayload.toByteArray(),
                    Base64.URL_SAFE or Base64.NO_WRAP
                )
            )
        )
    }
    private fun createSdpOffer(isMaster: Boolean) {
        val sdpMediaConstraints = MediaConstraints().apply {
            if (isMaster) {
                // 마스터는 오디오를 보내기만 함
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
            } else {
                // 뷰어는 오디오를 받기만 함
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            }
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        Log.d("signalingListener::", "viewer createSdpOffer createLocalPeerConnection")
        // 로컬 피어가 없으면 생성
        if (localPeer == null) {
            createLocalPeerConnection(mClientId,master)
        }

        localPeer?.createOffer(object : KinesisVideoSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)

                localPeer?.setLocalDescription(KinesisVideoSdpObserver(), sessionDescription)

                val sdpOfferMessage = Message.createOfferMessage(sessionDescription, mClientId)

                if (isValidClient()) {
                    client?.sendSdpOffer(sdpOfferMessage)
                } else {
                    notifySignalingConnectionFailed()
                }
            }
        }, sdpMediaConstraints)
    }

    private val _connectionEvent = MutableStateFlow<ConnectionEvent?>(null)
    val connectionEvent = _connectionEvent.asStateFlow()

    private fun notifySignalingConnectionFailed() {
        viewModelScope.launch {
            _connectionEvent.value = ConnectionEvent.ConnectionFailed
        }
    }
    fun onConnectionEventHandled() {
        _connectionEvent.value = null
    }


    fun releasePeerConnection() {
        viewModelScope.launch {
            try {
                printStatsExecutor.shutdownNow()
                printStatsExecutor = Executors.newSingleThreadScheduledExecutor()  // 새로운 executor 생성

                masterLocalPeer.forEach { (_, peer) ->
                    peer.close()
                    peer.dispose()
                }
                masterLocalPeer.clear()

                localPeer?.close()
                localPeer?.dispose()
                localPeer = null

                _remoteView.value?.let {
                    it.clearImage()
                    it.release()
                }
                _localView.value?.let {
                    it.clearImage()
                    it.release()
                }

                _remoteView.value = null
                _localView.value = null

            } catch (e: Exception) {
                Log.e("KVSSignalingViewModel", "PeerConnection 정리 실패", e)
            }
        }
    }
    fun updateState(state: WebRTCUiState) {
        _uiState.value = state
    }


    fun resetState() {
        _uiState.value = WebRTCUiState.Initial
        _localView.value = null
        _remoteView.value = null
    }

    private var isBackCamera = false
    private val _isCameraSwitching = MutableStateFlow(false)
    val isCameraSwitching: StateFlow<Boolean> = _isCameraSwitching

    fun switchCamera(context: Context) {
        viewModelScope.launch {
            _isCameraSwitching.value = true
            try {
                (videoCapturer as? CameraVideoCapturer)?.let { capturer ->
                    val enumerator = Camera1Enumerator(false)
                    val deviceNames = enumerator.deviceNames

                    val targetDevice = deviceNames.firstOrNull { deviceName ->
                        if (isBackCamera) {
                            enumerator.isFrontFacing(deviceName)
                        } else {
                            enumerator.isBackFacing(deviceName)
                        }
                    }

                    Log.d("Camera", "Target device: $targetDevice")

                    targetDevice?.let { device ->
                        capturer.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
                            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                                isBackCamera = !isFrontCamera
                                Log.d("Camera", "카메라 전환 완료: ${if(isFrontCamera) "전면" else "후면"}")
                            }

                            override fun onCameraSwitchError(error: String) {
                                Log.e("Camera", "카메라 전환 실패: $error")
                                // 전환 실패 시 캡처 상태 확인 후 필요하면 재시작
                                try {
                                    videoCapturer.startCapture(1280, 720, 30)
                                } catch (e: Exception) {
                                    Log.e("Camera", "캡처 재시작 실패", e)
                                }
                            }
                        }, device)
                    }
                }
            } catch (e: Exception) {
                Log.e("Camera", "카메라 전환 중 에러 발생", e)
            } finally {
                _isCameraSwitching.value = false
            }
        }
    }
    fun captureScreen(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // UI 관련 작업
                val metrics = withContext(Dispatchers.Main) {
                    context.resources.displayMetrics
                }

                val width = metrics.widthPixels
                val height = metrics.heightPixels

                // remoteView 가져오기
                val surfaceView = withContext(Dispatchers.Main) {
                    _remoteView.value
                }

                if (surfaceView == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "캡처할 화면이 없습니다", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // 비트맵 생성
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                // PixelCopy
                val copyResult = withContext(Dispatchers.Main) {
                    suspendCancellableCoroutine { continuation ->
                        try {
                            PixelCopy.request(
                                surfaceView,
                                bitmap,
                                { result ->
                                    continuation.resume(result == PixelCopy.SUCCESS)
                                },
                                Handler(Looper.getMainLooper())
                            )
                        } catch (e: Exception) {
                            continuation.resume(false)
                        }
                    }
                }

                if (!copyResult) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "화면 캡처에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                    bitmap.recycle()
                    return@launch
                }

                // 갤러리 저장
                val filename = "CCTV_${System.currentTimeMillis()}.jpg"
                var fos: OutputStream? = null

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                try {
                    uri?.let {
                        fos = resolver.openOutputStream(it)
                        fos?.let { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "화면이 갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    fos?.close()
                    bitmap.recycle()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "캡처 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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
    data object NoMasterConnected : WebRTCUiState()
}

