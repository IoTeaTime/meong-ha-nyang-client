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
import com.example.mhnfe.data.remote.response.GroupId
import com.example.mhnfe.data.remote.response.MemberId

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
            override val defaultValue: CCTVResponseBody = CCTVResponseBody(0, "")

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

    // 그룹 ID 저장
    @Provides
    @Singleton
    fun provideGroupIdDataStore(
        @ApplicationContext context: Context,
        serializer: Serializer<GroupId>
    ): DataStore<GroupId> {
        return DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.filesDir.resolve("group_id.pb") }
        )
    }

    @Provides
    @Singleton
    fun provideGroupIdSerializer(): Serializer<GroupId> {
        return object : Serializer<GroupId> {
            override val defaultValue: GroupId = GroupId(0)

            override suspend fun readFrom(input: InputStream): GroupId {
                return try {
                    Json.decodeFromString(
                        GroupId.serializer(),
                        input.readBytes().decodeToString()
                    )
                } catch (e: Exception) {
                    defaultValue
                }
            }

            override suspend fun writeTo(t: GroupId, output: OutputStream) {
                output.write(Json.encodeToString(
                    GroupId.serializer(),
                    t
                ).encodeToByteArray())
            }
        }
    }

    // 맴버 ID 저장
    @Provides
    @Singleton
    fun provideMemberIdDataStore(
        @ApplicationContext context: Context,
        serializer: Serializer<MemberId>
    ): DataStore<MemberId> {
        return DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.filesDir.resolve("member_id.pb") }
        )
    }

    @Provides
    @Singleton
    fun provideMemberIdSerializer(): Serializer<MemberId> {
        return object : Serializer<MemberId> {
            override val defaultValue: MemberId = MemberId(0)

            override suspend fun readFrom(input: InputStream): MemberId {
                return try {
                    Json.decodeFromString(
                        MemberId.serializer(),
                        input.readBytes().decodeToString()
                    )
                } catch (e: Exception) {
                    defaultValue
                }
            }

            override suspend fun writeTo(t: MemberId, output: OutputStream) {
                output.write(Json.encodeToString(
                    MemberId.serializer(),
                    t
                ).encodeToByteArray())
            }
        }
    }
}