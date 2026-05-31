package app.gonull.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [FocusModeEntity] — the whitelist parsing and focus-mode
 * factory logic. Gym/Meditation/Analog modes block everything *except* an
 * allow-list, so a bug in this parsing has previously let blocked apps through
 * (see the "Fix Gym/Meditation modes" history). These tests pin the behavior.
 */
class FocusModeEntityTest {

    // --- getAllowedPackagesList parsing -----------------------------------

    @Test
    fun `blank allow-list parses to empty`() {
        assertTrue(FocusModeEntity(modeType = "GYM", allowedPackages = "").getAllowedPackagesList().isEmpty())
        assertTrue(FocusModeEntity(modeType = "GYM", allowedPackages = "   ").getAllowedPackagesList().isEmpty())
    }

    @Test
    fun `single package parses to a one-element list`() {
        val list = FocusModeEntity(modeType = "GYM", allowedPackages = "com.spotify.music")
            .getAllowedPackagesList()
        assertEquals(listOf("com.spotify.music"), list)
    }

    @Test
    fun `multiple packages split on comma`() {
        val list = FocusModeEntity(
            modeType = "GYM",
            allowedPackages = "com.spotify.music,com.apple.android.music"
        ).getAllowedPackagesList()
        assertEquals(listOf("com.spotify.music", "com.apple.android.music"), list)
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        val list = FocusModeEntity(
            modeType = "GYM",
            allowedPackages = " com.spotify.music , com.pandora.android "
        ).getAllowedPackagesList()
        assertEquals(listOf("com.spotify.music", "com.pandora.android"), list)
    }

    @Test
    fun `empty entries from stray commas are dropped`() {
        val list = FocusModeEntity(
            modeType = "GYM",
            allowedPackages = "com.spotify.music,,com.pandora.android,"
        ).getAllowedPackagesList()
        assertEquals(listOf("com.spotify.music", "com.pandora.android"), list)
    }

    // --- isAnalogMode ------------------------------------------------------

    @Test
    fun `isAnalogMode is true only for the analog type`() {
        assertTrue(FocusModeEntity(modeType = FocusModeEntity.TYPE_ANALOG).isAnalogMode())
        assertFalse(FocusModeEntity(modeType = FocusModeEntity.TYPE_GYM).isAnalogMode())
        assertFalse(FocusModeEntity(modeType = FocusModeEntity.TYPE_MEDITATION).isAnalogMode())
    }

    // --- factory builders --------------------------------------------------

    @Test
    fun `createGymMode produces an inactive gym mode with the given allow-list`() {
        val mode = FocusModeEntity.createGymMode(listOf("com.spotify.music"))
        assertEquals(FocusModeEntity.TYPE_GYM, mode.modeType)
        assertFalse(mode.isActive)
        assertEquals(listOf("com.spotify.music"), mode.getAllowedPackagesList())
    }

    @Test
    fun `createGymMode defaults to the music app whitelist`() {
        val list = FocusModeEntity.createGymMode().getAllowedPackagesList()
        assertEquals(FocusModeEntity.DEFAULT_MUSIC_APPS, list)
    }

    @Test
    fun `createMeditationMode includes both music and meditation apps`() {
        val list = FocusModeEntity.createMeditationMode().getAllowedPackagesList()
        assertTrue(list.contains("com.spotify.music"))      // music app
        assertTrue(list.contains("com.calm.android"))       // meditation app
    }

    @Test
    fun `createMeditationMode de-duplicates overlapping packages`() {
        // A package present in both lists must appear exactly once.
        val list = FocusModeEntity.createMeditationMode(
            allowedPackages = listOf("com.spotify.music", "com.spotify.music", "com.calm.android")
        ).getAllowedPackagesList()
        assertEquals(1, list.count { it == "com.spotify.music" })
        assertEquals(list.size, list.distinct().size)
    }

    @Test
    fun `createAnalogMode is the analog type and whitelists essential system apps`() {
        val mode = FocusModeEntity.createAnalogMode()
        assertEquals(FocusModeEntity.TYPE_ANALOG, mode.modeType)
        assertTrue(mode.isAnalogMode())
        val allowed = mode.getAllowedPackagesList()
        assertTrue("dialer should stay usable", allowed.any { it.contains("dialer") })
        assertTrue("settings must stay reachable", allowed.contains("com.android.settings"))
    }
}
