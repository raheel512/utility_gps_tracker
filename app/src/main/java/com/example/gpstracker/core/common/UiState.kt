package com.example.gpstracker.core.common

/**
 * Generic UI state wrapper for unidirectional data flow screens.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
