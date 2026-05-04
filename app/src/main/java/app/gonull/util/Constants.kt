package app.gonull.util

object Constants {
    // Public-facing URLs
    const val PRIVACY_POLICY_URL = "https://gist.github.com/jung-life/401c90e08c2d1eb0892eb4fd5d57189f"
    const val BETA_FEEDBACK_URL = "https://docs.google.com/forms/d/e/1FAIpQLSdWKNJ4k7niGWrkikrRzSllXH5CaRljbh2vQ7PmUAK8QlbsXg/viewform"

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
    const val ACCESS_MONITORING_CHANNEL_ID = "access_monitoring"
    const val ACCESS_WARNING_CHANNEL_ID = "access_warning"

    // Access session constants
    object AccessSession {
        const val WARNING_THRESHOLD_MS = 2 * 60 * 1000L // 2 minutes before expiration
        const val CHECK_INTERVAL_MS = 5000L // Check every 5 seconds
    }

    // Progressive Friction constants
    object Friction {
        const val LEVEL_1_BYPASS = 1  // Code + Math (current)
        const val LEVEL_2_BYPASS = 2  // Code + Math + 5 min wait
        const val LEVEL_3_BYPASS = 3  // Code + Math + 15 min wait + Reflection
        const val LEVEL_4_LOCKED = 4  // Locked until tomorrow

        const val LEVEL_2_WAIT_MINUTES = 5
        const val LEVEL_3_WAIT_MINUTES = 15
        const val MIN_REFLECTION_CHARS = 50
    }

    // Notification types for accountability partner
    object NotificationType {
        const val EMERGENCY_BYPASS = "EMERGENCY_BYPASS"
        const val MULTIPLE_BYPASSES = "MULTIPLE_BYPASSES"
        const val BUDGET_EXCEEDED = "BUDGET_EXCEEDED"
    }

    // Budget constants
    object Budget {
        val BUDGET_OPTIONS = listOf(5, 10, 15, 30, 60, 120) // minutes
    }

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

    // The Usual Suspects - apps engineered for maximum engagement
    val USUAL_SUSPECTS = mapOf(
        // Social Media
        "com.instagram.android" to "Instagram",
        "com.facebook.katana" to "Facebook",
        "com.facebook.lite" to "Facebook Lite",
        "com.twitter.android" to "Twitter/X",
        "com.x.android" to "X",
        "com.snapchat.android" to "Snapchat",
        "com.linkedin.android" to "LinkedIn",
        "com.pinterest" to "Pinterest",
        "com.tumblr" to "Tumblr",
        "com.bereal.ft" to "BeReal",
        "com.vkontakte.android" to "VK",

        // Short-form Video (highest dopamine)
        "com.zhiliaoapp.musically" to "TikTok",
        "com.ss.android.ugc.trill" to "TikTok",
        "com.google.android.youtube" to "YouTube",
        "com.google.android.apps.youtube.music" to "YouTube Music",
        "tv.twitch.android.app" to "Twitch",
        "com.reddit.frontpage" to "Reddit",
        "com.rubenmayayo.reddit" to "Boost for Reddit",
        "com.laurencedawson.reddit_sync" to "Sync for Reddit",

        // Messaging (variable rewards)
        "com.facebook.orca" to "Messenger",
        "com.whatsapp" to "WhatsApp",
        "org.telegram.messenger" to "Telegram",
        "com.discord" to "Discord",
        "com.Slack" to "Slack",

        // Dating (intermittent reinforcement)
        "com.tinder" to "Tinder",
        "com.bumble.app" to "Bumble",
        "com.hinge.app" to "Hinge",
        "com.badoo.mobile" to "Badoo",
        "com.okcupid.okcupid" to "OkCupid",

        // Entertainment/Streaming
        "com.netflix.mediaclient" to "Netflix",
        "com.amazon.avod.thirdpartyclient" to "Prime Video",
        "com.disney.disneyplus" to "Disney+",
        "com.hbo.hbonow" to "HBO Max",
        "com.spotify.music" to "Spotify",

        // News (doom scrolling)
        "flipboard.app" to "Flipboard",
        "com.google.android.googlequicksearchbox" to "Google",
        "com.apple.news" to "Apple News",

        // Gaming (gacha/rewards)
        "com.king.candycrushsaga" to "Candy Crush",
        "com.supercell.clashofclans" to "Clash of Clans",
        "com.supercell.clashroyale" to "Clash Royale",
        "com.roblox.client" to "Roblox",

        // Shopping (dopamine from deals)
        "com.amazon.mShop.android.shopping" to "Amazon",
        "com.alibaba.aliexpresshd" to "AliExpress",
        "com.shopee.ph" to "Shopee",
        "com.ebay.mobile" to "eBay"
    )

    // Package name keywords for detection
    val USUAL_SUSPECT_KEYWORDS = listOf(
        "instagram", "facebook", "twitter", "tiktok", "snapchat",
        "reddit", "youtube", "twitch", "discord", "whatsapp",
        "telegram", "tinder", "bumble", "netflix", "spotify"
    )
}
