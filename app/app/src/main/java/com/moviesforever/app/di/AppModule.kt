package com.moviesforever.app.di

import com.google.firebase.firestore.FirebaseFirestore
import com.moviesforever.app.data.remote.WorkerApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideWorkerApiClient(): WorkerApiClient = WorkerApiClient()
}
