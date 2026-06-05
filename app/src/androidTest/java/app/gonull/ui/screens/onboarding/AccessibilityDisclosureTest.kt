package app.gonull.ui.screens.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.gonull.ui.theme.GoNullTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the accessibility prominent-disclosure screen. This screen is a
 * Play policy requirement (prominent disclosure + affirmative consent for the
 * AccessibilityService API), so a regression here is a compliance risk — exactly
 * the kind of thing worth pinning and running on a Firebase Test Lab matrix.
 */
class AccessibilityDisclosureTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun disclosure_namesAccessibilityServiceAndShowsBothChoices() {
        composeRule.setContent {
            GoNullTheme {
                AccessibilityDisclosurePage(onConsent = {}, onDecline = {})
            }
        }

        // Must explicitly name the Accessibility Service and offer affirmative
        // consent plus a decline path (Play's requirements).
        composeRule.onNodeWithText("ACCESSIBILITY SERVICE").assertIsDisplayed()
        composeRule.onNodeWithText("I Agree — Continue").assertIsDisplayed()
        composeRule.onNodeWithText("Not now").assertIsDisplayed()
    }

    @Test
    fun agree_firesConsentOnly() {
        var consented = false
        var declined = false
        composeRule.setContent {
            GoNullTheme {
                AccessibilityDisclosurePage(
                    onConsent = { consented = true },
                    onDecline = { declined = true }
                )
            }
        }

        composeRule.onNodeWithText("I Agree — Continue").performClick()

        assertTrue("Agree should fire onConsent", consented)
        assertFalse("Agree must not fire onDecline", declined)
    }

    @Test
    fun notNow_firesDeclineOnly() {
        var consented = false
        var declined = false
        composeRule.setContent {
            GoNullTheme {
                AccessibilityDisclosurePage(
                    onConsent = { consented = true },
                    onDecline = { declined = true }
                )
            }
        }

        composeRule.onNodeWithText("Not now").performClick()

        assertTrue("Not now should fire onDecline", declined)
        assertFalse("Not now must not fire onConsent", consented)
    }
}
