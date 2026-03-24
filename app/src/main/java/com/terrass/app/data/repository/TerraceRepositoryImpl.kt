package com.terrass.app.data.repository

import com.terrass.app.data.local.dao.TerraceDao
import com.terrass.app.data.local.dao.VoteDao
import com.terrass.app.data.local.entity.VoteEntity
import com.terrass.app.data.local.mapper.toDomain
import com.terrass.app.data.local.mapper.toEntity
import com.terrass.app.data.remote.PocketBaseService
import com.terrass.app.domain.model.SyncStatus
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerraceRepositoryImpl @Inject constructor(
    private val terraceDao: TerraceDao,
    private val voteDao: VoteDao,
    private val pocketBaseService: PocketBaseService,
    private val appScope: CoroutineScope,
) : TerraceRepository {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        appScope.launch {
            syncFromServer()
            pushPendingItems()
            startRealtimeSync()
        }
    }

    private suspend fun syncFromServer() {
        _syncStatus.value = SyncStatus.SYNCING
        runCatching {
            val dtos = pocketBaseService.fetchAllTerraces()
            dtos.forEach { terraceDao.upsert(it.toEntity().copy(synced = true)) }
            _syncStatus.value = SyncStatus.UP_TO_DATE
        }.onFailure {
            _syncStatus.value = SyncStatus.OFFLINE
        }
    }

    private fun startRealtimeSync() {
        appScope.launch {
            var attempt = 0L
            while (true) {
                _syncStatus.value = SyncStatus.SYNCING
                runCatching {
                    pocketBaseService.subscribeToEvents().collect { event ->
                        _syncStatus.value = SyncStatus.UP_TO_DATE
                        attempt = 0
                        when (event.action) {
                            "create", "update" -> event.record?.let {
                                terraceDao.upsert(it.toEntity().copy(synced = true))
                            }
                            "delete" -> event.recordId?.let { remoteId ->
                                terraceDao.getByRemoteId(remoteId)?.let { terraceDao.deleteById(it.id) }
                            }
                        }
                    }
                }.onFailure {
                    _syncStatus.value = SyncStatus.OFFLINE
                }
                attempt++
                delay(minOf(2000L * attempt, 30_000L))
            }
        }
    }

    private suspend fun pushPendingItems() {
        runCatching {
            terraceDao.getUnsynced().forEach { entity ->
                if (entity.remoteId == null) {
                    val remoteId = pocketBaseService.createTerrace(entity)
                    terraceDao.update(entity.copy(remoteId = remoteId, synced = true))
                } else {
                    pocketBaseService.updateTerrace(entity.remoteId, entity)
                    terraceDao.update(entity.copy(synced = true))
                }
            }
        }
    }

    override fun getAllTerraces(): Flow<List<Terrace>> =
        terraceDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getTerraceById(id: Long): Flow<Terrace?> =
        terraceDao.getByIdFlow(id).map { it?.toDomain() }

    override suspend fun addTerrace(terrace: Terrace): Long {
        val entity = terrace.toEntity()
        val localId = terraceDao.insert(entity)
        appScope.launch {
            runCatching { pocketBaseService.createTerrace(entity) }
                .onSuccess { remoteId ->
                    terraceDao.getById(localId)?.let {
                        terraceDao.update(it.copy(remoteId = remoteId, synced = true))
                    }
                }
        }
        return localId
    }

    override suspend fun updateTerrace(terrace: Terrace) {
        val existing = terraceDao.getById(terrace.id)
        val entity = terrace.toEntity().copy(
            remoteId = existing?.remoteId,
            synced = false,
            thumbsUp = existing?.thumbsUp ?: 0,
            thumbsDown = existing?.thumbsDown ?: 0,
        )
        terraceDao.update(entity)
        appScope.launch {
            entity.remoteId?.let { remoteId ->
                runCatching { pocketBaseService.updateTerrace(remoteId, entity) }
                    .onSuccess { terraceDao.update(entity.copy(synced = true)) }
            }
        }
    }

    override suspend fun deleteTerrace(id: Long) {
        val entity = terraceDao.getById(id)
        terraceDao.deleteById(id)
        appScope.launch {
            entity?.remoteId?.let { remoteId ->
                runCatching { pocketBaseService.deleteTerrace(remoteId) }
            }
        }
    }

    override suspend fun vote(terraceId: Long, isPositive: Boolean) {
        val entity = terraceDao.getById(terraceId) ?: return
        val updated = if (isPositive) entity.copy(thumbsUp = entity.thumbsUp + 1)
                      else entity.copy(thumbsDown = entity.thumbsDown + 1)
        terraceDao.update(updated)
        val voteId = voteDao.insert(VoteEntity(terraceId = terraceId, isPositive = isPositive))
        entity.remoteId?.let { remoteId ->
            appScope.launch {
                runCatching { pocketBaseService.addVote(remoteId, isPositive) }
                    .onSuccess {
                        voteDao.getById(voteId)?.let { voteDao.update(it.copy(synced = true)) }
                    }
            }
        }
    }
}
