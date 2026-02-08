package app.gonull.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_modes")
data class FocusModeEntity(
    @PrimaryKey
    val modeType: String, // "GYM", "MEDITATION", "ANALOG"
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

    fun isAnalogMode(): Boolean = modeType == TYPE_ANALOG

    companion object {
        const val TYPE_GYM = "GYM"
        const val TYPE_MEDITATION = "MEDITATION"
        const val TYPE_ANALOG = "ANALOG"

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

        // Essential system packages allowed in Analog Mode (2005 phone)
        val DEFAULT_ANALOG_WHITELIST = listOf(
            // Phone/Dialer
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.android.phone",
            "com.samsung.android.incallui",
            // SMS/Messages
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.samsung.android.messaging",
            // Maps
            "com.google.android.apps.maps",
            // Camera
            "com.google.android.GoogleCamera",
            "com.android.camera",
            "com.android.camera2",
            "com.samsung.android.app.camera",
            "com.sec.android.app.camera",
            // Clock/Alarm
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.sec.android.app.clockpackage",
            // Calculator
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator",
            // Settings (always needed)
            "com.android.settings",
            "com.samsung.android.app.routines",
            // Contacts
            "com.google.android.contacts",
            "com.android.contacts",
            "com.samsung.android.app.contacts"
        )

        fun createGymMode(allowedPackages: List<String> = DEFAULT_MUSIC_APPS): FocusModeEntity {
            return FocusModeEntity(
                modeType = TYPE_GYM,
                isActive = false,
                allowedPackages = allowedPackages.joinToString(",")
            )
        }

        fun createMeditationMode(
            allowedPackages: List<String> = DEFAULT_MUSIC_APPS + DEFAULT_MEDITATION_APPS
        ): FocusModeEntity {
            return FocusModeEntity(
                modeType = TYPE_MEDITATION,
                isActive = false,
                allowedPackages = allowedPackages.distinct().joinToString(",")
            )
        }

        fun createAnalogMode(): FocusModeEntity {
            return FocusModeEntity(
                modeType = TYPE_ANALOG,
                isActive = false,
                allowedPackages = DEFAULT_ANALOG_WHITELIST.joinToString(",")
            )
        }
    }
}
