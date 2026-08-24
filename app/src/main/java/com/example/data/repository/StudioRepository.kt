package com.example.data.repository

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.studio.StudioDao
import com.example.data.local.studio.VideoProjectEntity
import com.example.domain.model.studio.FallbackResourceMode
import com.example.domain.model.studio.GenerationStage
import com.example.domain.model.studio.LicensedAsset
import com.example.domain.model.studio.VideoAsset
import com.example.domain.model.studio.VideoGenerationJob
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoRenderStatus
import com.example.domain.model.studio.VideoStatus
import com.example.domain.model.studio.VideoStyle
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val ideaAdapter = moshi.adapter(VideoIdea::class.java)
    private val planAdapter = moshi.adapter(VideoPlan::class.java)
    private val styleAdapter = moshi.adapter(VideoStyle::class.java)
    private val jobAdapter = moshi.adapter(VideoGenerationJob::class.java)
    private val fallbackAdapter = moshi.adapter(FallbackResourceMode::class.java)
    private val assetListAdapter = moshi.adapter<List<VideoAsset>>(
        Types.newParameterizedType(List::class.java, VideoAsset::class.java)
    )
    private val licensedAssetListAdapter = moshi.adapter<List<LicensedAsset>>(
        Types.newParameterizedType(List::class.java, LicensedAsset::class.java)
    )

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
        } catch (_: Exception) {
            VideoIdea()
        }

        val parsedPlan = try {
            planAdapter.fromJson(this.planJson) ?: VideoPlan()
        } catch (_: Exception) {
            VideoPlan()
        }

        val parsedStyle = try {
            if (this.styleJson.isNotBlank()) styleAdapter.fromJson(this.styleJson) ?: VideoStyle() else VideoStyle()
        } catch (_: Exception) {
            VideoStyle()
        }

        val parsedAssets = try {
            if (this.assetsJson.isNotBlank()) assetListAdapter.fromJson(this.assetsJson) ?: emptyList() else emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val parsedLicensedAssets = try {
            if (this.licensedAssetsJson.isNotBlank()) licensedAssetListAdapter.fromJson(this.licensedAssetsJson) ?: emptyList() else emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val parsedFallbackMode = try {
            if (this.fallbackModeJson.isNotBlank()) fallbackAdapter.fromJson(this.fallbackModeJson) ?: FallbackResourceMode() else FallbackResourceMode()
        } catch (_: Exception) {
            FallbackResourceMode()
        }

        val parsedJob = try {
            if (this.jobJson.isNotBlank()) jobAdapter.fromJson(this.jobJson) else null
        } catch (_: Exception) {
            null
        }

        val parsedStatus = try {
            VideoStatus.valueOf(this.status)
        } catch (_: Exception) {
            VideoStatus.DRAFT
        }

        val parsedRenderStatus = try {
            VideoRenderStatus.valueOf(this.renderStatus)
        } catch (_: Exception) {
            VideoRenderStatus.IDLE
        }

        val parsedStage = try {
            GenerationStage.valueOf(this.generationStage)
        } catch (_: Exception) {
            GenerationStage.IDLE
        }

        return VideoProject(
            id = this.id,
            title = this.title,
            status = parsedStatus,
            renderStatus = parsedRenderStatus,
            generationStage = parsedStage,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            idea = parsedIdea,
            plan = parsedPlan,
            style = parsedStyle,
            assets = parsedAssets,
            licensedAssets = parsedLicensedAssets,
            fallbackMode = parsedFallbackMode,
            currentJob = parsedJob,
            errorMessage = this.errorMessage
        )
    }

    private fun VideoProject.toEntity(): VideoProjectEntity {
        return VideoProjectEntity(
            id = this.id,
            title = this.title,
            status = this.status.name,
            renderStatus = this.renderStatus.name,
            generationStage = this.generationStage.name,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            ideaJson = ideaAdapter.toJson(this.idea),
            planJson = planAdapter.toJson(this.plan),
            styleJson = styleAdapter.toJson(this.style),
            assetsJson = assetListAdapter.toJson(this.assets),
            licensedAssetsJson = licensedAssetListAdapter.toJson(this.licensedAssets),
            fallbackModeJson = fallbackAdapter.toJson(this.fallbackMode),
            jobJson = this.currentJob?.let { jobAdapter.toJson(it) } ?: "",
            errorMessage = this.errorMessage
        )
    }
}

