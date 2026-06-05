package app.gonull.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gonull.data.local.dao.BlockedAppDao
import app.gonull.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * DAO-level tests for the blocked-apps table, including the delete path the
 * service uses to purge never-blockable packages (keyboards, our own app).
 */
@RunWith(AndroidJUnit4::class)
class BlockedAppDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BlockedAppDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.blockedAppDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun deleteByPackage_removesEntry() = runBlocking {
        // Simulate a keyboard that was wrongly saved before the never-blockable fix.
        dao.insertBlockedApp(BlockedAppEntity("com.samsung.android.honeyboard", "Keyboard"))
        assertTrue(dao.isAppBlocked("com.samsung.android.honeyboard"))

        dao.deleteBlockedAppByPackage("com.samsung.android.honeyboard")

        assertFalse(dao.isAppBlocked("com.samsung.android.honeyboard"))
        assertTrue(dao.getActiveBlockedApps().first().isEmpty())
    }

    @Test
    fun setActive_false_hidesFromActiveList() = runBlocking {
        dao.insertBlockedApp(BlockedAppEntity("com.instagram.android", "Instagram"))
        assertEquals(1, dao.getActiveBlockedApps().first().size)

        dao.setAppActive("com.instagram.android", false)
        assertEquals(0, dao.getActiveBlockedApps().first().size)

        dao.setAppActive("com.instagram.android", true)
        assertEquals(1, dao.getActiveBlockedApps().first().size)
    }

    @Test
    fun insert_replacesOnConflict() = runBlocking {
        dao.insertBlockedApp(BlockedAppEntity("com.x.android", "X", unlockDelayMinutes = 15))
        dao.insertBlockedApp(BlockedAppEntity("com.x.android", "X", unlockDelayMinutes = 60))

        val apps = dao.getActiveBlockedApps().first()
        assertEquals(1, apps.size)
        assertEquals(60, apps[0].unlockDelayMinutes)
    }
}
