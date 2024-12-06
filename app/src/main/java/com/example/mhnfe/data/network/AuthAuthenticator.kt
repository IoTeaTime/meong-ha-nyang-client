package com.example.mhnfe.data.network

import android.util.Log
import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.token.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // URL에 "open-api"가 포함된 요청은 인증 로직을 건너뜀
        val urlPath = response.request.url.encodedPath // 예: "/api/v1/open-api/resource"
        if (urlPath.startsWith("/open-api/auth/check-verification")) {
            Log.d("AuthAuthenticator","AuthAuthenticator pass urlPath")
        }

        else if (response.code == HTTP_UNAUTHORIZED) {
            // The access token is expired. Refresh the credentials.
            Log.d("AuthAuthenticator","AuthAuthenticator Start")
            synchronized(this) {
                // Make sure only one coroutine refreshes the token at a time.
                return runBlocking {
                    val newTokenResult = tokenManager.refreshAccessToken()
                    Log.d("AuthAuthenticator","AuthAuthenticator get newToken!!}")
                    if (newTokenResult!=null) {
                        val accessToken = newTokenResult
                        // Update the access token in your storage.
                        accessTokenDataStore.updateData { currentToken ->
                            currentToken.copy(accessToken = accessToken)
                        }
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", accessToken)
                            .build()
                    } else {
                        Log.d("AuthAuthenticator","AuthAuthenticator failed by expired refreshToken!!")
                        return@runBlocking response.request
                    }
                }
            }
        }
        return null
    }
}

/*
package com.example.mhnfe.data.network

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.RefreshToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val refreshTokenDataStore: DataStore<RefreshToken>,
    private val accessTokenDataStore: DataStore<AccessToken>
) : Authenticator {

    private val mutex = Mutex() // Mutex를 사용하여 동시성 제어

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == HTTP_UNAUTHORIZED) {
            val refreshToken = runBlocking {
                refreshTokenDataStore.data.map { it.refreshToken }.first()
            }

            return runBlocking {
                mutex.withLock {
                    val newTokenResult = getNewToken(refreshToken)

                    if (newTokenResult is Result.Success) {
                        val accessToken = newTokenResult.body!!.result.accessToken
                        val newRefreshToken = newTokenResult.body.result.refreshToken

                        // 새로운 AccessToken과 RefreshToken 저장
                        accessTokenDataStore.updateData { currentToken ->
                            currentToken.copy(accessToken = accessToken)
                        }
                        refreshTokenDataStore.updateData { currentToken ->
                            currentToken.copy(refreshToken = newRefreshToken)
                        }

                        // 새로운 토큰으로 요청을 재구성
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", accessToken)
                            .build()
                    } else {
                        // 토큰 갱신 실패 시 기존 요청 반환
                        return@runBlocking null
                    }
                }
            }
        }
        return null
    }

    private fun getNewToken(refreshToken: String): Result<TokenResponse> {
        // 새 토큰 요청 로직 (구체적인 구현은 생략)
        TODO("Implement your token refresh logic here")
    }
}
*/