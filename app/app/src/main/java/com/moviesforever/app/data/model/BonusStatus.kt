package com.moviesforever.app.data.model

/**
 * The current user's live progress against the active [BonusDeal], computed
 * by counting `referral-claims` docs made under that deal's window (see
 * BonusRepositoryImpl). Nothing here is admin-entered except [deal] itself
 * and [alreadyPaidOut] (derived from [UserAccount.bonusPaidDealIds]).
 */
data class BonusStatus(
    val deal: BonusDeal,
    val unlocksAchieved: Int,
    val alreadyPaidOut: Boolean
) {
    val unlocksRemaining: Int
        get() = (deal.unlocksRequired - unlocksAchieved).coerceAtLeast(0)

    val isTargetReached: Boolean
        get() = deal.unlocksRequired > 0 && unlocksAchieved >= deal.unlocksRequired
}
