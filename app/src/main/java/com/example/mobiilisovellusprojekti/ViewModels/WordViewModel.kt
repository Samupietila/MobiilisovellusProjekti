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

class WordViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = WordDatabase.getDatabase(application).wordDao()
    private val repository = WordRepository(dao)

    val allWords: StateFlow<List<Word>> = repository.allWordsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _randomWord = MutableStateFlow<Word?>(null)
    val randomWord: StateFlow<Word?> = _randomWord.asStateFlow()

    fun getRandomWord() {
        viewModelScope.launch(Dispatchers.IO) {
            _randomWord.value = repository.getRandomWord()
        }
    }

    fun insert(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(word)
        }
    }

    fun delete(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(word)
        }
    }
}
