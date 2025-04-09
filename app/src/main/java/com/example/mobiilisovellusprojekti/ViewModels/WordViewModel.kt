package com.example.mobiilisovellusprojekti.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiilisovellusprojekti.data.Word
import com.example.mobiilisovellusprojekti.data.WordDatabase
import com.example.mobiilisovellusprojekti.data.WordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class WordViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = WordDatabase.getDatabase(application).wordDao()
    private val repository = WordRepository(dao)

    val allWords: StateFlow<List<Word>> = repository.allWordsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
