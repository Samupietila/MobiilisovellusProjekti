package com.example.mobiilisovellusprojekti.ViewModels

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.mockito.MockedStatic
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: GameViewModel
    private lateinit var logMock: MockedStatic<Log>

    @Before
    fun setUp() {
        logMock = Mockito.mockStatic(Log::class.java)
        logMock.`when`<Int> { Log.d(Mockito.anyString(), Mockito.anyString()) }.thenReturn(0)
        logMock.`when`<Int> { Log.e(Mockito.anyString(), Mockito.anyString()) }.thenReturn(0)
        viewModel = GameViewModel()
    }

    @After
    fun tearDown() {
        logMock.close()
    }

    @Test
    fun setWord_setsCorrectWord() = runTest {
        viewModel.setWord("banana")
        assertThat(viewModel.currentWord.value).isEqualTo("banana")
    }

    @Test
    fun onNewMessage_correctAnswer_setsGameOverTrue() = runTest {
        viewModel.setWord("Apple")
        viewModel.onNewMessage("apple") // case-insensitive comparison
        assertThat(viewModel.gameOver.value).isTrue()
    }

    @Test
    fun onNewMessage_wrongAnswer_setsGameOverFalse() = runTest {
        viewModel.setWord("banana")
        viewModel.onNewMessage("kiwi")
        assertThat(viewModel.gameOver.value).isFalse()
    }

    @Test
    fun resetGameState_resetsWordAndGameOver() = runTest {
        viewModel.setWord("orange")
        viewModel.setGameOver(true)

        viewModel.resetGameState()

        assertThat(viewModel.currentWord.value).isEmpty()
        assertThat(viewModel.gameOver.value).isFalse()
    }
}
