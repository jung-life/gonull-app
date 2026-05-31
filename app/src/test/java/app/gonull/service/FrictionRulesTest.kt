package app.gonull.service

import app.gonull.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the progressive-friction escalation rules. These are the
 * decisions that make a bypass progressively harder over a day, so regressions
 * here directly weaken the core value of the app.
 */
class FrictionRulesTest {

    // --- nextLevel ---------------------------------------------------------

    @Test
    fun `first bypass of the day is level 1`() {
        assertEquals(Constants.Friction.LEVEL_1_BYPASS, FrictionRules.nextLevel(0))
    }

    @Test
    fun `level escalates one step per recorded bypass`() {
        assertEquals(Constants.Friction.LEVEL_2_BYPASS, FrictionRules.nextLevel(1))
        assertEquals(Constants.Friction.LEVEL_3_BYPASS, FrictionRules.nextLevel(2))
        assertEquals(Constants.Friction.LEVEL_4_LOCKED, FrictionRules.nextLevel(3))
    }

    @Test
    fun `level is clamped at LOCKED and never exceeds it`() {
        assertEquals(Constants.Friction.LEVEL_4_LOCKED, FrictionRules.nextLevel(4))
        assertEquals(Constants.Friction.LEVEL_4_LOCKED, FrictionRules.nextLevel(10))
        assertEquals(Constants.Friction.LEVEL_4_LOCKED, FrictionRules.nextLevel(999))
    }

    // --- isLockedForToday --------------------------------------------------

    @Test
    fun `not locked before the third bypass is used`() {
        assertFalse(FrictionRules.isLockedForToday(0))
        assertFalse(FrictionRules.isLockedForToday(1))
        assertFalse(FrictionRules.isLockedForToday(2))
    }

    @Test
    fun `locked once three bypasses have been used`() {
        // After 3 bypasses the next attempt would be level 4 (LOCKED).
        assertTrue(FrictionRules.isLockedForToday(3))
        assertTrue(FrictionRules.isLockedForToday(4))
    }

    @Test
    fun `lock threshold agrees with the locked friction level`() {
        // The first count that locks must be the same one that maps to LEVEL_4.
        val firstLockedCount = (0..10).first { FrictionRules.isLockedForToday(it) }
        assertEquals(
            Constants.Friction.LEVEL_4_LOCKED,
            FrictionRules.nextLevel(firstLockedCount)
        )
    }

    // --- waitTimeMinutes ---------------------------------------------------

    @Test
    fun `level 1 requires no wait`() {
        assertEquals(0, FrictionRules.waitTimeMinutes(Constants.Friction.LEVEL_1_BYPASS))
    }

    @Test
    fun `levels 2 and 3 require their configured waits`() {
        assertEquals(
            Constants.Friction.LEVEL_2_WAIT_MINUTES,
            FrictionRules.waitTimeMinutes(Constants.Friction.LEVEL_2_BYPASS)
        )
        assertEquals(
            Constants.Friction.LEVEL_3_WAIT_MINUTES,
            FrictionRules.waitTimeMinutes(Constants.Friction.LEVEL_3_BYPASS)
        )
    }

    @Test
    fun `wait time increases with friction level`() {
        val l2 = FrictionRules.waitTimeMinutes(Constants.Friction.LEVEL_2_BYPASS)
        val l3 = FrictionRules.waitTimeMinutes(Constants.Friction.LEVEL_3_BYPASS)
        assertTrue("level 3 should wait longer than level 2", l3 > l2)
    }

    @Test
    fun `locked level reports no wait`() {
        // Level 4 is a hard lock, not a wait — the UI gates it differently.
        assertEquals(0, FrictionRules.waitTimeMinutes(Constants.Friction.LEVEL_4_LOCKED))
    }

    // --- isReflectionRequired ---------------------------------------------

    @Test
    fun `reflection is required only at level 3`() {
        assertFalse(FrictionRules.isReflectionRequired(Constants.Friction.LEVEL_1_BYPASS))
        assertFalse(FrictionRules.isReflectionRequired(Constants.Friction.LEVEL_2_BYPASS))
        assertTrue(FrictionRules.isReflectionRequired(Constants.Friction.LEVEL_3_BYPASS))
        assertFalse(FrictionRules.isReflectionRequired(Constants.Friction.LEVEL_4_LOCKED))
    }

    // --- description -------------------------------------------------------

    @Test
    fun `every defined level has a non-blank description`() {
        listOf(
            Constants.Friction.LEVEL_1_BYPASS,
            Constants.Friction.LEVEL_2_BYPASS,
            Constants.Friction.LEVEL_3_BYPASS,
            Constants.Friction.LEVEL_4_LOCKED
        ).forEach { level ->
            assertTrue(
                "level $level should have a description",
                FrictionRules.description(level).isNotBlank()
            )
        }
    }

    @Test
    fun `locked description communicates the lock`() {
        assertEquals("Locked until tomorrow", FrictionRules.description(Constants.Friction.LEVEL_4_LOCKED))
    }
}
