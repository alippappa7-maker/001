package com.example.domain.service.studio

import com.example.domain.model.studio.AssetLicense
import com.example.domain.model.studio.AssetType
import com.example.domain.model.studio.LicensedAsset
import com.example.domain.model.studio.ResourceSearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Interface for providing and querying media assets with verified licensing and attribution.
 * Designed for future integration with licensed asset libraries and local device media.
 */
interface ResourceProvider {
    /**
     * Search available resources according to query parameters.
     * In local mode, returns verified offline assets without live internet scraping.
     */
    suspend fun searchResources(query: ResourceSearchQuery): List<LicensedAsset>

    /**
     * Retrieve a specific resource by its unique ID.
     */
    suspend fun getResourceById(id: String): LicensedAsset?

    /**
     * Validate a user-provided or external resource:
     * - Rejects resources with missing source / invalid license (UNKNOWN_UNLICENSED).
     * - Ensures user consent is recorded.
     */
    suspend fun validateAndRegisterResource(asset: LicensedAsset): Result<LicensedAsset>

    /**
     * Retrieve all local assets registered by the user on this device.
     */
    fun observeLocalResources(): Flow<List<LicensedAsset>>

    /**
     * Save/register a user-uploaded local resource.
     */
    suspend fun saveUserResource(asset: LicensedAsset): Result<LicensedAsset>

    /**
     * Remove a local user resource.
     */
    suspend fun deleteUserResource(id: String): Boolean
}

/**
 * Local implementation of ResourceProvider managing offline fallback resources from phone storage.
 * Strictly enforces source verification, license validity, and explicit user consent.
 */
class LocalResourceProvider : ResourceProvider {

    private val localAssetsState = MutableStateFlow<List<LicensedAsset>>(createDefaultCuratedLocalAssets())

    override suspend fun searchResources(query: ResourceSearchQuery): List<LicensedAsset> {
        val current = localAssetsState.value
        return current.filter { asset ->
            val matchesText = query.queryText.isBlank() ||
                    asset.title.contains(query.queryText, ignoreCase = true) ||
                    asset.source.contains(query.queryText, ignoreCase = true)
            val matchesType = query.assetType == null || asset.assetType == query.assetType
            val matchesLicense = query.requiredLicense == null || asset.license == query.requiredLicense
            matchesText && matchesType && matchesLicense
        }
    }

    override suspend fun getResourceById(id: String): LicensedAsset? {
        return localAssetsState.value.find { it.id == id }
    }

    override suspend fun validateAndRegisterResource(asset: LicensedAsset): Result<LicensedAsset> {
        // 1. Consent check
        if (!asset.isConsentGiven) {
            return Result.failure(
                IllegalArgumentException("يلزم تأكيد موافقة المستخدم وتعهده بملكية أو ترخيص المورد قبل الاستخدام.")
            )
        }

        // 2. License check
        if (asset.license == AssetLicense.UNKNOWN_UNLICENSED || !asset.license.isPermitted) {
            return Result.failure(
                IllegalArgumentException("تم رفض المورد: الترخيص غير محدد أو غير مصرح به للاستخدام.")
            )
        }

        // 3. Source check for external assets
        if (!asset.isUserProvided && asset.source.isBlank()) {
            return Result.failure(
                IllegalArgumentException("تم رفض المورد: يجب تحديد مصدر واضح وموثق للمورد الخارجي.")
            )
        }

        // Save into local list
        val updatedList = localAssetsState.value.filter { it.id != asset.id } + asset
        localAssetsState.value = updatedList
        return Result.success(asset)
    }

    override fun observeLocalResources(): Flow<List<LicensedAsset>> {
        return localAssetsState.asStateFlow()
    }

    override suspend fun saveUserResource(asset: LicensedAsset): Result<LicensedAsset> {
        return validateAndRegisterResource(asset)
    }

    override suspend fun deleteUserResource(id: String): Boolean {
        val beforeSize = localAssetsState.value.size
        localAssetsState.value = localAssetsState.value.filter { it.id != id }
        return localAssetsState.value.size < beforeSize
    }

    private fun createDefaultCuratedLocalAssets(): List<LicensedAsset> {
        return listOf(
            LicensedAsset(
                id = "asset_default_calligraphy_1",
                title = "مخطوطة قبس الروحانية (بسملة ونور)",
                uriOrPath = "local://assets/calligraphy_1.png",
                assetType = AssetType.CALLIGRAPHY,
                fileSizeBytes = 420 * 1024L,
                source = "مكتبة قبس المعتمدة محليًا",
                license = AssetLicense.PUBLIC_DOMAIN,
                isUserProvided = false,
                isConsentGiven = true
            ),
            LicensedAsset(
                id = "asset_default_nature_1",
                title = "سماء فجرية متلألئة بالنجوم",
                uriOrPath = "local://assets/sky_dawn.png",
                assetType = AssetType.IMAGE,
                fileSizeBytes = 1200 * 1024L,
                source = "مكتبة مشاع إبداعي محلي",
                license = AssetLicense.CREATIVE_COMMONS_CC0,
                isUserProvided = false,
                isConsentGiven = true
            ),
            LicensedAsset(
                id = "asset_default_audio_1",
                title = "مؤثر صوتي هادئ للسكينة",
                uriOrPath = "local://assets/serenity_tone.mp3",
                assetType = AssetType.SOUND_EFFECT,
                fileSizeBytes = 850 * 1024L,
                source = "مكتبة قبس الصوتية المفتوحة",
                license = AssetLicense.LICENSED_STOCK,
                isUserProvided = false,
                isConsentGiven = true
            )
        )
    }
}
