package app.gonull.ui.screens.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.ui.theme.*
import app.gonull.util.PermissionHelper

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }

    var accessibilityEnabled by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(false) }

    // Check permissions periodically
    LaunchedEffect(Unit) {
        while (true) {
            accessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context)
            overlayEnabled = PermissionHelper.canDrawOverlays(context)

            if (accessibilityEnabled && overlayEnabled) {
                onComplete()
                break
            }

            kotlinx.coroutines.delay(1000)
        }
    }

    when (currentPage) {
        0 -> WelcomePage(onNext = { currentPage = 1 })
        1 -> AccessibilityDisclosurePage(onNext = { currentPage = 2 })
        2 -> PermissionsPage(
            accessibilityEnabled = accessibilityEnabled,
            overlayEnabled = overlayEnabled,
            onBack = { currentPage = 1 }
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Text(
            text = "Ø",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 96.sp,
                color = GoNullGreen
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "GoNull",
            style = MaterialTheme.typography.headlineLarge,
            color = GoNullWhite,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Break free from digital addiction",
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Unlike other app blockers that rely on willpower, GoNull creates meaningful friction through commitment contracts.",
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AccessibilityDisclosurePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "How GoNull Works",
            style = MaterialTheme.typography.headlineMedium,
            color = GoNullWhite,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Accessibility Service Disclosure
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GoNullSurface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "⚡ Accessibility Service",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoNullGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "GoNull uses Android's Accessibility Service to help users with:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                BulletPoint("ADHD and executive function challenges")
                BulletPoint("Digital addiction and impulse control disorders")
                BulletPoint("Building healthier screen time habits")

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = GoNullBorder)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "What we detect:",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullWhite,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                BulletPoint("When you open an app you've chosen to block")
                BulletPoint("The package name of the app (e.g., \"com.instagram.android\")")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "What we DON'T collect:",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullWhite,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                BulletPoint("Your screen content")
                BulletPoint("Your passwords or personal data")
                BulletPoint("Your browsing history")
                BulletPoint("Any data sent to external servers")

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = GoNullGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔒 Privacy Guarantee",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoNullGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All detection happens locally on your device. No data is collected, stored on servers, or shared with third parties.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoNullWhite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Why this helps section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = GoNullSurface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🧠 Designed for Neurodivergent Users",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoNullWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "For users with ADHD, autism, or impulse control challenges, simply \"deciding\" to avoid apps doesn't work. GoNull creates external structure that supports self-regulation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "By detecting app launches and creating friction (time delays, accountability), GoNull acts as a \"pause button\" for your brain.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoNullGray
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoNullGreen,
                contentColor = GoNullBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I Understand - Continue", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsPage(
    accessibilityEnabled: Boolean,
    overlayEnabled: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Grant Permissions",
            style = MaterialTheme.typography.headlineMedium,
            color = GoNullWhite,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tap each permission below to enable",
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullGray,
            textAlign = TextAlign.Center
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
                description = "Required to detect when blocked apps open",
                requirement = "REQUIRED",
                isGranted = accessibilityEnabled,
                onClick = { PermissionHelper.openAccessibilitySettings(context) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                title = "Display Over Other Apps",
                description = "Required to show the blocking screen",
                requirement = "REQUIRED",
                isGranted = overlayEnabled,
                onClick = { PermissionHelper.openOverlaySettings(context) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                title = "Developer Settings",
                description = "On some devices, you may need to enable 'Allow screen overlays' in Developer Settings for the blocking screen to work properly",
                requirement = "IF NEEDED",
                isGranted = false,
                onClick = { PermissionHelper.openDeveloperSettings(context) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (accessibilityEnabled && overlayEnabled) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = GoNullGreen.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = GoNullGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "All permissions granted! Starting GoNull...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoNullWhite
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        TextButton(onClick = onBack) {
            Text("← Back", color = GoNullGray)
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
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GoNullWhite,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (requirement == "REQUIRED") GoNullRed.copy(alpha = 0.2f) else GoNullYellow.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = requirement,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (requirement == "REQUIRED") GoNullRed else GoNullYellow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GoNullGray,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isGranted) "Granted" else "Not granted",
                tint = if (isGranted) GoNullGreen else GoNullRed,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
