package app.gonull.ui.screens.scanning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gonull.data.AppDataCache
import app.gonull.ui.theme.*
import app.gonull.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A brief "scanning your phone for the usual suspects" screen shown after
 * onboarding. It runs a REAL scan of installed apps for known attention-traps
 * (social + entertainment), reveals them one by one over a radar animation,
 * then proceeds. Doubles as the cover for the app-data preload.
 */
@Composable
fun ScanningScreen(onComplete: () -> Unit) {
    val context = LocalContext.current

    var detectedApps by remember { mutableStateOf<List<String>>(emptyList()) }
    var revealedCount by remember { mutableIntStateOf(0) }
    var scanDone by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Scanning installed apps…") }

    LaunchedEffect(Unit) {
        // Warm the app-data cache in the background while we animate.
        launch(Dispatchers.IO) { runCatching { AppDataCache.preload(context) } }

        // The usual suspects: social + entertainment packages (same keywords the
        // app uses elsewhere to categorize/recommend blocks).
        val suspectKeywords = listOf(
            "facebook", "instagram", "twitter", "x.android", "tiktok", "snapchat",
            "linkedin", "reddit", "whatsapp", "telegram", "messenger",
            "youtube", "netflix", "disney", "spotify", "twitch", "prime.video", "hulu"
        )

        val suspects = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            runCatching {
                pm.getInstalledApplications(0)
                    .filter { app ->
                        val pkg = app.packageName.lowercase()
                        !PermissionHelper.isSystemOrCriticalService(app.packageName) &&
                            suspectKeywords.any { pkg.contains(it) }
                    }
                    .mapNotNull { runCatching { pm.getApplicationLabel(it).toString() }.getOrNull() }
                    .distinct()
                    .sorted()
            }.getOrDefault(emptyList())
        }
        detectedApps = suspects

        // Dramatic minimum so the scan reads as deliberate, not a flicker.
        delay(700)

        if (suspects.isEmpty()) {
            statusText = "No usual suspects installed — nice."
            delay(1200)
            onComplete()
            return@LaunchedEffect
        }

        statusText = "Found your usual suspects"
        suspects.forEachIndexed { index, _ ->
            delay(320)
            revealedCount = index + 1
        }

        scanDone = true
        delay(1100) // let the result land
        onComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoNullBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SCANNING",
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 6.sp),
            color = GoNullGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = GoNullGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        RadarAnimation(
            suspectCount = detectedApps.size,
            revealedCount = revealedCount,
            done = scanDone,
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Revealed suspects list
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            detectedApps.take(revealedCount).forEach { appName ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInHorizontally { it / 2 }
                ) {
                    SuspectRow(appName)
                }
            }
        }

        if (scanDone) {
            Text(
                text = "${detectedApps.size} attention ${if (detectedApps.size == 1) "trap" else "traps"} found",
                style = MaterialTheme.typography.titleMedium,
                color = GoNullWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SuspectRow(appName: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GoNullRed.copy(alpha = 0.10f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "⚠", fontSize = 16.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = appName,
            style = MaterialTheme.typography.bodyLarge,
            color = GoNullWhite,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "DETECTED",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = GoNullRed,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RadarAnimation(
    suspectCount: Int,
    revealedCount: Int,
    done: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "radar")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "sweep"
    )
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringColor = if (done) GoNullGreen else GoNullGreen.copy(alpha = 0.9f)

            // Concentric static rings
            for (i in 1..3) {
                drawCircle(
                    color = ringColor.copy(alpha = 0.18f),
                    radius = r * i / 3f,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }

            // Crosshairs
            drawLine(ringColor.copy(alpha = 0.12f), Offset(center.x, 0f), Offset(center.x, size.height), 2f)
            drawLine(ringColor.copy(alpha = 0.12f), Offset(0f, center.y), Offset(size.width, center.y), 2f)

            // Expanding pulse ring
            if (!done) {
                drawCircle(
                    color = GoNullGreen.copy(alpha = (1f - pulse) * 0.5f),
                    radius = r * pulse,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }

            // Rotating sweep (a fading trailing arc + leading line)
            if (!done) {
                rotate(degrees = sweep, pivot = center) {
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(GoNullGreen, GoNullGreen.copy(alpha = 0f)),
                            startY = center.y - r,
                            endY = center.y
                        ),
                        start = center,
                        end = Offset(center.x, center.y - r),
                        strokeWidth = 4f
                    )
                }
            }
        }

        // Center readout: live count of suspects found
        Text(
            text = if (done || revealedCount > 0) "$revealedCount" else "Ø",
            style = MaterialTheme.typography.headlineLarge,
            color = if (done) GoNullGreen else GoNullWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp
        )
    }
}
