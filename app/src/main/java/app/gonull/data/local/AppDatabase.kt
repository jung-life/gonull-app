package app.gonull.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.gonull.data.local.dao.*
import app.gonull.data.local.entity.*

@Database(
    entities = [
        BlockedAppEntity::class,
        UnlockRequestEntity::class,
        UsageLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun unlockRequestDao(): UnlockRequestDao
    abstract fun usageLogDao(): UsageLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gonull_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
