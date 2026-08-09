package com.tessera.puzzle.di

import com.tessera.puzzle.image.PhotoImporter
import com.tessera.puzzle.image.PhotoImporterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageModule {
    @Binds
    @Singleton
    abstract fun bindPhotoImporter(impl: PhotoImporterImpl): PhotoImporter
}
