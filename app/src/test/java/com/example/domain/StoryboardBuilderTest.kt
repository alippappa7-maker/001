package com.example.domain

import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.VideoDuration
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoScene
import com.example.domain.service.studio.StoryboardBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات وحدة لـ [StoryboardBuilder] — طبقة جسر منطقية بحتة (لا تحتاج Robolectric)
 * لأنها لا تلمس Android Bitmap. تتحقق من صحة بناء لوحة القصة من المشروع.
 */
class StoryboardBuilderTest {

    private val builder = StoryboardBuilder()

    @Test
    fun `build من مشروع بلا مشاهد يولّد مشهدًا افتراضيًا واحدًا بنص الفكرة`() {
        val project = VideoProject(
            title = "تجربة",
            idea = VideoIdea(ideaText = "كل نفس ذائقة الموت"),
            plan = VideoPlan(
                durationSeconds = 15,
                scenes = emptyList(),
                suggestedTexts = listOf("إن مع العسر يسرًا")
            )
        )

        val storyboard = builder.build(project)

        assertEquals(1, storyboard.scenes.size)
        assertEquals(720, storyboard.width)
        assertEquals(1280, storyboard.height)
        assertEquals(30, storyboard.fps)
        val scene = storyboard.scenes.first()
        assertTrue(scene.durationMs > 0)
        assertEquals(1, scene.textLayers.size)
        assertEquals("إن مع العسر يسرًا", scene.textLayers.first().text)
    }

    @Test
    fun `build يحوّل مشاهد الخطة إلى مشاهد تركيب ويحافظ على النصوص الظاهرة`() {
        val project = VideoProject(
            idea = VideoIdea(orientation = VideoOrientation.SQUARE),
            plan = VideoPlan(
                durationSeconds = 30,
                scenes = listOf(
                    VideoScene(durationSeconds = 10, onScreenText = "اللهم صلّ وسلم"),
                    VideoScene(durationSeconds = 10, onScreenText = "على نبينا محمد"),
                    VideoScene(durationSeconds = 10, onScreenText = "وعلى آله وصحبه")
                )
            )
        )

        val storyboard = builder.build(project)

        assertEquals(3, storyboard.scenes.size)
        assertEquals(1080, storyboard.width)
        assertEquals(3, storyboard.textLayerCount)
        assertEquals("اللهم صلّ وسلم", storyboard.scenes[0].textLayers.first().text)
        assertEquals("على آله وصحبه", storyboard.scenes[2].textLayers.first().text)
        // كل مشهد له مدة موجبة
        storyboard.scenes.forEach { assertTrue(it.durationMs > 0) }
    }

    @Test
    fun `build مع خلفية افتراضية تكون لونًا ثابتًا`() {
        val project = VideoProject(
            idea = VideoIdea(editingStyle = EditingStyle.MEDITATIVE),
            plan = VideoPlan(
                durationSeconds = 15,
                scenes = listOf(VideoScene(durationSeconds = 15, onScreenText = "سبحان الله"))
            )
        )

        val storyboard = builder.build(project)
        val background = storyboard.scenes.first().background

        assertEquals(BackgroundType.SOLID_COLOR, background.type)
        assertTrue(background.isUsable())
    }

    @Test
    fun `buildFromTexts يبني مشهدًا لكل نص`() {
        val texts = listOf("الله", "أكبر", "سبحان الله")
        val storyboard = builder.buildFromTexts(
            texts = texts,
            orientation = VideoOrientation.PORTRAIT,
            durationSecondsTotal = 30,
            style = EditingStyle.MOVING_QUOTES
        )

        assertEquals(3, storyboard.scenes.size)
        assertTrue(storyboard.scenes.first().textLayers.first().glow)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `storyboard فارغ يرفع استثناء`() {
        // التأكد من أن قيد النموذج يُفرض: لا يمكن بناء storyboard بلا مشاهد.
        builder.buildFromTexts(
            texts = emptyList(),
            orientation = VideoOrientation.PORTRAIT,
            durationSecondsTotal = 15
        )
    }
}
