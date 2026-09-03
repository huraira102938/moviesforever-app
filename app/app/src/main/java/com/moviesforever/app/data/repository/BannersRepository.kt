package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.Banner

interface BannersRepository {
    fun observeBanners(): kotlinx.coroutines.flow.Flow<List<Banner>>
}
