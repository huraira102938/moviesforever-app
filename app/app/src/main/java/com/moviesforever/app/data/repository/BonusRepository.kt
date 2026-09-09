package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.BonusDeal
import kotlinx.coroutines.flow.Flow

interface BonusRepository {
    /**
     * The current active, non-expired bonus deal (if any), and how many
     * qualifying unlocks [username] has racked up against it so far. Emits
     * null when there is no live bonus deal running right now.
     *
     * "alreadyPaidOut" is intentionally NOT decided here -- this repository
     * only knows about deals/claims. Whether it's been paid depends on the
     * user's own account doc, so the caller (AppViewModel) combines this
     * with [AccountRepository] to fill that in.
     */
    fun observeActiveDealProgress(username: String): Flow<Pair<BonusDeal, Int>?>
}
