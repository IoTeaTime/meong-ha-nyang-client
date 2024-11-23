package com.example.mhnfe.di.module

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.example.mhnfe.data.api.GroupApi
import com.example.mhnfe.data.api.ApiService
import com.example.mhnfe.data.model.Group
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton
import android.provider.Settings
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideThingId(@ApplicationContext context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @Provides
    @Singleton
    fun provideGroupApi(): GroupApi {
        return ApiService.createApiService(GroupApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGroupSerializer(): Serializer<Group> {
        return object : Serializer<Group> {
            override val defaultValue: Group = Group(0, "", "")

            override suspend fun readFrom(input: InputStream): Group {
                return try {
                    Json.decodeFromString(
                        Group.serializer(),
                        input.readBytes().decodeToString()
                    )
                } catch (e: Exception) {
                    defaultValue
                }
            }

            override suspend fun writeTo(t: Group, output: OutputStream) {
                output.write(
                    Json.encodeToString(
                        Group.serializer(),
                        t
                    ).encodeToByteArray()
                )
            }
        }
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
        serializer: androidx.datastore.core.Serializer<Group>
    ): DataStore<Group> {
        return DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.filesDir.resolve("group_data.pb") }
        )
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("login_prefs", MODE_PRIVATE)
    }
}