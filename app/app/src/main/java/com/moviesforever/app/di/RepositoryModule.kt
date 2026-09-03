package com.moviesforever.app.di

import com.moviesforever.app.data.repository.BannersRepository
import com.moviesforever.app.data.repository.BannersRepositoryImpl
import com.moviesforever.app.data.repository.CategoriesRepository
import com.moviesforever.app.data.repository.CategoriesRepositoryImpl
import com.moviesforever.app.data.repository.GenresRepository
import com.moviesforever.app.data.repository.GenresRepositoryImpl
import com.moviesforever.app.data.repository.MoviesRepository
import com.moviesforever.app.data.repository.MoviesRepositoryImpl
import com.moviesforever.app.data.repository.PricingRepository
import com.moviesforever.app.data.repository.PricingRepositoryImpl
import com.moviesforever.app.data.repository.RedemptionRepository
import com.moviesforever.app.data.repository.RedemptionRepositoryImpl
import com.moviesforever.app.data.repository.UnlockRepository
import com.moviesforever.app.data.repository.UnlockRepositoryImpl
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
    abstract fun bindMoviesRepository(impl: MoviesRepositoryImpl): MoviesRepository

    @Binds
    @Singleton
    abstract fun bindCategoriesRepository(impl: CategoriesRepositoryImpl): CategoriesRepository

    @Binds
    @Singleton
    abstract fun bindGenresRepository(impl: GenresRepositoryImpl): GenresRepository

    @Binds
    @Singleton
    abstract fun bindBannersRepository(impl: BannersRepositoryImpl): BannersRepository

    @Binds
    @Singleton
    abstract fun bindPricingRepository(impl: PricingRepositoryImpl): PricingRepository

    @Binds
    @Singleton
    abstract fun bindUnlockRepository(impl: UnlockRepositoryImpl): UnlockRepository

    @Binds
    @Singleton
    abstract fun bindRedemptionRepository(impl: RedemptionRepositoryImpl): RedemptionRepository
}
