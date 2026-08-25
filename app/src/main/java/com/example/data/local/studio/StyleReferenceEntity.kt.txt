package com.example.data.local.studio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "style_references")
data class StyleReferenceEntity(
    @PrimaryKey
    val id: String,
    val sourceLabel: String,
    val signatureJson: String,
    val createdAt: Long
)
