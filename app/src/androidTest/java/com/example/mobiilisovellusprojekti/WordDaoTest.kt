package com.example.mobiilisovellusprojekti.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WordDaoTest {

    private lateinit var db: WordDatabase
    private lateinit var dao: WordDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, WordDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.wordDao()
    }

    @After
    fun tearDown() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    @Test
    fun insertAndRetrieveWord() = runBlocking {
        val testWord = Word(word = "omena", difficulty = 2)
        dao.insert(testWord)
        val result = dao.getAllFlow().first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("omena", result[0].word)
        Assert.assertEquals(2, result[0].difficulty)
    }
}
