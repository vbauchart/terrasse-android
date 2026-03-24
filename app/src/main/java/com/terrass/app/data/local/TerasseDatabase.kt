package com.terrass.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.terrass.app.data.local.dao.TerraceDao
import com.terrass.app.data.local.dao.VoteDao
import com.terrass.app.data.local.entity.TerraceEntity
import com.terrass.app.data.local.entity.VoteEntity

@Database(
    entities = [TerraceEntity::class, VoteEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class TerasseDatabase : RoomDatabase() {
    abstract fun terraceDao(): TerraceDao
    abstract fun voteDao(): VoteDao
}
