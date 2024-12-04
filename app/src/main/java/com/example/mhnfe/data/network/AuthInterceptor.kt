package com.example.mhnfe.data.network

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.response.AccessToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val accessTokenDataStore: DataStore<AccessToken>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val accessToken = accessTokenDataStore.data.map { it.accessToken }.first()

            // 헤더에 "Authorization" 키가 존재하는 경우에만 intercept 수행
            val request = if (accessToken.isNotEmpty() && chain.request().headers["Authorization"] != null) {
                chain.request().putTokenHeader(accessToken)
            } else {
                chain.request()
            }

            chain.proceed(request)
        }
    }

    private fun Request.putTokenHeader(accessToken: String): Request {
        return this.newBuilder()
            .addHeader(AUTHORIZATION, accessToken)
            .build()
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
    }
}
