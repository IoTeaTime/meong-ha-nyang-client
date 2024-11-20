package com.example.mhnfe.data.service

import com.example.mhnfe.data.model.request.RequestFcmToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// TODO. okHttpClient를 매번 만들게 돼서, 개선할 필요가 있음
// TODO. url을 문자열로 두는 것보다 공통변수 또는 환경변수로 추출하는 것이 좋아 보임
class FcmService {
    private val url = "https://api.meonghanyang.kro.kr/api/fcm/token"
    private val okHttpClient = OkHttpClient()

    /**
     * 기기마다 발급되는 FCM 토큰을 서버로 보내 계정과 연결하는 메서드입니다.
     */
    suspend fun saveFcmToken(requestBody: RequestFcmToken) {
        val gson = Gson()
        val json = gson.toJson(requestBody)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "") // TODO. 로그인 성공 후 Authorization Token 연결 필요
            .post(json.toRequestBody(MEDIA_TYPE_JSON))
            .build()

        withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                println(response.body!!.string())
            }
        }
    }

    companion object {
        val MEDIA_TYPE_JSON= "application/json; charset=utf-8".toMediaType()
    }
}