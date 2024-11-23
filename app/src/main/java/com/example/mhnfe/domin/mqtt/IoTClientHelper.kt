package com.example.mhnfe.domin.mqtt

import android.content.Context
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iot.model.AttachPolicyRequest
import com.amazonaws.services.iot.model.AttachThingPrincipalRequest
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult
import com.amazonaws.services.iot.model.DeleteThingRequest
import com.amazonaws.services.iot.model.RegisterThingRequest
import com.example.mhnfe.BuildConfig
import com.example.mhnfe.R
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IoTClientHelper @Inject constructor(
    private val thingId: String,
) {
    private var client: AWSIotClient

    init {
        client = AWSIotClient(
            BasicAWSCredentials(
                BuildConfig.AWS_ACCESS_KEY, // AWS 액세스 키를 여기에 입력하세요
                BuildConfig.AWS_PRIVATE_KEY  // AWS 비밀 키를 여기에 입력하세요
            )
        )
        client.setRegion(Region.getRegion(BuildConfig.AWS_REGION)) // Region을 입력하세요
    }

    fun getKeyAndCert(): CreateKeysAndCertificateResult {
        val request = CreateKeysAndCertificateRequest()
            .apply { setAsActive = true }
        return client.createKeysAndCertificate(request)
    }

    fun registerDevice(context: Context, result: CreateKeysAndCertificateResult) {
        Security.addProvider(BouncyCastleProvider())

        val attachPolicyRequest = AttachPolicyRequest().apply {
            policyName = "certified_thing"  // 생성한 정책의 이름을 넣습니다.
            target = result.certificateArn  // 인증서 ARN
        }

        client.attachPolicy(attachPolicyRequest)


        val templateBody = context.resources.openRawResource(R.raw.thing_template)
            .bufferedReader().use { it.readText() }

        val registerRequest = RegisterThingRequest().apply {
            this.templateBody = templateBody
            this.parameters = mapOf("DeviceSerialNumber" to thingId)
        }
        client.registerThing(registerRequest)


        val attachThingPrincipalRequest = AttachThingPrincipalRequest().apply {
            thingName = thingId // 연결할 사물의 이름
            principal = result.certificateArn // 인증서 ARN
        }
        client.attachThingPrincipal(attachThingPrincipalRequest)
    }

    fun deleteDevice() {
        val deleteThingRequest = DeleteThingRequest()
        deleteThingRequest.thingName = thingId
        client.deleteThing(deleteThingRequest)
    }
}
