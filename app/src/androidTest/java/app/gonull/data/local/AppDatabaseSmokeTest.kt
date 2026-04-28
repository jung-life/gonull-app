package app.gonull.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gonull.data.local.entity.BlockedAppEntity
import app.gonull.data.local.entity.UnlockRequestEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Opens the DB at the current schema version and exercises the DAOs that the
 * blocking hot path depends on. Catches entity/DAO mismatches that the unit test
 * suite cannot, since Room's generated impls only show up at instrumented runtime.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseSmokeTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun blockedAppRoundTrip() = runBlocking {
        val app = BlockedAppEntity(
            packageName = "com.example.test",
            appName = "Test App",
            unlockDelayMinutes = 30
        )
        db.blockedAppDao().insertBlockedApp(app)

        val active = db.blockedAppDao().getActiveBlockedApps().first()
        assertEquals(1, active.size)
        assertEquals("com.example.test", active[0].packageName)
        assertTrue(db.blockedAppDao().isAppBlocked("com.example.test"))
    }

    @Test
    fun unlockRequestRoundTrip() = runBlocking {
        val now = System.currentTimeMillis()
        val request = UnlockRequestEntity(
            packageName = "com.example.test",
            requestedAt = now,
            unlocksAt = now + 30 * 60 * 1000L,
            status = "PENDING"
        )
        db.unlockRequestDao().insertRequest(request)

        val pending = db.unlockRequestDao().getPendingRequest("com.example.test")
        assertNotNull(pending)
        assertEquals("PENDING", pending!!.status)
    }

    /**
     * Touch every DAO's accessor on the database — proves the @Database wiring
     * compiles for every entity/DAO pair without invoking each query.
     */
    @Test
    fun allDaosAreReachable() {
        assertNotNull(db.blockedAppDao())
        assertNotNull(db.unlockRequestDao())
        assertNotNull(db.usageLogDao())
        assertNotNull(db.dailyBypassCountDao())
        assertNotNull(db.dailyUsageDao())
        assertNotNull(db.usageSessionDao())
        assertNotNull(db.accountabilityPartnerDao())
        assertNotNull(db.partnerNotificationLogDao())
        assertNotNull(db.streakDao())
        assertNotNull(db.focusModeDao())
        assertNotNull(db.questLogDao())
        assertNotNull(db.boredomSessionDao())
        assertNotNull(db.journalEntryDao())
        assertNotNull(db.triggerLogDao())
        assertNotNull(db.postSessionReflectionDao())
        assertNotNull(db.implementationIntentionDao())
        assertNotNull(db.dailyCommitmentDao())
    }
}
