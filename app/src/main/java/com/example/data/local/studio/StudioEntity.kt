package com.example.data.local.studio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_projects")
data class VideoProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val status: String,
    val renderStatus: String = "IDLE",
    val generationStage: String = "IDLE",
    val createdAt: Long,
    val updatedAt: Long,
    val ideaJson: String,
    val planJson: String,
    val styleJson: String = "",
    val assetsJson: String = "",
    val licensedAssetsJson: String = "",
    val fallbackModeJson: String = "",
    val jobJson: String = "",
    val errorMessage: String? = null
)

