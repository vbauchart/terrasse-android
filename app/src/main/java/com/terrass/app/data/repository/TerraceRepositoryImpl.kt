package com.terrass.app.data.repository

import com.terrass.app.data.local.dao.TerraceDao
import com.terrass.app.data.local.dao.VoteDao
import com.terrass.app.data.local.entity.VoteEntity
import com.terrass.app.data.local.mapper.toDomain
import com.terrass.app.data.local.mapper.toEntity
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerraceRepositoryImpl @Inject constructor(
    private val terraceDao: TerraceDao,
    private val voteDao: VoteDao,
) : TerraceRepository {

    override fun getAllTerraces(): Flow<List<Terrace>> =
        terraceDao.getAllWithVotes().map { list -> list.map { it.toDomain() } }

    override fun getTerraceById(id: Long): Flow<Terrace?> =
        terraceDao.getByIdWithVotes(id).map { it?.toDomain() }

    override suspend fun addTerrace(terrace: Terrace): Long =
        terraceDao.insert(terrace.toEntity())

    override suspend fun updateTerrace(terrace: Terrace) =
        terraceDao.update(terrace.toEntity())

    override suspend fun deleteTerrace(id: Long) =
        terraceDao.deleteById(id)

    override suspend fun vote(terraceId: Long, isPositive: Boolean) =
        voteDao.insert(VoteEntity(terraceId = terraceId, isPositive = isPositive))
}
