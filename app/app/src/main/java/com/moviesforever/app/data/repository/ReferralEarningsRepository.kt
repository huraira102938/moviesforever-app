package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.ReferralEarnings
import kotlinx.coroutines.flow.Flow

interface ReferralEarningsRepository {
    /**
     * Live pending/paid/total-received totals for [username] as a referrer,
     * computed from the `transactions` collection -- the exact same source
     * and math the admin panel's User Management page uses.
     */
    fun observeEarnings(username: String): Flow<ReferralEarnings>
}
