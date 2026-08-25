package com.example.data.local.studio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    @Query("SELECT * FROM video_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<VideoProjectEntity>>

    @Query("SELECT * FROM video_projects WHERE status = :status ORDER BY updatedAt DESC")
    fun getProjectsByStatus(status: String): Flow<List<VideoProjectEntity>>

    @Query("SELECT * FROM video_projects WHERE id = :id")
    suspend fun getProjectById(id: String): VideoProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: VideoProjectEntity)

    @Update
    suspend fun updateProject(project: VideoProjectEntity)

    @Query("DELETE FROM video_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("DELETE FROM video_projects")
    suspend fun deleteAllProjects()

    // --- Style References (بصمات الأسلوب المستخرجة من فيديوهات مرجعية) ---

    @Query("SELECT * FROM style_references ORDER BY createdAt DESC")
    fun getAllStyleReferences(): Flow<List<StyleReferenceEntity>>

    @Query("SELECT * FROM style_references WHERE id = :id")
    suspend fun getStyleReferenceById(id: String): StyleReferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyleReference(reference: StyleReferenceEntity)

    @Query("DELETE FROM style_references WHERE id = :id")
    suspend fun deleteStyleReferenceById(id: String)
}
