package com.example.mobiilisovellusprojekti.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiilisovellusprojekti.data.Word
import com.example.mobiilisovellusprojekti.data.WordDatabase
import com.example.mobiilisovellusprojekti.data.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
/**
 * ViewModel class for managing the state and operations related to words.
 * This ViewModel works with a repository to interact with the underlying database.
 *
 * @param application The application context required for accessing the database.
 */
class WordViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * The Data Access Object (DAO) for interacting with the Word database.
     */
    private val dao = WordDatabase.getDatabase(application).wordDao()
    /**
     * The repository that provides data operations for words.
     */
    private val repository = WordRepository(dao)
    /**
     * A StateFlow that provides a list of all words stored in the database.
     * Automatically updates when the underlying data changes.
     */
    val allWords: StateFlow<List<Word>> = repository.allWordsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    /**
     * A private MutableStateFlow to hold a randomly selected word.
     */
    private val _randomWord = MutableStateFlow<Word?>(null)
    /**
     * Publicly exposed StateFlow for observing the randomly selected word.
     */
    val randomWord: StateFlow<Word?> = _randomWord.asStateFlow()
    /**
     * Fetches a random word from the database and updates the [randomWord] StateFlow.
     * This function runs on the IO dispatcher.
     */
    fun getRandomWord() {
        viewModelScope.launch(Dispatchers.IO) {
            _randomWord.value = repository.getRandomWord()
        }
    }
    /**
     * Inserts a new word into the database.
     * This function runs on the IO dispatcher.
     *
     * @param word The [Word] object to insert into the database.
     */

    fun insert(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(word)
        }
    }
    /**
     * Deletes a word from the database.
     * This function runs on the IO dispatcher.
     *
     * @param word The [Word] object to delete from the database.
     */
    fun delete(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(word)
        }
    }
}
