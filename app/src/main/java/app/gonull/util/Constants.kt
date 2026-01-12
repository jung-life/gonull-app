package app.gonull.util

object Constants {
    // Default settings
    const val DEFAULT_UNLOCK_DELAY_MINUTES = 30
    const val DEFAULT_ACCESS_DURATION_MINUTES = 15

    // Unlock delay options (minutes)
    val UNLOCK_DELAY_OPTIONS = listOf(15, 30, 60, 120, 240, 480, 1440)

    // Access duration options (minutes)
    val ACCESS_DURATION_OPTIONS = listOf(5, 10, 15, 30, 60)

    // Request status
    object RequestStatus {
        const val PENDING = "PENDING"
        const val UNLOCKED = "UNLOCKED"
        const val EXPIRED = "EXPIRED"
        const val CANCELLED = "CANCELLED"
    }

    // Event types
    object EventType {
        const val BLOCKED = "BLOCKED"
        const val UNLOCKED = "UNLOCKED"
        const val USED = "USED"
    }

    // Notification channels
    const val TIMER_CHANNEL_ID = "unlock_timer"
    const val ALERTS_CHANNEL_ID = "alerts"

    // Shared preferences keys
    object Prefs {
        const val ONBOARDING_COMPLETE = "onboarding_complete"
        const val DEFAULT_DELAY = "default_delay"
        const val DEFAULT_ACCESS_DURATION = "default_access_duration"
    }

    // Common social media packages
    val SUGGESTED_APPS = listOf(
        "com.instagram.android",
        "com.twitter.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.reddit.frontpage",
        "com.facebook.katana",
        "com.facebook.orca", // Messenger
        "com.snapchat.android",
        "com.google.android.youtube",
        "com.linkedin.android"
    )
}
