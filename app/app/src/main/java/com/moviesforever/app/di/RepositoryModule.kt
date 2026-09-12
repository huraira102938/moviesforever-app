package com.moviesforever.app.di

import com.moviesforever.app.data.repository.AccountRepository
import com.moviesforever.app.data.repository.AccountRepositoryImpl
import com.moviesforever.app.data.repository.AppShareRepository
import com.moviesforever.app.data.repository.AppShareRepositoryImpl
import com.moviesforever.app.data.repository.BannersRepository
import com.moviesforever.app.data.repository.BannersRepositoryImpl
import com.moviesforever.app.data.repository.CategoriesRepository
import com.moviesforever.app.data.repository.CategoriesRepositoryImpl
import com.moviesforever.app.data.repository.ContactDetailsRepository
import com.moviesforever.app.data.repository.ContactDetailsRepositoryImpl
import com.moviesforever.app.data.repository.DownloadRepository
import com.moviesforever.app.data.repository.DownloadRepositoryImpl
import com.moviesforever.app.data.repository.GenresRepository
import com.moviesforever.app.data.repository.GenresRepositoryImpl
import com.moviesforever.app.data.repository.InstallRepository
import com.moviesforever.app.data.repository.InstallRepositoryImpl
import com.moviesforever.app.data.repository.MoviesRepository
import com.moviesforever.app.data.repository.MoviesRepositoryImpl
import com.moviesforever.app.data.repository.NotificationsRepository
import com.moviesforever.app.data.repository.NotificationsRepositoryImpl
import com.moviesforever.app.data.repository.PaymentDetailsRepository
import com.moviesforever.app.data.repository.PaymentDetailsRepositoryImpl
import com.moviesforever.app.data.repository.PricingRepository
import com.moviesforever.app.data.repository.PricingRepositoryImpl
import com.moviesforever.app.data.repository.RedemptionRepository
import com.moviesforever.app.data.repository.RedemptionRepositoryImpl
import com.moviesforever.app.data.repository.ReferralEarningsRepository
import com.moviesforever.app.data.repository.ReferralEarningsRepositoryImpl
import com.moviesforever.app.data.repository.TrendingRepository
import com.moviesforever.app.data.repository.TrendingRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindReferralEarningsRepository(impl: ReferralEarningsRepositoryImpl): ReferralEarningsRepository

    @Binds
    @Singleton
    abstract fun bindAppShareRepository(impl: AppShareRepositoryImpl): AppShareRepository

    @Binds
    @Singleton
    abstract fun bindPaymentDetailsRepository(impl: PaymentDetailsRepositoryImpl): PaymentDetailsRepository

    @Binds
    @Singleton
    abstract fun bindContactDetailsRepository(impl: ContactDetailsRepositoryImpl): ContactDetailsRepository

    @Binds
    @Singleton
    abstract fun bindInstallRepository(impl: InstallRepositoryImpl): InstallRepository

    @Binds
    @Singleton
    abstract fun bindTrendingRepository(impl: TrendingRepositoryImpl): TrendingRepository

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository
}
