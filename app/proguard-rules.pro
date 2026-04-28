# Preserve source/line info so production stack traces are mappable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room — keep entities (used reflectively by generated Dao impls and migrations).
-keep class app.gonull.data.local.entity.** { *; }
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# Service classes are referenced from manifest + reflection (Accessibility / FGS bind paths).
-keep class app.gonull.service.** { *; }
-keep class app.gonull.receiver.** { *; }

# Activities the OS launches by name.
-keep class app.gonull.ui.MainActivity
-keep class app.gonull.ui.screens.blocking.BlockingOverlayActivity
-keep class app.gonull.ui.screens.warning.AccessWarningActivity
-keep class app.gonull.ui.screens.reflection.PostSessionReflectionActivity

# Kotlin coroutines internals.
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}

# Media3 / ExoPlayer.
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Accompanist.
-dontwarn com.google.accompanist.**
