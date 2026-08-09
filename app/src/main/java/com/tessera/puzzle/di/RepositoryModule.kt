package com.tessera.puzzle.di

import com.tessera.puzzle.data.repository.BoardRepositoryImpl
import com.tessera.puzzle.data.repository.PuzzleRepositoryImpl
import com.tessera.puzzle.data.repository.StatsRepositoryImpl
import com.tessera.puzzle.data.settings.SettingsRepositoryImpl
import com.tessera.puzzle.domain.repository.BoardRepository
import com.tessera.puzzle.domain.repository.PuzzleRepository
import com.tessera.puzzle.domain.repository.SettingsRepository
import com.tessera.puzzle.domain.repository.StatsRepository
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
    abstract fun bindPuzzleRepository(impl: PuzzleRepositoryImpl): PuzzleRepository

    @Binds
    @Singleton
    abstract fun bindBoardRepository(impl: BoardRepositoryImpl): BoardRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
