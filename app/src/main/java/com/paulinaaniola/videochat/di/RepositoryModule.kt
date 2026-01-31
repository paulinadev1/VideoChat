package com.paulinaaniola.videochat.di

import com.paulinaaniola.videochat.data.VideoChatRepositoryImpl
import com.paulinaaniola.videochat.domain.repository.VideoChatRepository
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
    abstract fun bindVideoChatRepository(
        impl: VideoChatRepositoryImpl
    ): VideoChatRepository
}
