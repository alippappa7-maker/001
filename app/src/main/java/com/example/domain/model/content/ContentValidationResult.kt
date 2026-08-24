package com.example.domain.model.content

sealed class ContentValidationResult {
    data object Valid : ContentValidationResult()
    data class Invalid(val reason: String) : ContentValidationResult()
}
