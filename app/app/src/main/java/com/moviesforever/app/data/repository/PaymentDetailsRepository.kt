package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.PaymentDetails
import kotlinx.coroutines.flow.Flow

interface PaymentDetailsRepository {
    /**
     * Live view of the `settings/payment-details` doc, so an admin panel
     * change is reflected in the app immediately -- no new build needed.
     */
    fun observePaymentDetails(): Flow<PaymentDetails>
}
