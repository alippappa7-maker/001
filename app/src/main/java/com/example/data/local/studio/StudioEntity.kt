package com.example.data.local.studio

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.studio.VideoStatus

@Entity(tableName = "video_projects")
data class VideoProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val ideaJson: String,
    val planJson: String,
    val errorMessage: String?
)
