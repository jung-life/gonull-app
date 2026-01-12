package app.gonull

import android.app.Application
import app.gonull.data.local.AppDatabase

class GoNullApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
    }
}
