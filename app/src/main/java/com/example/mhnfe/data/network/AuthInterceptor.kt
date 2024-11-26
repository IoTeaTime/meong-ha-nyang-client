package com.example.mhnfe.data.network

import com.example.mhnfe.data.repository.UserRepository
import com.example.mhnfe.data.token.TokenProvider
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository
) : Interceptor {
    private val lock = ReentrantLock()

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // Authorization 헤더가 있는 요청만 가로챈다
        val authorizationHeader = request.header("Authorization")
        if (!authorizationHeader.isNullOrEmpty()) {
            // 1. 현재 저장된 액세스 토큰 가져오기
            val accessToken = tokenProvider.getAccessToken()

            if (!accessToken.isNullOrEmpty()) {
                // Authorization 헤더에 추가
                request = addAuthorizationHeader(request, accessToken)
            }

            val response = chain.proceed(request)

            // 2. 응답이 401 Unauthorized인 경우 액세스 토큰 갱신 시도
            if (response.code == 401) {
                response.close() // 기존 응답 닫기 (리소스 누수 방지)

                lock.withLock {
                    // 갱신된 토큰을 한 번만 요청
                    val refreshToken = tokenProvider.getRefreshToken()

                    if (!refreshToken.isNullOrEmpty()) {
                        val newAccessToken = userRepository.refreshAccessToken(refreshToken)?.toString()
                        if (!newAccessToken.isNullOrEmpty()) {
                            tokenProvider.saveAccessToken(newAccessToken)
                            println("New Access Token saved: $newAccessToken")
                            val newRequest = addAuthorizationHeader(request,
                                newAccessToken.toString()
                            )
                            return chain.proceed(newRequest)
                        } else {
                            println("Failed to refresh access token.")
                        }
                    } else {
                        println("No refresh token available.")
                    }
                }
            }
            return response
        } else {
            // Authorization 헤더가 없는 경우, 요청을 그대로 통과
            return chain.proceed(request)
        }
    }

    private fun addAuthorizationHeader(request: Request, token: String): Request {
        return request.newBuilder()
            .addHeader("Authorization", token)
            .build()
    }
}
