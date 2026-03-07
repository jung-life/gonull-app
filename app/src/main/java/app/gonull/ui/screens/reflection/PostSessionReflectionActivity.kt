package app.gonull.ui.screens.reflection

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import app.gonull.data.local.AppDatabase
import app.gonull.data.local.entity.PostSessionReflectionEntity
import app.gonull.ui.components.PostSessionReflectionScreen
import app.gonull.ui.theme.GoNullTheme
import kotlinx.coroutines.launch

class PostSessionReflectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: run {
            finish()
            return
        }

        val sessionMinutes = intent.getIntExtra(EXTRA_SESSION_MINUTES, -1)
            .takeIf { it >= 0 }

        val database = AppDatabase.getDatabase(applicationContext)

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }

        setContent {
            GoNullTheme {
                PostSessionReflectionScreen(
                    appName = appName,
                    sessionMinutes = sessionMinutes,
                    onSubmit = { rating, text ->
                        lifecycleScope.launch {
                            database.postSessionReflectionDao().insertReflection(
                                PostSessionReflectionEntity(
                                    packageName = packageName,
                                    worthItRating = rating,
                                    reflectionText = text,
                                    sessionDurationMinutes = sessionMinutes
                                )
                            )
                            finish()
                        }
                    },
                    onSkip = { finish() }
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_SESSION_MINUTES = "session_minutes"
    }
}
