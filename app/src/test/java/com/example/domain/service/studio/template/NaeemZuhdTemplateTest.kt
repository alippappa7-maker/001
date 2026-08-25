package com.example.domain.service.studio.template

import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.NaeemZuhdTemplateInput
import com.example.domain.model.studio.VideoOrientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات وحدة لـ [NaeemZuhdTemplate] — تتم عبر [NoOpSunsetBackgroundProvider]
 * فلا تُنشئ أي Bitmap (لا حاجة لـ Robolectric). تتحقق من بنية المشاهد
 * وأن القالب لا يختلق أي نص ديني.
 */
class NaeemZuhdTemplateTest {

    private val template = NaeemZuhdTemplate(NoOpSunsetBackgroundProvider)

    @Test
    fun `build بمدخلات كاملة يولّد الافتتاح والانعكاسات والخاتمة`() {
        val input = NaeemZuhdTemplateInput(
            hadithText = "اللهم أغنني بالعلم",
            hadithSource = "رواه الإمام أحمد",
            reflectiveLines = listOf("تأمل النعمة", "والشكر يزيد")
        )

        val storyboard = template.build(input, VideoOrientation.PORTRAIT)

        // 1 افتتاح + 2 انعكاس + 1 خاتمة = 4 مشاهد
        assertEquals(4, storyboard.scenes.size)
        assertEquals(720, storyboard.width)
        assertEquals(1280, storyboard.height)
        assertEquals(30, storyboard.fps)
    }

    @Test
    fun `مشهد الافتتاح يحوي نص الحديث من المدخلات`() {
        val input = NaeemZuhdTemplateInput(hadithText = "استعينوا على قضاء الحوائج بالكتمان")

        val opening = template.build(input, VideoOrientation.PORTRAIT).scenes.first()

        assertEquals("naeem_opening", opening.id)
        assertEquals("استعينوا على قضاء الحوائج بالكتمان", opening.textLayers.first().text)
        assertTrue(opening.textLayers.first().glow)
    }

    @Test
    fun `حقل الحديث الفارغ يستخدم placeholder واضح دون اختلاق نص ديني`() {
        val input = NaeemZuhdTemplateInput(hadithText = "")

        val opening = template.build(input, VideoOrientation.PORTRAIT).scenes.first()

        assertEquals("أدخل نص الحديث أو الأثر هنا", opening.textLayers.first().text)
    }

    @Test
    fun `المصدر الاختياري يظهر كطبقة نص ثانية`() {
        val input = NaeemZuhdTemplateInput(hadithText = "قال علي", hadithSource = "رواه البخاري")

        val layers = template.build(input, VideoOrientation.PORTRAIT).scenes.first().textLayers

        assertEquals("رواه البخاري", layers[1].text)
        assertEquals("قال علي", layers.first().text)
    }

    @Test
    fun `عند غياب مزوّد الغروب تستخدم خلفية لون ثابت احتياطي`() {
        val input = NaeemZuhdTemplateInput(hadithText = "قال علي")

        val opening = template.build(input, VideoOrientation.PORTRAIT).scenes.first()

        // NoOp مزوّد يعيد null → القالب يلجأ إلى SOLID_COLOR بدلاً من IMAGE
        assertEquals(BackgroundType.SOLID_COLOR, opening.background.type)
    }

    @Test
    fun `مشاهد الانعكاس تأخذ نصوصها من المدخلات بنفس الترتيب`() {
        val input = NaeemZuhdTemplateInput(
            hadithText = "الآية",
            reflectiveLines = listOf("الجملة الأولى", "الجملة الثانية")
        )

        val scenes = template.build(input, VideoOrientation.PORTRAIT).scenes
        val reflections = scenes.filter { it.id.startsWith("naeem_reflection_") }

        assertEquals(2, reflections.size)
        assertEquals("الجملة الأولى", reflections[0].textLayers.first().text)
        assertEquals("الجملة الثانية", reflections[1].textLayers.first().text)
    }

    @Test
    fun `fromProject يأخذ النص من مشاهد المشروع دون اختلاق أي نص`() {
        val project = createProjectWithTexts(listOf("حديث من المشروع", "جملة ثانية"))

        val input = template.fromProject(project)

        assertEquals("حديث من المشروع", input.hadithText)
        assertEquals(listOf("جملة ثانية"), input.reflectiveLines)
        assertEquals("قبس", input.brandName)
    }

    private fun createProjectWithTexts(texts: List<String>) =
        com.example.domain.model.studio.VideoProject(
            idea = com.example.domain.model.studio.VideoIdea(
                ideaText = "النص الاحتياطي",
                orientation = VideoOrientation.PORTRAIT,
                editingStyle = com.example.domain.model.studio.EditingStyle.STORYTELLING
            ),
            plan = com.example.domain.model.studio.VideoPlan(
                scenes = texts.map { text ->
                    com.example.domain.model.studio.VideoScene(onScreenText = text)
                }
            )
        )
}
