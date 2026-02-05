package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_modes")
data class FocusModeEntity(
    @PrimaryKey
    val modeType: String, // "GYM", "YOGA", "CUSTOM"
    val isActive: Boolean = false,
    val activatedAt: Long? = null,
    val expiresAt: Long? = null, // null = manual deactivation, otherwise auto-expires
    val allowedPackages: String = "" // comma-separated package names
) {
    fun getAllowedPackagesList(): List<String> {
        return if (allowedPackages.isBlank()) {
            emptyList()
        } else {
            allowedPackages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    companion object {
        const val TYPE_GYM = "GYM"
        const val TYPE_YOGA = "YOGA"

        // Default music/audio app packages
        val DEFAULT_MUSIC_APPS = listOf(
            "com.spotify.music",
            "com.apple.android.music",
            "com.google.android.apps.youtube.music",
            "com.amazon.mp3",
            "com.pandora.android",
            "com.soundcloud.android",
            "deezer.android.app",
            "com.jio.media.jiobeats",
            "com.gaana",
            "com.bsbportal.music",
            "com.tencent.qqmusic",
            "com.sec.android.app.music"
        )

        // Meditation and calm apps
        val DEFAULT_MEDITATION_APPS = listOf(
            "com.calm.android",
            "com.getsomeheadspace.android",
            "co.thefabulous.app",
            "com.meditation.elevenminute",
            "com.insighttimer.android",
            "net.meditofoundation.medito",
            "com.simple.habit",
            "com.waking_up.android"
        )

        fun createGymMode(allowedPackages: List<String> = DEFAULT_MUSIC_APPS): FocusModeEntity {
            return FocusModeEntity(
                modeType = TYPE_GYM,
                isActive = false,
                allowedPackages = allowedPackages.joinToString(",")
            )
        }

        fun createYogaMode(
            allowedPackages: List<String> = DEFAULT_MUSIC_APPS + DEFAULT_MEDITATION_APPS
        ): FocusModeEntity {
            return FocusModeEntity(
                modeType = TYPE_YOGA,
                isActive = false,
                allowedPackages = allowedPackages.distinct().joinToString(",")
            )
        }
    }
}
