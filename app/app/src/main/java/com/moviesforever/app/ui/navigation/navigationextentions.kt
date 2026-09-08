package com.moviesforever.app.ui.navigation



import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Guards against the classic "rapid double-tap on a back/nav button" bug in
 * Navigation Compose: tapping a back IconButton twice in quick succession can
 * fire popBackStack() twice before the first pop's transition has actually
 * completed, popping TWO entries instead of one. If the back stack is shallow
 * (e.g. [Main, Referral]), that second pop can remove the last remaining
 * destination too, leaving the NavHost with nothing to render -- which shows
 * up to the user as the screen going solid black.
 *
 * The fix: a back stack entry stays in [Lifecycle.State.RESUMED] only while it
 * is the current, visible destination. The instant a pop begins, that entry
 * moves out of RESUMED. So checking this state before popping means a second,
 * near-simultaneous call (which arrives after the first pop already started)
 * sees the entry is no longer RESUMED and safely no-ops instead of popping
 * again.
 */
fun NavController.popBackStackSafe(): Boolean {
    return if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    } else {
        false
    }
}

/** Same idea as [popBackStackSafe], but for forward navigation calls. */
fun NavController.navigateSafe(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(route, builder)
    }
}