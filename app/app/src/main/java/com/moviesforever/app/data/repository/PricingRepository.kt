package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.PricingSettings

interface PricingRepository {
    fun observePricing(): kotlinx.coroutines.flow.Flow<PricingSettings>
}
