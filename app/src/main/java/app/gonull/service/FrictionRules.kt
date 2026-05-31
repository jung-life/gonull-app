package app.gonull.service

import app.gonull.util.Constants

/**
 * Pure, Context-free progressive-friction decision rules.
 *
 * Extracted from [ProgressiveFrictionManager] so the escalation logic can be
 * unit-tested without a database or the Android framework. The manager owns the
 * persistence (today's bypass count, logging); this object owns the *decisions*.
 *
 * Bypass # | Requirements                    | Wait Time
 * ---------|---------------------------------|----------
 * 1st      | Code + Math                     | None
 * 2nd      | Code + Math + Wait              | 5 minutes
 * 3rd      | Code + Math + Wait + Reflection | 15 minutes
 * 4th+     | LOCKED                          | Until tomorrow
 */
object FrictionRules {

    /** Friction level (1..LEVEL_4_LOCKED) for the next bypass given today's count. */
    fun nextLevel(bypassCountToday: Int): Int =
        minOf(bypassCountToday + 1, Constants.Friction.LEVEL_4_LOCKED)

    /** True once the app is locked for the rest of the day (4+ bypasses used). */
    fun isLockedForToday(bypassCountToday: Int): Boolean =
        bypassCountToday >= Constants.Friction.LEVEL_4_LOCKED - 1

    /** Required wait, in minutes, before a bypass at the given friction level. */
    fun waitTimeMinutes(frictionLevel: Int): Int = when (frictionLevel) {
        Constants.Friction.LEVEL_2_BYPASS -> Constants.Friction.LEVEL_2_WAIT_MINUTES
        Constants.Friction.LEVEL_3_BYPASS -> Constants.Friction.LEVEL_3_WAIT_MINUTES
        else -> 0
    }

    /** Whether a written reflection is required at the given friction level. */
    fun isReflectionRequired(frictionLevel: Int): Boolean =
        frictionLevel == Constants.Friction.LEVEL_3_BYPASS

    /** Human-readable description of what a given friction level demands. */
    fun description(frictionLevel: Int): String = when (frictionLevel) {
        Constants.Friction.LEVEL_1_BYPASS -> "Complete verification to access"
        Constants.Friction.LEVEL_2_BYPASS ->
            "Complete verification + ${Constants.Friction.LEVEL_2_WAIT_MINUTES} minute wait"
        Constants.Friction.LEVEL_3_BYPASS ->
            "Complete verification + ${Constants.Friction.LEVEL_3_WAIT_MINUTES} minute wait + reflection"
        Constants.Friction.LEVEL_4_LOCKED -> "Locked until tomorrow"
        else -> "Access denied"
    }
}
