package app.gonull.ui.screens.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.R
import app.gonull.ui.components.VideoPlayer
import app.gonull.ui.theme.*
import app.gonull.util.Constants
import app.gonull.util.PermissionHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }

    when (currentPage) {
        0 -> VideoIntroPage(
            onVideoEnd = { currentPage = 1 },
            onSkip = { currentPage = 1 }
        )
        1 -> WelcomePage(onNext = { currentPage = 2 })
        2 -> AccessibilityDisclosurePage(
            onConsent = { currentPage = 3 },
            // Decline: return to the previous screen without enabling the service.
            // The user can advance again to re-trigger this disclosure.
            onDecline = { currentPage = 1 }
        )
        3 -> PermissionsPage(
            onBack = { currentPage = 2 },
            onContinue = onComplete
        )
    }
}

@Composable
fun VideoIntroPage(
    onVideoEnd: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .clickable(onClick = onSkip)
    ) {
        VideoPlayer(
            videoResId = R.raw.onboarding,
            modifier = Modifier.fillMaxSize(),
            onVideoEnd = onVideoEnd
        )

        // Skip button in top right
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Skip",
                color = GoNullWhite.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Tap to continue hint at bottom
        Text(
            text = "Tap anywhere to continue",
            color = GoNullWhite.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        Text(
            text = "IN THE BUNKER",
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 4.sp
            ),
            color = GoNullGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "BREAK THE LOOP",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp
            ),
            color = GoNullWhite,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Main content - scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // The Problem
            Text(
                text = "THE PROBLEM",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp
                ),
                color = GoNullGreen,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "You tell yourself \"just 5 minutes.\" Two hours later, you're still scrolling. You feel drained. You hate yourself for wasting time.",
                style = MaterialTheme.typography.bodyLarge,
                color = GoNullWhite,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // The Science
            Text(
                text = "THE SCIENCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp
                ),
                color = GoNullGreen,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "These apps use variable reward schedules—the same psychology as slot machines. Your rational brain needs 20+ seconds to override impulse. By then, you've already unlocked your phone.",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // The Truth
            Text(
                text = "THE TRUTH",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp
                ),
                color = GoNullGreen,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Willpower is bringing a knife to a gunfight. You can't out-discipline a system built by teams of engineers whose job is to keep you engaged. You need external barriers, not internal strength.",
                style = MaterialTheme.typography.bodyMedium,
                color = GoNullGray,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Divider line
            Divider(color = GoNullBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(32.dp))

            // The Solution
            Text(
                text = "That's not weakness. That's strategy.",
                style = MaterialTheme.typography.titleLarge,
                color = GoNullWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // CTA Button
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enter the Bunker", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AccessibilityDisclosurePage(
    onConsent: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text(
            text = "THE PROTOCOL",
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 4.sp
            ),
            color = GoNullGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "How Recovery Works",
            style = MaterialTheme.typography.headlineMedium,
            color = GoNullWhite,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Prominent disclosure: shown first so users see the Accessibility
            // Service explanation before reaching the permission-enable step.
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = GoNullSurface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACCESSIBILITY SERVICE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp
                        ),
                        color = GoNullGreen,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "GoNull uses Android's Accessibility Service to detect when you open an " +
                            "app you've chosen to block, so it can show the blocking screen. That is " +
                            "the only thing GoNull does with this permission.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullWhite,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "It does not read the content of your screen, what you type, your " +
                            "passwords, or any other app. No personal or sensitive data is collected " +
                            "or shared — everything stays on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoNullGray,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The 20-Second Rule
            ProtocolCard(
                number = "01",
                title = "The 20-Second Rule",
                description = "Your rational brain needs 20+ seconds to override impulse. Hard friction buys that time."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dopamine Deficit
            ProtocolCard(
                number = "02",
                title = "The Deficit State",
                description = "Endless scrolling downregulates your dopamine receptors. The only fix is letting your brain recalibrate."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 30-Day Reset
            ProtocolCard(
                number = "03",
                title = "The 30-Day Reset",
                description = "It takes ~30 days for receptors to reset. Slipping doesn't erase progress—it just pauses recovery."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Affirmative-consent statement (required for the Accessibility disclosure).
        Text(
            text = "By tapping \"I Agree\", you consent to GoNull using the Accessibility " +
                "Service for the purpose described above.",
            style = MaterialTheme.typography.bodySmall,
            color = GoNullGray,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Consent: affirmative action that advances to enabling the service.
        Button(
            onClick = onConsent,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I Agree — Continue", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Decline: user does not consent; blocking is not enabled.
        TextButton(
            onClick = onDecline,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Not now",
                color = GoNullGray,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrivacyPolicyLink()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PrivacyPolicyLink() {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.bodySmall,
            color = GoNullGray,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PRIVACY_POLICY_URL))
                )
            }
        )
        Text(
            text = "  •  ",
            style = MaterialTheme.typography.bodySmall,
            color = GoNullGray
        )
        Text(
            text = "Terms of Service",
            style = MaterialTheme.typography.bodySmall,
            color = GoNullGray,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TERMS_OF_SERVICE_URL))
                )
            }
        )
    }
}

@Composable
fun ProtocolCard(
    number: String,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = GoNullSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Number badge
            Text(
                text = number,
                style = MaterialTheme.typography.headlineMedium,
                color = GoNullGreen.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGreen
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PermissionsPage(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current

    // Check permissions internally — PermissionsPage is solely responsible for
    // this flow. No parent auto-navigation.
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(false) }
    var usageStatsEnabled by remember { mutableStateOf(false) }

    // Periodically refresh permission state
    LaunchedEffect(Unit) {
        while (true) {
            accessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)
            overlayEnabled = PermissionHelper.canDrawOverlays(context)
            usageStatsEnabled = PermissionHelper.hasUsageStatsPermission(context)
            kotlinx.coroutines.delay(1000)
        }
    }

    // POST_NOTIFICATIONS is a runtime permission on Android 13+. Without it, the
    // unlock-timer countdown and session-expiry warnings are silently hidden.
    val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val notificationPermissionState =
        if (needsNotificationPermission) rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) else null
    val notificationsGranted = notificationPermissionState?.status?.isGranted ?: true

    // Accessibility, Usage Stats, and Notifications are required.
    // Display Over Apps is optional — if unavailable (OEM-disabled on some Vivo/Oppo
    // devices), we'll fall back to UsageStatsBased blocking with a 2-second lag.
    val allRequiredGranted =
        accessibilityEnabled && usageStatsEnabled && notificationsGranted
    val missingCount =
        listOf(accessibilityEnabled, usageStatsEnabled, notificationsGranted)
            .count { !it }

    // Don't auto-navigate. The user must tap "Let's Start Our Journey" to proceed
    // (to the scanning screen), which prevents the jarring "rush" to the next screen.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Text(
            text = "SETUP",
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 4.sp
            ),
            color = GoNullGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Grant Access",
            style = MaterialTheme.typography.headlineMedium,
            color = GoNullWhite,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap each permission to enable",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Scrollable permission cards
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            PermissionCard(
                title = "Accessibility Service",
                description = "Detects when you open blocked apps",
                requirement = "REQUIRED",
                isGranted = accessibilityEnabled,
                onClick = { PermissionHelper.openAccessibilitySettings(context) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                title = "Display Over Apps",
                description = "Shows the blocking screen instantly. On some devices this is unavailable — we'll use a slower fallback instead.",
                requirement = "OPTIONAL",
                isGranted = overlayEnabled,
                onClick = { PermissionHelper.openOverlaySettings(context) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                title = "Usage Stats",
                description = "Shows your app usage data",
                requirement = "REQUIRED",
                isGranted = usageStatsEnabled,
                onClick = { PermissionHelper.openUsageStatsSettings(context) }
            )

            if (needsNotificationPermission) {
                Spacer(modifier = Modifier.height(12.dp))

                PermissionCard(
                    title = "Notifications",
                    description = "Shows unlock timers and session warnings",
                    requirement = "REQUIRED",
                    isGranted = notificationsGranted,
                    onClick = { notificationPermissionState?.launchPermissionRequest() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                title = "Developer Settings",
                description = "Some devices need this for overlays to work",
                requirement = "OPTIONAL",
                isGranted = false,
                onClick = { PermissionHelper.openDeveloperSettings(context) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Continue button. When permissions are still missing, this acts as a
        // shortcut that opens the first ungranted permission. When all are granted,
        // user must click this button explicitly to proceed (no auto-navigation).
        Button(
            onClick = {
                when {
                    // Proceed to the scanning screen, which covers the app-data
                    // preload with its animation — so no spinner needed here.
                    allRequiredGranted -> onContinue()
                    !accessibilityEnabled -> PermissionHelper.openAccessibilitySettings(context)
                    !usageStatsEnabled -> PermissionHelper.openUsageStatsSettings(context)
                    !notificationsGranted -> notificationPermissionState?.launchPermissionRequest()
                    // Overlay is optional — if user doesn't grant it, we use UsageStatsBased fallback
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullGreen,
                contentColor = GoNullBlack,
                disabledContainerColor = GoNullSurface,
                disabledContentColor = GoNullGray
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (allRequiredGranted) {
                    "Let's Start Our Journey"
                } else {
                    "Set Up Next Permission ($missingCount left)"
                },
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back", color = GoNullGray)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCard(
    title: String,
    description: String,
    requirement: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val isRequired = requirement == "REQUIRED"
    val badgeColor = when {
        isGranted -> GoNullGreen
        isRequired -> GoNullYellow
        else -> GoNullGray
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) GoNullGreen.copy(alpha = 0.1f) else GoNullSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isGranted) GoNullGreen else GoNullWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (isGranted) "DONE" else requirement,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isGranted) "Granted" else "Not granted",
                tint = if (isGranted) GoNullGreen else if (isRequired) GoNullYellow else GoNullGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
