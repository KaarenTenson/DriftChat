package com.app.driftchat

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.driftchat.data.AppDatabase
import com.app.driftchat.data.UserDataDao
import com.app.driftchat.domainmodel.Gender
import com.app.driftchat.domainmodel.UserData
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class UserDataDaoTest {

    private lateinit var userDataDao: UserDataDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDataDao = db.userDataDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() = runTest {
        // 1. ARRANGE
        val user = UserData(
            id = 1,
            name = "Tristan",
            hobbies = setOf("Coding", "Gaming"),
            description = "A test user.",
            gender = Gender.MALE,
            quote = "Test quote"
        )

        userDataDao.insertUserData(user)
        val retrievedUser = userDataDao.getUserDataById(1).first()

        assertNotNull(retrievedUser)
        assertEquals(user.name, retrievedUser!!.name)
        assertEquals(user.gender, retrievedUser.gender)
        assertEquals(user.hobbies, retrievedUser.hobbies)
    }
}