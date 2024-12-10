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
            return null
        }

        else if (response.code == HTTP_UNAUTHORIZED) {
            // The access token is expired. Refresh the credentials.
            Log.d("AuthAuthenticator","AuthAuthenticator Start")
            synchronized(this) {
                // Make sure only one coroutine refreshes the token at a time.
                return runBlocking {
//                    val newTokenResult = tokenManager.refreshAccessToken()
                    try {
                        Log.d("AuthAuthenticator", "get newToken try")
                        val newTokenResult = tokenManager.refreshAccessToken()
                        Log.d("AuthAuthenticator", "AuthAuthenticator get newToken!!}")
                        if (newTokenResult != null) {
                            val accessToken = newTokenResult
                            // Update the access token in your storage.
                            accessTokenDataStore.updateData { currentToken ->
                                currentToken.copy(accessToken = accessToken)
                            }
                            return@runBlocking response.request.newBuilder()
                                .header("Authorization", accessToken)
                                .build()
                        } else {
                            Log.d(
                                "AuthAuthenticator",
                                "AuthAuthenticator failed by expired refreshToken!!"
                            )
                            return@runBlocking null
                        }
                    } catch (e: Exception) {
                        // 예외가 발생하면 로그아웃 처리
                        Log.e("AuthAuthenticator", "Error while refreshing token", e)
                        return@runBlocking null
                    }
                }
            }
        }
        return null
    }
}