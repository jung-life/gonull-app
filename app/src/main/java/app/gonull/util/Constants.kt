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
