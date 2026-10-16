package com.teamsalon.booking

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.teamsalon.booking.data.AppDatabase
import com.teamsalon.booking.data.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Person 3's instrumented test — Room needs a real SQLite engine, so this
 * runs on an emulator/device rather than as a plain JVM test. Uses an
 * in-memory database so it never touches real app data.
 */
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun loginWithCorrectCredentialsReturnsUser() = runBlocking {
        db.userDao().insertAll(listOf(UserEntity(fullName = "Test User", email = "test@demo.com", password = "pass123")))

        val result = db.userDao().login("test@demo.com", "pass123")

        assertNotNull(result)
        assertEquals("Test User", result?.fullName)
    }

    @Test
    fun loginWithWrongPasswordReturnsNull() = runBlocking {
        db.userDao().insertAll(listOf(UserEntity(fullName = "Test User", email = "test@demo.com", password = "pass123")))

        val result = db.userDao().login("test@demo.com", "wrongpass")

        assertNull(result)
    }

    @Test
    fun loginWithUnknownEmailReturnsNull() = runBlocking {
        val result = db.userDao().login("nobody@demo.com", "anything")

        assertNull(result)
    }
}
