package com.terrass.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.terrass.app.data.local.entity.TerraceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TerraceDao {

    @Query("SELECT * FROM terraces WHERE status = 'active'")
    fun getAll(): Flow<List<TerraceEntity>>

    @Query("SELECT * FROM terraces WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TerraceEntity?>

    @Query("SELECT * FROM terraces WHERE id = :id")
    suspend fun getById(id: Long): TerraceEntity?

    @Query("SELECT * FROM terraces WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): TerraceEntity?

    @Query("SELECT * FROM terraces WHERE synced = 0")
    suspend fun getUnsynced(): List<TerraceEntity>

    @Insert
    suspend fun insert(terrace: TerraceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(terrace: TerraceEntity): Long

    @Update
    suspend fun update(terrace: TerraceEntity)

    @Query("DELETE FROM terraces WHERE id = :id")
    suspend fun deleteById(id: Long)
}
