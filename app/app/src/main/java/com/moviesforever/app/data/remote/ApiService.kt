package com.moviesforever.app.data.remote

import com.moviesforever.app.data.remote.dto.RedeemRequest
import com.moviesforever.app.data.remote.dto.RedeemResponse
import com.moviesforever.app.data.remote.dto.SignedUrlRequest
import com.moviesforever.app.data.remote.dto.SignedUrlResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Cloudflare Worker backend API.
 * Endpoints must be added to the Worker with the configured base URL via
 * BuildConfig.MOVIESFOREVER_WORKER_URL (see BuildConfig field wiring).
 */
interface ApiService {
    @POST("redeem")
    suspend fun redeem(@Body request: RedeemRequest): RedeemResponse

    @POST("signed-url")
    suspend fun getSignedUrl(@Body request: SignedUrlRequest): SignedUrlResponse
}
