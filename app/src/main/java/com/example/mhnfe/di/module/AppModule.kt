package com.example.mhnfe.di.module

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.RefreshToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton
import android.content.Context.MODE_PRIVATE
import com.example.mhnfe.data.remote.response.CCTVResponseBody

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 로그인 직렬화
    @Provides
    @Singleton
    fun provideLoginRequestSerializer(): Serializer<LoginRequest> {
        return object : Serializer<LoginRequest> {
            override val defaultValue: LoginRequest = LoginRequest("", "")

            override suspend fun readFrom(input: InputStream): LoginRequest {
                return try {
                    Json.decodeFromString(LoginRequest.serializer(), input.readBytes().decodeToString())
                } catch (e: Exception) {
                    defaultValue
                }
            }

            override suspend fun writeTo(t: LoginRequest, output: OutputStream) {
                output.write(Json.encodeToString(LoginRequest.serializer(), t).encodeToByteArray())
            }
        }
    }

    // 로그인 데이터 제공
    @Provides
    @Singleton
    fun provideLoginRequestDataStore(
        @ApplicationContext context: Context,
        serializer: Serializer<LoginRequest>
    ): DataStore<LoginRequest> {
        return DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.filesDir.resolve("login_request.pb") }
        )
    }

    // 엑세스 토큰 직렬화
    @Provides
    @Singleton
    fun provideAccessTokenSerializer(): Serializer<AccessToken> {
        return object : Serializer<AccessToken> {
            override val defaultValue: AccessToken = AccessToken("")

            override suspend fun readFrom(input: InputStream): AccessToken {
                return try {
                    Json.decodeFromString(
                        AccessToken.serializer(),
                        input.readBytes().decodeToString()
                    )
                } catch (e: Exception) {
                    defaultValue
                }
            }

            override suspend fun writeTo(t: AccessToken, output: OutputStream) {
                output.write(Json.encodeToString(
                    AccessToken.serializer(),
                    t
                ).encodeToByteArray())
            }
        }
    }

    // 리프레시 토큰 직렬화
    @Provides
    @Singleton
    fun provideRefreshTokenSerializer(): Serializer<RefreshToken> {
        return object : Serializer<RefreshToken> {
            override val defaultValue: RefreshToken = RefreshToken("")

            override suspend fun readFrom(input: InputStream): RefreshToken {
                return try {
                    Json.decodeFromString(
                        RefreshToken.serializer(),
                        input.readBytes().decodeToString()
                    )
                } catch (e: Exception) {
                    defaultValue
                }
            }

            override suspend fun writeTo(t: RefreshToken, output: OutputStream) {
                output.write(Json.encodeToString(
                    RefreshToken.serializer(),
                    t
                ).encodeToByteArray())
            }
        }
    }

    // 엑세스 토큰 제공
    @Provides
    @Singleton
    fun provideThingId(@ApplicationContext context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // 엑세스 토큰 제공
    @Provides
    @Singleton
    fun provideAccessTokenDataStore(
        @ApplicationContext context: Context,
        serializer: Serializer<AccessToken>
    ): DataStore<AccessToken> {
        return DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.filesDir.resolve("access_token.pb") }
        )
    }

    // 리프레시 토큰 제공
    @Provides
    @Singleton
    fun provideRefreshTokenDataStore(
        @ApplicationContext context: Context,
        serializer: Serializer<RefreshToken>
    ): DataStore<RefreshToken> {
        return DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.filesDir.resolve("refresh_token.pb") }
        )
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("login_prefs", MODE_PRIVATE)
    }

    // CCTV ID 직렬화
    @Provides
    @Singleton
    fun provideCctvResponseSerializer(): Serializer<CCTVResponseBody> {
        return object : Serializer<CCTVResponseBody> {
            override val defaultValue: CCTVResponseBody = CCTVResponseBody(0)

            override suspend fun readFrom(input: InputStream): CCTVResponseBody {
                return try {
                    Json.decodeFromString(
                        CCTVResponseBody.serializer(),
                        input.readBytes().decodeToString()
                    )
                } catch (e: Exception) {
                    defaultValue
                }
            }

            override suspend fun writeTo(t: CCTVResponseBody, output: OutputStream) {
                output.write(Json.encodeToString(
                    CCTVResponseBody.serializer(),
                    t
                ).encodeToByteArray())
            }
        }
    }

    // CCTV Response 데이터스토어 제공
    @Provides
    @Singleton
    fun provideCctvResponseDataStore(
        @ApplicationContext context: Context,
        serializer: Serializer<CCTVResponseBody>
    ): DataStore<CCTVResponseBody> {
        return DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.filesDir.resolve("cctv_id.pb") }
        )
    }
}