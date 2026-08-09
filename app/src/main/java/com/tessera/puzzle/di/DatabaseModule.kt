package com.tessera.puzzle.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.tessera.puzzle.data.db.TesseraDatabase
import com.tessera.puzzle.data.db.dao.BoardDao
import com.tessera.puzzle.data.db.dao.PuzzleDao
import com.tessera.puzzle.data.db.dao.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "tessera_settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TesseraDatabase =
        Room.databaseBuilder(context, TesseraDatabase::class.java, TesseraDatabase.NAME)
            // Pre-release: destructive on schema change; real migrations authored
            // before first production release (Phase 6). Schema is exported.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePuzzleDao(db: TesseraDatabase): PuzzleDao = db.puzzleDao()

    @Provides
    fun provideBoardDao(db: TesseraDatabase): BoardDao = db.boardDao()

    @Provides
    fun provideStatsDao(db: TesseraDatabase): StatsDao = db.statsDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.settingsDataStore
}
