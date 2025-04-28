package com.example.mobiilisovellusprojekti.data

data class GameAnswer (
    val id: String,
    val answer: String,
    val isCorrect: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}