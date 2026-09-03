package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.Category

interface CategoriesRepository {
    fun observeCategories(): kotlinx.coroutines.flow.Flow<List<Category>>
}
