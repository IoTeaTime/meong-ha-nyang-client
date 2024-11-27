package com.example.mhnfe.domain.repository

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.data.remote.response.GroupResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface GroupRepository {
    suspend fun getGroup(response: GroupResponse): Group
}