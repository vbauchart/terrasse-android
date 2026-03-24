package com.terrass.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.terrass.app.data.local.entity.VoteEntity

@Dao
interface VoteDao {

    @Insert
    suspend fun insert(vote: VoteEntity): Long

    @Update
    suspend fun update(vote: VoteEntity)

    @Query("SELECT * FROM votes WHERE id = :id")
    suspend fun getById(id: Long): VoteEntity?

    @Query("SELECT * FROM votes WHERE synced = 0")
    suspend fun getUnsynced(): List<VoteEntity>
}
