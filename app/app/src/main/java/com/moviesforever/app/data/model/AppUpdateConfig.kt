package com.moviesforever.app.data.model

/**
 * Remote "force update" switch, stored by the admin panel in Firestore at `settings/appUpdate`.
 *
 * A build is blocked when [enabled] is true AND its versionCode is lower than [minVersionCode].
 * Everything shown on the block screen (message, contact number, download link) comes from here,
 * so it can be changed from the admin panel without releasing a new build.
 */
data class AppUpdateConfig(
    val enabled: Boolean = false,
    val minVersionCode: Long = 0L,
    val title: String = "",
    val message: String = "",
    val contactNumber: String = "",
    val downloadUrl: String = ""
) {
    fun blocks(currentVersionCode: Long): Boolean = enabled && currentVersionCode < minVersionCode
}
