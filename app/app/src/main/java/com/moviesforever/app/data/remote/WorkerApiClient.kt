package com.moviesforever.app.data.remote

import com.moviesforever.app.BuildConfig
import com.moviesforever.app.data.remote.dto.RedeemRequest
import com.moviesforever.app.data.remote.dto.RedeemResponse
import com.moviesforever.app.data.remote.dto.SignedUrlRequest
import com.moviesforever.app.data.remote.dto.SignedUrlResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WorkerApiClient {

    private val api: ApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.MOVIESFOREVER_WORKER_URL.let {
                if (it.endsWith("/")) it else "$it/"
            })
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    suspend fun redeem(id: String, username: String): RedeemResponse =
        withContext(Dispatchers.IO) {
            api.redeem(RedeemRequest(id = id, username = username))
        }

    suspend fun getSignedUrl(
        movieId: String,
        id: String?,
        username: String?
    ): SignedUrlResponse = withContext(Dispatchers.IO) {
        api.getSignedUrl(SignedUrlRequest(movieId = movieId, id = id, username = username))
    }
}
