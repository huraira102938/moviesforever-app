package com.moviesforever.app.data.model

/**
 * Category ids (Firestore `categories` document ids) that get special treatment.
 * If a category is deleted and created again in the admin panel it gets a NEW id: update it here.
 */
object CategoryRules {
    const val BOLLYWOOD_ID = "L9pOF3RwDdTMA1DWljoa"
    const val SOUTH_ID = "ZeRQutD2FN0BS9qTyeTP"
    const val PUNJABI_ID = "hpyasXGJKaBR9g0RWH3P"

    /** Not shown as shelves on Home. Their movies stay in Search and in the "Show All" below. */
    val HIDDEN_ON_HOME: Set<String> = setOf(SOUTH_ID, PUNJABI_ID)

    /** "Show All" on a category also lists these categories' movies, mixed together. */
    val SHOW_ALL_ALSO_INCLUDES: Map<String, List<String>> = mapOf(
        BOLLYWOOD_ID to listOf(SOUTH_ID)
    )
}
