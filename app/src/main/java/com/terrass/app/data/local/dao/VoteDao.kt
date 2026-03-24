package com.terrass.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.terrass.app.data.local.entity.VoteEntity

@Dao
interface VoteDao {
    @Insert
    suspend fun insert(vote: VoteEntity)
}
