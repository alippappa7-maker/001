package com.example.domain.service.studio.template

import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoScene
import com.example.domain.service.studio.Media3VideoRenderService
import com.example.domain.service.studio.template.AnimationTemplate
import com.example.domain.service.studio.template.CinematicTemplate
import com.example.domain.service.studio.template.DocumentaryTemplate
import com.example.domain.service.studio.template.EducationalTemplate
import com.example.domain.service.studio.template.FastReelsTemplate
import com.example.domain.service.studio.template.ShortAdTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات تؤكد أن كل قالب من الأنماط الستة العامة ينتج سلوكًا بصريًا متميّزًا
 * (عدد مشاهد، مدد، انتقالات، طبقات نص، محاذاة) — وليس مجرد لون خلفية مختلف.
 *
 * كل القوالب خالية من Android Bitmap (خلفيات SOLID_COLOR فقط)، فلا تحتاج Robolectric.
 */
class GeneralTemplatesTest {

    private fun projectWith(
        style: EditingStyle,
        texts: List<String>,
        orientation: VideoOrientation = VideoOrientation.PORTRAIT
    ): VideoProject = VideoProject(
        idea = VideoIdea(ideaText = texts.firstOrNull() ?: "", editingStyle = style, orientation = orientation),
        plan = VideoPlan(
            durationSeconds = 30,
            scenes = texts.map { VideoScene(durationSeconds = 5, onScreenText = it) }
        )
    )

    // ---------------------------------------------------------------- CINEMATIC

    @Test
    fun `cinematic - يبني عنوانًا ومشاهد نبضية وخاتمة`() {
        val project = projectWith(EditingStyle.CINEMATIC, listOf("العنوان", "جملة ثانية", "جملة ثالثة"))
        val storyboard = CinematicTemplate().build(project)

        assertEquals(4, storyboard.scenes.size) // عنوان + 2 نبض + خاتمة
        assertEquals("cinematic_title", storyboard.scenes.first().id)
        assertEquals("cinematic_closing", storyboard.scenes.last().id)
        // العنوان مدة أطول من النبض.
        assertTrue(storyboard.scenes.first().durationMs > storyboard.scenes[1].durationMs)
        // انتقالات طويلة سينمائيًا.
        assertTrue(storyboard.scenes.first().transitionMs >= 800L)
    }

    @Test
    fun `cinematic - عند فراغ النصوص يستخدم placeholder`() {
        val project = VideoProject(idea = VideoIdea(editingStyle = EditingStyle.CINEMATIC))
        val storyboard = CinematicTemplate().build(project)

        assertEquals(2, storyboard.scenes.size) // عنوان فقط + خاتمة
        assertTrue(storyboard.scenes.first().textLayers.first().text.isNotBlank())
    }

    // ---------------------------------------------------------------- DOCUMENTARY

    @Test
    fun `documentary - كل مشهد له طبقة عنوان ونص رئيسي`() {
        val project = projectWith(EditingStyle.DOCUMENTARY, listOf("المقدمة", "نقطة ١", "نقطة ٢"))
        val storyboard = DocumentaryTemplate().build(project)

        // مقدمة + نقطتان + خاتمة = 4 مشاهد.
        assertEquals(4, storyboard.scenes.size)
        assertEquals("doc_intro", storyboard.scenes.first().id)
        // كل مشهد نقطة له طبقتا نص على الأقل (عنوان + نص).
        val pointScene = storyboard.scenes[1]
        assertTrue(pointScene.textLayers.size >= 2)
        assertTrue(pointScene.textLayers.first().text.startsWith("نقطة"))
    }

    // ---------------------------------------------------------------- EDUCATIONAL

    @Test
    fun `educational - مشاهد الدرس مرقّمة بالأرقام العربية`() {
        val project = projectWith(EditingStyle.EDUCATIONAL, listOf("عنوان الدرس", "نقطة ١", "نقطة ٢"))
        val storyboard = EducationalTemplate().build(project)

        // عنوان + درسان + خاتمة = 4 مشاهد.
        assertEquals(4, storyboard.scenes.size)
        assertEquals("edu_lesson_1", storyboard.scenes[1].id)
        // طبقة الرقم موجودة في مشهد الدرس.
        val numberLayer = storyboard.scenes[1].textLayers.first()
        assertEquals('١'.toString(), numberLayer.text)
    }

    // ---------------------------------------------------------------- FAST_REELS

    @Test
    fun `fast_reels - مشاهد قصيرة وانتقالات سريعة`() {
        val texts = listOf("أول", "ثانٍ", "ثالث", "رابع")
        val project = projectWith(EditingStyle.FAST_REELS, texts)
        val storyboard = FastReelsTemplate().build(project)

        assertEquals(texts.size, storyboard.scenes.size)
        // كل المشاهد قصيرة (أقل من 3 ثوانٍ).
        storyboard.scenes.forEach { assertTrue(it.durationMs < 3000L) }
        // الانتقالات سريعة.
        storyboard.scenes.forEach { assertTrue(it.transitionMs <= 250L) }
    }

    @Test
    fun `fast_reels - موضع النص يتبدّل بين المشاهد`() {
        val project = projectWith(EditingStyle.FAST_REELS, listOf("أ", "ب", "ج"))
        val storyboard = FastReelsTemplate().build(project)

        val anchor0 = storyboard.scenes[0].textLayers.first().verticalAnchor
        val anchor1 = storyboard.scenes[1].textLayers.first().verticalAnchor
        assertNotEquals(anchor0, anchor1)
    }

    // ---------------------------------------------------------------- SHORT_AD

    @Test
    fun `short_ad - بنية hook ثم benefit ثم cta`() {
        val project = projectWith(EditingStyle.SHORT_AD, listOf("جذب", "فائدة ١", "فائدة ٢"))
        val storyboard = ShortAdTemplate().build(project)

        // hook + 1 benefit + cta = 3 مشاهد (آخر نص يُحجز كـ CTA).
        assertEquals(3, storyboard.scenes.size)
        assertEquals("ad_hook", storyboard.scenes.first().id)
        assertEquals("ad_cta", storyboard.scenes.last().id)
        // مشهد CTA مختلف بوضوح (خلفية مختلفة عن hook).
        assertNotEquals(
            storyboard.scenes.first().background.colorArgb,
            storyboard.scenes.last().background.colorArgb
        )
    }

    @Test
    fun `short_ad - عند فراغ CTA يستخدم placeholder واضح`() {
        val project = projectWith(EditingStyle.SHORT_AD, listOf("جذب فقط"))
        val storyboard = ShortAdTemplate().build(project)

        val ctaText = storyboard.scenes.last().textLayers.first().text
        assertTrue(ctaText.contains("دعوة الإجراء"))
    }

    // ---------------------------------------------------------------- ANIMATION

    @Test
    fun `animation - خلفيات زاهية متبادلة بين المشاهد`() {
        val texts = listOf("أول", "ثانٍ", "ثالث", "رابع", "خامس")
        val project = projectWith(EditingStyle.ANIMATION, texts)
        val storyboard = AnimationTemplate().build(project)

        assertEquals(texts.size, storyboard.scenes.size)
        // الخلفيات تتبدّل (ليست كلها متطابقة).
        val bgColors = storyboard.scenes.map { it.background.colorArgb }.toSet()
        assertTrue(bgColors.size > 1)
    }

    @Test
    fun `animation - كل مشهد يحوي علامة شكلية كطبقة`() {
        val project = projectWith(EditingStyle.ANIMATION, listOf("أ", "ب"))
        val storyboard = AnimationTemplate().build(project)

        storyboard.scenes.forEach { scene ->
            assertTrue(scene.textLayers.isNotEmpty())
            val marker = scene.textLayers.first().text
            assertTrue(marker in listOf("◆", "●", "■", "▲"))
        }
    }

    // ---------------------------------------------------------------- تكامل

    /**
     * اختبار تكامل: يؤكد أن كل EditingStyle من الأنماط العامة الستة يُوجَّه
     * إلى قالب متميّز غير null عبر [Media3VideoRenderService.templateForStyle] —
     * ولا يسقط أي نمط في المسار العام لـ StoryboardBuilder. الدالة مستقلة
     * ولا تحتاج Android Context، لذا يمكن اختبارها خالصة.
     */
    @Test
    fun `كل نمط تحرير عام يُوجَّه إلى قالب متميّز غير null عبر templateForStyle`() {
        val styles = listOf(
            EditingStyle.CINEMATIC,
            EditingStyle.DOCUMENTARY,
            EditingStyle.EDUCATIONAL,
            EditingStyle.FAST_REELS,
            EditingStyle.SHORT_AD,
            EditingStyle.ANIMATION
        )

        styles.forEach { style ->
            val template = Media3VideoRenderService.templateForStyle(style)
            assertNotNull("$style يجب أن يُوجَّه إلى قالب غير null", template)
        }

        // نتائج القوالب يجب أن تختلف في بنيتها (عدد المشاهد أو الخلفيات).
        val texts = listOf("نص أول", "نص ثانٍ", "نص ثالث")
        val sceneCounts = styles.map { style ->
            val template = Media3VideoRenderService.templateForStyle(style)!!
            val project = projectWith(style, texts)
            template.build(project).scenes.size
        }.toSet()
        assertTrue("يجب أن تختلف القوالب في عدد المشاهد أو بنيتها", sceneCounts.size > 1)
    }

    @Test
    fun `الأنماط الخاصة ترجع null من templateForStyle لأنها تُعالج داخل الخدمة`() {
        // MEDITATIVE/STORYTELLING/MOVING_QUOTES و QURAN_RECITATION تُعالج داخل
        // Media3VideoRenderService مباشرة (بعضها يعتمد على Android)، لذا يجب
        // أن ترجع null من الدالة المستقلة.
        val specialStyles = listOf(
            EditingStyle.MEDITATIVE,
            EditingStyle.STORYTELLING,
            EditingStyle.MOVING_QUOTES
        )
        specialStyles.forEach { style ->
            val template = Media3VideoRenderService.templateForStyle(style)
            assertEquals("$style يُعالج داخل الخدمة، يجب أن يرجع null هنا", null, template)
        }
    }
}
