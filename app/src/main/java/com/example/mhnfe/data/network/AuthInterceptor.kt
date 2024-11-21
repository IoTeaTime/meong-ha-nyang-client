package com.example.mhnfe.data.network

import android.content.Context
import com.example.mhnfe.data.repository.UserRepository
import com.example.mhnfe.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context, private val userRepository: UserRepository) : Interceptor {

    private val tokenManager = TokenManager(context, userRepository)

    // Access Token을 새로 생성하는 함수
    private fun newRequestWithAccessToken(accessToken: String, originalRequest: okhttp3.Request): okhttp3.Request {
        return originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 특정 URL에 대해서만 동작하도록 조건 추가 ("auth"라는 문자열이 포함된 URL을 제외하고 동작)
        if (!originalRequest.url.toString().contains("auth")) {
            // 액세스 토큰 가져오기
            var accessToken = tokenManager.getAccessToken()

            // 액세스 토큰이 없으면 403 에러 처리
            if (accessToken.isNullOrEmpty()) {
                // 액세스 토큰이 없으면 재로그인
                return chain.proceed(originalRequest)
            }

            // 액세스 토큰을 Authorization 헤더에 추가한 새 요청 생성
            var newRequest = newRequestWithAccessToken(accessToken, originalRequest)

            // 요청을 진행하고 응답을 받음
            var response = chain.proceed(newRequest)

//            // 403 에러가 발생하면 리프레시 토큰으로 액세스 토큰 갱신
//            if (response.code == 403) {
//                // 액세스 토큰 갱신
//                val refreshedToken = refreshAccessToken()
//                if (refreshedToken != null) {
//                    // 토큰이 갱신되었으면 새 요청을 진행
//                    newRequest = newRequestWithAccessToken(refreshedToken, originalRequest)
//                    // 기존 응답을 닫고 새 요청을 진행
//                    response.close()
//                    response = chain.proceed(newRequest)
//                } else {
//                    // 리프레시 토큰으로도 갱신이 실패한 경우
//                    // 로그아웃 처리
//                    tokenManager.clearTokens()
//                }
//            }

            // 401 에러가 발생하면 리프레시 토큰으로 액세스 토큰 갱신
            if (response.code == 401) {
                // 액세스 토큰 갱신
                val refreshedToken = refreshAccessToken()
                if (refreshedToken != null) {
                    // 액세스 토큰이 갱신되었으면 새 요청을 진행
                    newRequest = newRequestWithAccessToken(refreshedToken, originalRequest)
                    // 기존 응답을 닫고 새 요청을 진행
                    response.close()
                    response = chain.proceed(newRequest)
                } else {
                    // 리프레시 토큰으로도 갱신이 실패한 경우
                    tokenManager.clearTokens()
                    return response
                }
            }

            // 만약 새 요청에서 다시 401 에러가 발생하면 더 이상 진행하지 않고 종료
            if (response.code == 401) {
                tokenManager.clearTokens()
                return response
            }
            return response
        }

        // "auth"라는 문자열이 포함되지 않은 요청은 그대로 진행
        return chain.proceed(originalRequest)
    }

    // 리프레시 토큰을 사용하여 액세스 토큰을 갱신
    private fun refreshAccessToken(): String? {
        val refreshToken = tokenManager.getRefreshToken()

        return if (!refreshToken.isNullOrEmpty()) {
            try {
                // 서버에서 새로운 액세스 토큰을 발급받음
                val response = userRepository.refreshAccessToken(refreshToken)

                if (response.result.code == 200) {
                    val newAccessToken = response.body.newAccessToken
                    val newExpirationTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000)

                    // 새로운 액세스 토큰 저장
                    tokenManager.saveAccessToken(newAccessToken ?: "", newExpirationTime)
                    return newAccessToken
                } else {
                    // 리프레시 토큰 갱신 실패
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
}

//package com.example.mhnfe.data.network
//
//import android.content.Context
//import com.example.mhnfe.data.repository.UserRepository
//import com.example.mhnfe.utils.TokenManager
//import okhttp3.Interceptor
//import okhttp3.Response
//
//class AuthInterceptor(private val context: Context) : Interceptor {
//    private val tokenManager = TokenManager(context, UserRepository())
//
//    // SharedPreferences 객체 초기화
//    private val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
//
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val request = chain.request()
//
//
//        // 인증이 필요 없는 요청은 그대로 진행
//        return chain.proceed(request)
//    }
//}
