package com.example.data.repository

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.studio.StudioDao
import com.example.data.local.studio.VideoProjectEntity
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface StudioRepository {
    fun getAllProjects(): Flow<List<VideoProject>>
    suspend fun getProjectById(id: String): VideoProject?
    suspend fun saveProject(project: VideoProject)
    suspend fun deleteProject(id: String)
}

class StudioRepositoryImpl(private val application: Application) : StudioRepository {

    private val studioDao: StudioDao = AppDatabase.getDatabase(application).studioDao()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val ideaAdapter = moshi.adapter(VideoIdea::class.java)
    private val planAdapter = moshi.adapter(VideoPlan::class.java)

    override fun getAllProjects(): Flow<List<VideoProject>> {
        return studioDao.getAllProjects().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getProjectById(id: String): VideoProject? {
        return studioDao.getProjectById(id)?.toDomainModel()
    }

    override suspend fun saveProject(project: VideoProject) {
        studioDao.insertProject(project.toEntity())
    }

    override suspend fun deleteProject(id: String) {
        studioDao.deleteProjectById(id)
    }

    private fun VideoProjectEntity.toDomainModel(): VideoProject {
        val parsedIdea = try {
            ideaAdapter.fromJson(this.ideaJson) ?: VideoIdea()
        } catch (e: Exception) {
            VideoIdea()
        }

        val parsedPlan = try {
            planAdapter.fromJson(this.planJson) ?: VideoPlan()
        } catch (e: Exception) {
            VideoPlan()
        }

        return VideoProject(
            id = this.id,
            title = this.title,
            status = try { VideoStatus.valueOf(this.status) } catch (e: Exception) { VideoStatus.DRAFT },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            idea = parsedIdea,
            plan = parsedPlan,
            errorMessage = this.errorMessage
        )
    }

    private fun VideoProject.toEntity(): VideoProjectEntity {
        return VideoProjectEntity(
            id = this.id,
            title = this.title,
            status = this.status.name,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            ideaJson = ideaAdapter.toJson(this.idea),
            planJson = planAdapter.toJson(this.plan),
            errorMessage = this.errorMessage
        )
    }
}
