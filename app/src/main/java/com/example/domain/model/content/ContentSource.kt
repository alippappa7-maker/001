package com.example.domain.model.content

data class ContentSource(
    val name: String,
    val reference: String? = null,
    val url: String? = null,
    val verified: Boolean = false
)
