package com.terrass.app.domain.repository

import com.terrass.app.domain.model.SyncStatus
import com.terrass.app.domain.model.Terrace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TerraceRepository {
    val syncStatus: StateFlow<SyncStatus>
    fun getAllTerraces(): Flow<List<Terrace>>
    fun getTerraceById(id: Long): Flow<Terrace?>
    suspend fun addTerrace(terrace: Terrace): Long
    suspend fun updateTerrace(terrace: Terrace)
    suspend fun deleteTerrace(id: Long)
    suspend fun vote(terraceId: Long, isPositive: Boolean)
}
