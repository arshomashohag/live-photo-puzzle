package com.tessera.puzzle.di

import com.tessera.puzzle.camera.CameraController
import com.tessera.puzzle.camera.CameraControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {
    @Binds
    abstract fun bindCameraController(impl: CameraControllerImpl): CameraController
}
