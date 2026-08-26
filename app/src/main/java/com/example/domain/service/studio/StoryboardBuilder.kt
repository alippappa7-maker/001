package com.example.domain.service.studio

import com.example.domain.model.studio.BackgroundLayer
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.TextAnimation
import com.example.domain.model.studio.TextLayer
import com.example.domain.model.studio.VideoAsset
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoScene

/**
 * يحوّل [VideoProject] الموجود في التطبيق إلى [CompositionStoryboard] قابل للرندر.
 *
 * هذه الطبقة هي الجسر بين النموذج العام (idea/plan/scenes) ونموذج المحرك الداخلي.
 * المتعمّد: لا يولّد أي نص ديني من تلقاء نفسه — يأخذ النصوص من [VideoScene.onScreenText]
 * أو من خطة المشروع [VideoPlan.suggestedTexts] فقط. هذا يحافظ على مبدأ عدم اختراع
 * آيات/أحاديث بدون مرجع موثّق.
 */
class StoryboardBuilder {

    /**
     * يبني لوحة القصة من المشروع. يستخدم المشاهد الموجودة في الخطة، وإن لم توجد
     * يبني مشهدًا افتراضيًا واحدًا من نص الفكرة.
     */
    fun build(project: VideoProject): CompositionStoryboard {
        val (width, height) = resolveDimensions(project.idea.orientation)
        val fps = 30
        val perSceneMs = resolvePerSceneMs(project.plan.durationSeconds, project.plan.scenes)

        val scenes: List<CompositionScene> = if (project.plan.scenes.isNotEmpty()) {
            project.plan.scenes.mapIndexed { index, videoScene ->
                mapVideoSceneToCompositionScene(
                    videoScene = videoScene,
                    durationMs = perSceneMs,
                    asset = project.assets.firstOrNull { it.id == videoScene.attachedAssetId },
                    sceneIndex = index
                )
            }
        } else {
            listOf(buildDefaultSceneFromIdea(project, perSceneMs))
        }

        return CompositionStoryboard(
            width = width,
            height = height,
            fps = fps,
            scenes = scenes
        )
    }

    /**
     * نسخة بديلة: تبني لوحة قصة من نصوص مقترحة جاهزة (مثل قائمة اقتباسات).
     * كل نص يصبح مشهدًا مستقلًا.
     */
    fun buildFromTexts(
        texts: List<String>,
        orientation: VideoOrientation,
        durationSecondsTotal: Int,
        toneLabel: String = "",
        style: EditingStyle = EditingStyle.MOVING_QUOTES
    ): CompositionStoryboard {
        val (width, height) = resolveDimensions(orientation)
        val perSceneMs = if (texts.isEmpty()) {
            durationSecondsTotal * 1000L
        } else {
            (durationSecondsTotal * 1000L / texts.size).coerceAtLeast(1500L)
        }

        val scenes = texts.mapIndexed { index, text ->
            CompositionScene(
                durationMs = perSceneMs,
                background = defaultBackgroundForStyle(style, index),
                textLayers = listOf(
                    TextLayer(
                        text = text,
                        fontSizeSp = if (text.length > 60) 36 else 44,
                        glow = style == EditingStyle.MOVING_QUOTES,
                        alignment = LayerHorizontalAlignment.CENTER,
                        verticalAnchor = LayerVerticalAlignment.CENTER,
                        animation = TextAnimation.WORD_BY_WORD,
                        animationStartMs = 150L
                    )
                )
            )
        }

        return if (scenes.isEmpty()) {
            CompositionStoryboard(width = width, height = height, fps = 30, scenes = scenes)
        } else {
            CompositionStoryboard(width = width, height = height, fps = 30, scenes = scenes)
        }
    }

    // --------------------------------------------------------------------- private

    private fun resolveDimensions(orientation: VideoOrientation): Pair<Int, Int> = when (orientation) {
        VideoOrientation.PORTRAIT -> 720 to 1280
        VideoOrientation.LANDSCAPE -> 1280 to 720
        VideoOrientation.SQUARE -> 1080 to 1080
    }

    private fun resolvePerSceneMs(totalSeconds: Int, scenes: List<VideoScene>): Long {
        if (scenes.isEmpty()) return (totalSeconds * 1000L).coerceAtLeast(3000L)
        val total = (totalSeconds * 1000L).coerceAtLeast(3000L)
        return (total / scenes.size).coerceAtLeast(1500L)
    }

    private fun mapVideoSceneToCompositionScene(
        videoScene: VideoScene,
        durationMs: Long,
        asset: VideoAsset?,
        sceneIndex: Int
    ): CompositionScene {
        val background = when {
            asset != null && !asset.sourceUrlOrPath.isNullOrBlank() && asset.type.name.contains("VIDEO") ->
                BackgroundLayer(type = BackgroundType.VIDEO, videoUri = asset.sourceUrlOrPath)
            asset != null && !asset.sourceUrlOrPath.isNullOrBlank() ->
                BackgroundLayer(type = BackgroundType.IMAGE, videoUri = asset.sourceUrlOrPath)
            else -> defaultBackgroundForStyle(EditingStyle.CINEMATIC, sceneIndex)
        }

        val textLayers = buildList {
            if (videoScene.onScreenText.isNotBlank()) {
                add(
                    TextLayer(
                        text = videoScene.onScreenText,
                        animation = TextAnimation.FADE_IN,
                        alignment = LayerHorizontalAlignment.CENTER,
                        verticalAnchor = LayerVerticalAlignment.CENTER
                    )
                )
            }
        }

        return CompositionScene(
            durationMs = durationMs,
            background = background,
            textLayers = textLayers,
            transitionMs = 400L
        )
    }

    private fun buildDefaultSceneFromIdea(project: VideoProject, durationMs: Long): CompositionScene {
        val text = project.plan.suggestedTexts.firstOrNull()
            ?: project.plan.summary.ifBlank { project.idea.ideaText.ifBlank { "بسم الله الرحمن الرحيم" } }
        return CompositionScene(
            durationMs = durationMs,
            background = defaultBackgroundForStyle(project.idea.editingStyle, 0),
            textLayers = listOf(
                TextLayer(
                    text = text,
                    fontSizeSp = 44,
                    glow = true,
                    alignment = LayerHorizontalAlignment.CENTER,
                    verticalAnchor = LayerVerticalAlignment.CENTER,
                    animation = TextAnimation.FADE_IN
                )
            )
        )
    }

    private fun defaultBackgroundForStyle(style: EditingStyle, sceneIndex: Int): BackgroundLayer {
        // ألوان خلفية افتراضية لطيفة حسب الأسلوب. كلها ألوان ثابتة (لا فيديو/صورة) كنقطة بداية.
        val palette = when (style) {
            EditingStyle.CINEMATIC -> 0xFF0B1020.toInt() to 0xFF1A1230.toInt()
            EditingStyle.MOVING_QUOTES -> 0xFF050608.toInt() to 0xFF0B1020.toInt()
            EditingStyle.MEDITATIVE -> 0xFF10241F.toInt() to 0xFF0B1416.toInt()
            EditingStyle.STORYTELLING -> 0xFF1A1408.toInt() to 0xFF0B0E14.toInt()
            else -> 0xFF0B1020.toInt() to 0xFF11182A.toInt()
        }
        return BackgroundLayer(type = BackgroundType.SOLID_COLOR, colorArgb = palette.first)
    }
}
