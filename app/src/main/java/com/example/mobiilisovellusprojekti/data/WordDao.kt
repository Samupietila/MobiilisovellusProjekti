package com.example.mobiilisovellusprojekti.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: Word)

    @Delete
    suspend fun delete(word: Word)

    @Query("SELECT * FROM words")
    fun getAllFlow(): Flow<List<Word>>
}
