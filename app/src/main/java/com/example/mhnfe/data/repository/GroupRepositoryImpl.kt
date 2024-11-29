package com.example.mhnfe.data.repository


import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.request.CreateGroupRequest
import com.example.mhnfe.data.remote.request.Group
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.GroupInfoResponse
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse
import com.example.mhnfe.domain.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val thingId: String
) : GroupRepository {
    override suspend fun getGroup(response: GroupResponse): Group {
        return Group(
            groupId = response.body.groupId,
            groupName = response.body.groupName,
            createdAt = response.body.createdAt
        )
    }

    override suspend fun createGroup(): Result<Group> {
        return try {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val request = CreateGroupRequest(thingId = thingId)
            val response = withContext(Dispatchers.IO) {
                groupApi.createGroup(token, request)
            }
            Result.success(getGroup(response))
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getGroupInfo(): Result<GroupInfoResponse> {
        return try {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val response = withContext(Dispatchers.IO) {
                groupApi.getGroupInfo(token)
            }
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateCctvQR(): QRApiResponse {
        val token = accessTokenDataStore.data.map { it.accessToken }.first()
        return groupApi.generateCctvQR(token)
    }

    override suspend fun generateViewerQR(): QRApiResponse {
        val token = accessTokenDataStore.data.map { it.accessToken }.first()
        return groupApi.generateViewerQR(token)
    }
}