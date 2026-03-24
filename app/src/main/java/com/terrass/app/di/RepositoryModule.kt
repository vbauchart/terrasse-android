package com.terrass.app.di

import com.terrass.app.data.repository.TerraceRepositoryImpl
import com.terrass.app.domain.repository.TerraceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTerraceRepository(impl: TerraceRepositoryImpl): TerraceRepository
}
