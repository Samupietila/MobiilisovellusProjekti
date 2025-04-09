package com.example.mobiilisovellusprojekti.data

class WordRepository(private val dao: WordDao) {
    val allWordsFlow = dao.getAllFlow()

    suspend fun insert(word: Word) = dao.insert(word)

    suspend fun delete(word: Word) = dao.delete(word)
}
