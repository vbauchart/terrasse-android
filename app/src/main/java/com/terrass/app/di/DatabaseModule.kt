package com.terrass.app.di

import android.content.Context
import androidx.room.Room
import com.terrass.app.data.local.TerasseDatabase
import com.terrass.app.data.local.dao.TerraceDao
import com.terrass.app.data.local.dao.VoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TerasseDatabase =
        Room.databaseBuilder(context, TerasseDatabase::class.java, "terrasse.db").build()

    @Provides
    fun provideTerraceDao(db: TerasseDatabase): TerraceDao = db.terraceDao()

    @Provides
    fun provideVoteDao(db: TerasseDatabase): VoteDao = db.voteDao()
}
