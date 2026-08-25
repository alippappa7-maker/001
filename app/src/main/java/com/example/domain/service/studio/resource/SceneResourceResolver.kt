package com.example.domain.service.studio.resource

import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.VideoProject

/**
 * يحلّ نوايا الموارد ([com.example.domain.model.studio.ResourceIntent])
 * المرتبطة بمشاهد لوحة القصة، ويستبدل الخلفيات اللونية الاحتياطية
 * بموارد حقيقية (صور/فيديوهات) متى ما توفّرت.
 *
 * سلسلة الأولوية (عبر [CompositeResourceProvider]):
 * 1. أصل محلي أرفقه المستخدم بالمشهد.
 * 2. مورد منزّل مُخزّن محليًا من Pexels/Pixabay.
 * 3. مورد جديد يُجلب من Pexels/Pixabay (يتطلب إنترنت/مفتاح).
 * 4. لون صلب احتياطي — لا يفشل أبدًا.
 *
 * إن لم يُوجد مورد حقيقي، تُترك خلفية المشهد كما هي (لون صلب)،
 * مما يضمن عمل التصدير دائمًا حتى بدون إنترنت أو مفاتيح API.
 */
class SceneResourceResolver(
    private val provider: CompositeResourceProvider
) {

    /**
     * يمرّ على مشاهد لوحة القصة، ويحلّ كل نيّة مورد إلى خلفية حقيقية إن أمكن.
     * يعيد لوحة قصة جديدة بموارد مُحلّلة (دون تعديل الأصلية).
     */
    suspend fun resolve(storyboard: CompositionStoryboard, project: VideoProject): CompositionStoryboard {
        val resolvedScenes = storyboard.scenes.map { scene ->
            resolveScene(scene, project)
        }
        return storyboard.copy(scenes = resolvedScenes)
    }

    private suspend fun resolveScene(
        scene: CompositionScene,
        project: VideoProject
    ): CompositionScene {
        val intent = scene.resourceIntent ?: return scene

        val resource = provider.resolve(intent) ?: return scene

        // إن لم يُوجد مورد حقيقي، ابقَ على الخلفية اللونية الحالية.
        if (!resource.isRealAsset) return scene

        val newBackground = when (resource) {
            is MediaResource.LocalImage -> scene.background.copy(
                type = BackgroundType.IMAGE,
                imageUri = resource.path
            )
            is MediaResource.LocalVideo -> scene.background.copy(
                type = BackgroundType.VIDEO,
                videoUri = resource.path
            )
            is MediaResource.SolidColor -> scene.background.copy(
                type = BackgroundType.SOLID_COLOR,
                colorArgb = resource.colorArgb
            )
        }

        return scene.copy(background = newBackground, resourceIntent = null)
    }
}
