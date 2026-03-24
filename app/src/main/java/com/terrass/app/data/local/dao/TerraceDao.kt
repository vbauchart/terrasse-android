package com.terrass.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.terrass.app.data.local.entity.TerraceEntity
import com.terrass.app.data.local.entity.TerraceWithVotes
import kotlinx.coroutines.flow.Flow

@Dao
interface TerraceDao {

    @Query("""
        SELECT t.*,
               COALESCE(SUM(CASE WHEN v.is_positive = 1 THEN 1 ELSE 0 END), 0) AS thumbsUp,
               COALESCE(SUM(CASE WHEN v.is_positive = 0 THEN 1 ELSE 0 END), 0) AS thumbsDown
        FROM terraces t
        LEFT JOIN votes v ON t.id = v.terrace_id
        WHERE t.status = 'active'
        GROUP BY t.id
    """)
    fun getAllWithVotes(): Flow<List<TerraceWithVotes>>

    @Query("""
        SELECT t.*,
               COALESCE(SUM(CASE WHEN v.is_positive = 1 THEN 1 ELSE 0 END), 0) AS thumbsUp,
               COALESCE(SUM(CASE WHEN v.is_positive = 0 THEN 1 ELSE 0 END), 0) AS thumbsDown
        FROM terraces t
        LEFT JOIN votes v ON t.id = v.terrace_id
        WHERE t.id = :id
        GROUP BY t.id
    """)
    fun getByIdWithVotes(id: Long): Flow<TerraceWithVotes?>

    @Query("SELECT * FROM terraces WHERE id = :id")
    suspend fun getById(id: Long): TerraceEntity?

    @Insert
    suspend fun insert(terrace: TerraceEntity): Long

    @Update
    suspend fun update(terrace: TerraceEntity)

    @Query("DELETE FROM terraces WHERE id = :id")
    suspend fun deleteById(id: Long)
}
