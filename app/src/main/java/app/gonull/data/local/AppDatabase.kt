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
        StreakEntity::class,
        FocusModeEntity::class,
        QuestLogEntity::class,
        BoredomSessionEntity::class,
        JournalEntryEntity::class,
        TriggerLogEntity::class,
        PostSessionReflectionEntity::class,
        ImplementationIntentionEntity::class,
        DailyCommitmentEntity::class
    ],
    version = 8,
    exportSchema = true
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
    abstract fun focusModeDao(): FocusModeDao
    abstract fun questLogDao(): QuestLogDao
    abstract fun boredomSessionDao(): BoredomSessionDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun triggerLogDao(): TriggerLogDao
    abstract fun postSessionReflectionDao(): PostSessionReflectionDao
    abstract fun implementationIntentionDao(): ImplementationIntentionDao
    abstract fun dailyCommitmentDao(): DailyCommitmentDao

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

        // Migration from version 3 to 4
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create focus_modes table for Gym Mode and Meditation Mode
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS focus_modes (
                        modeType TEXT PRIMARY KEY NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 0,
                        activatedAt INTEGER DEFAULT NULL,
                        expiresAt INTEGER DEFAULT NULL,
                        allowedPackages TEXT NOT NULL DEFAULT ''
                    )
                """)
            }
        }

        // Migration from version 4 to 5: Rename YOGA to MEDITATION
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE focus_modes SET modeType = 'MEDITATION' WHERE modeType = 'YOGA'")
            }
        }

        // Migration from version 6 to 7: Add pendingUnblockAt to blocked_apps
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE blocked_apps ADD COLUMN pendingUnblockAt INTEGER DEFAULT NULL")
            }
        }

        // Migration from version 7 to 8: Add rehab technique tables
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS trigger_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        packageName TEXT NOT NULL,
                        `trigger` TEXT NOT NULL,
                        cravingIntensity INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS post_session_reflections (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        packageName TEXT NOT NULL,
                        worthItRating INTEGER NOT NULL,
                        reflectionText TEXT,
                        sessionDurationMinutes INTEGER,
                        timestamp INTEGER NOT NULL
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS implementation_intentions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        packageName TEXT,
                        thenAction TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_commitments (
                        date TEXT PRIMARY KEY NOT NULL,
                        commitmentText TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }

        // Migration from version 5 to 6: Add quest_log, boredom_sessions, journal_entries
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS quest_log (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        questId TEXT NOT NULL,
                        questTitle TEXT NOT NULL,
                        questCategory TEXT NOT NULL,
                        completedAt INTEGER NOT NULL,
                        triggeredByPackage TEXT
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS boredom_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        completedAt INTEGER,
                        durationSeconds INTEGER NOT NULL DEFAULT 0,
                        wasCompleted INTEGER NOT NULL DEFAULT 0,
                        triggeredByPackage TEXT,
                        context TEXT
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS journal_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        entryText TEXT NOT NULL,
                        linkedFocusMode TEXT,
                        focusDurationMinutes INTEGER,
                        mood TEXT
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
