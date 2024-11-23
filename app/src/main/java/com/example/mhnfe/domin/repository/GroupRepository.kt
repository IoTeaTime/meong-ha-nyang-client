package com.example.mhnfe.domin.repository

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.data.remote.response.GroupResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val dataStore: DataStore<Group>  // Group 타입으로 유지
) {
    // 매개변수를 GroupResponse로 받아서 body만 저장
    suspend fun saveGroupResponse(response: GroupResponse) {
        dataStore.updateData { response.body }  // response.body가 Group 타입
    }

    // Group 타입으로 데이터를 반환
    fun getGroupResponse(): Flow<Group> {
        return dataStore.data
    }
}