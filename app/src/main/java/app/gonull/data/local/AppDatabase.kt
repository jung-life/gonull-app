package app.gonull.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.gonull.data.local.dao.*
import app.gonull.data.local.entity.*

@Database(
    entities = [
        BlockedAppEntity::class,
        UnlockRequestEntity::class,
        UsageLogEntity::class,
        DailyBypassCountEntity::class,
        DailyUsageEntity::class,
        UsageSessionEntity::class,
        AccountabilityPartnerEntity::class,
        PartnerNotificationLogEntity::class,
        StreakEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun unlockRequestDao(): UnlockRequestDao
    abstract fun usageLogDao(): UsageLogDao
    abstract fun dailyBypassCountDao(): DailyBypassCountDao
    abstract fun dailyUsageDao(): DailyUsageDao
    abstract fun usageSessionDao(): UsageSessionDao
    abstract fun accountabilityPartnerDao(): AccountabilityPartnerDao
    abstract fun partnerNotificationLogDao(): PartnerNotificationLogDao
    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to blocked_apps table
                db.execSQL("ALTER TABLE blocked_apps ADD COLUMN budgetMinutes INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE blocked_apps ADD COLUMN budgetEnabled INTEGER NOT NULL DEFAULT 0")

                // Add new column to usage_logs table
                db.execSQL("ALTER TABLE usage_logs ADD COLUMN reflectionText TEXT DEFAULT NULL")

                // Create daily_bypass_counts table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_bypass_counts (
                        packageName TEXT NOT NULL,
                        date TEXT NOT NULL,
                        bypassCount INTEGER NOT NULL DEFAULT 0,
                        lastBypassAt INTEGER NOT NULL,
                        PRIMARY KEY(packageName, date)
                    )
                """)

                // Create daily_usage table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_usage (
                        packageName TEXT NOT NULL,
                        date TEXT NOT NULL,
                        usedMinutes INTEGER NOT NULL DEFAULT 0,
                        lastUpdatedAt INTEGER NOT NULL,
                        PRIMARY KEY(packageName, date)
                    )
                """)

                // Create usage_sessions table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS usage_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        packageName TEXT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        endedAt INTEGER,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // Create accountability_partners table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS accountability_partners (
                        id INTEGER PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        phoneNumber TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL
                    )
                """)

                // Create partner_notification_logs table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS partner_notification_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        partnerPhone TEXT NOT NULL,
                        notificationType TEXT NOT NULL,
                        packageName TEXT,
                        message TEXT NOT NULL,
                        sentAt INTEGER NOT NULL,
                        wasSuccessful INTEGER NOT NULL DEFAULT 1
                    )
                """)
            }
        }

        // Migration from version 2 to 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create streaks table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS streaks (
                        packageName TEXT PRIMARY KEY NOT NULL,
                        currentStreak INTEGER NOT NULL DEFAULT 0,
                        longestStreak INTEGER NOT NULL DEFAULT 0,
                        lastUpdatedDate TEXT NOT NULL,
                        streakStartDate TEXT NOT NULL,
                        totalDaysTracked INTEGER NOT NULL DEFAULT 0,
                        totalCleanDays INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gonull_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
