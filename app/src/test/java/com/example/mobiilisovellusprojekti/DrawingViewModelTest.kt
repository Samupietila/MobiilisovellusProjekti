package com.example.mobiilisovellusprojekti

import com.example.mobiilisovellusprojekti.ViewModels.*
import androidx.compose.ui.geometry.Offset
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DrawingViewModelTest {

    private lateinit var viewModel: DrawingViewModel

    @Before
    fun setup() {
        viewModel = DrawingViewModel()
    }

    @Test
    fun `starts new path when OnNewPathStart is dispatched`() {
        viewModel.onAction(DrawingAction.OnNewPathStart)
        val state = viewModel.state.value
        assertNotNull(state.currentPath)
        assertEquals(0, state.currentPath?.path?.size)
    }

    @Test
    fun `draws offset correctly`() {
        viewModel.onAction(DrawingAction.OnNewPathStart)
        val point = Offset(10f, 10f)
        viewModel.onAction(DrawingAction.OnDraw(point))
        val state = viewModel.state.value
        assertEquals(1, state.currentPath?.path?.size)
        assertEquals(point, state.currentPath?.path?.first())
    }

    @Test
    fun `ends path and moves it to paths list`() {
        viewModel.onAction(DrawingAction.OnNewPathStart)
        viewModel.onAction(DrawingAction.OnDraw(Offset(5f, 5f)))
        viewModel.onAction(DrawingAction.OnPathEnd)
        val state = viewModel.state.value
        assertNull(state.currentPath)
        assertEquals(1, state.paths.size)
    }

    @Test
    fun `clears canvas`() {
        viewModel.onAction(DrawingAction.OnNewPathStart)
        viewModel.onAction(DrawingAction.OnDraw(Offset(5f, 5f)))
        viewModel.onAction(DrawingAction.OnPathEnd)
        viewModel.onAction(DrawingAction.OnClearCanvasClick)
        val state = viewModel.state.value
        assertTrue(state.paths.isEmpty())
    }

    @Test
    fun `changes selected color`() {
        val redColor = allColors[1] // Red
        viewModel.onAction(DrawingAction.OnSelectColor(redColor))
        assertEquals(redColor, viewModel.state.value.selectedColor)
    }
}