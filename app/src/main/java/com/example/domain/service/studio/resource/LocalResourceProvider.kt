package com.example.domain.service.studio.resource

import com.example.domain.model.studio.ResourceIntent
import com.example.domain.model.studio.VideoAsset
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.AssetType

/**
 * مزود يفحص الأصول المحلية التي اختارها المستخدم للمشهد.
 *
 * يطابق [ResourceIntent.attachedAssetId] مع [VideoProject.assets]،
 * فإن وُجد أصل محلي مطابق (صورة/فيديو) يعيد مساره كـ [MediaResource].
 *
 * هذا المزود متاح دائمًا ولا يحتاج إنترنت — أولوية على المزودات الخارجية.
 */
class LocalResourceProvider(
    private val project: VideoProject
) : MediaResourceProvider {

    override val isAvailable: Boolean = true
    override val displayName: String = "موارد محلية (مستخدم)"

    private val assetsById: Map<String, VideoAsset> = project.assets.associateBy { it.id }

    override suspend fun resolve(intent: ResourceIntent): MediaResource? {
        val assetId = intent.attachedAssetId ?: return null
        val asset = assetsById[assetId] ?: return null
        val path = asset.sourceUrlOrPath?.takeIf { it.isNotBlank() } ?: return null

        return when (asset.type) {
            AssetType.IMAGE -> MediaResource.LocalImage(path)
            AssetType.VIDEO_CLIP -> MediaResource.LocalVideo(path, intent.durationMs)
            AssetType.GRAPHIC -> MediaResource.LocalImage(path)
            // الأنواع الصوتية والخطوط لا تُستخدم كخلفيات بصرية.
            else -> null
        }
    }
}
