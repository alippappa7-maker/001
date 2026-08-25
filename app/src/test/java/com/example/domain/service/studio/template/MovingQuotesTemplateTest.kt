package com.example.domain.service.studio.template

import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.MovingQuotesTemplateInput
import com.example.domain.model.studio.VideoOrientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات وحدة لـ [MovingQuotesTemplate] — لا تعتمد على أي Bitmap ولا Robolectric.
 * تتحقق من أن عدد المشاهد يساوي عدد الاقتباسات، وأن القالب لا يختلق أي نص.
 */
class MovingQuotesTemplateTest {

    private val template = MovingQuotesTemplate()

    @Test
    fun `build بعدد اقتباسات يولّد مشهدًا لكل اقتباس`() {
        val input = MovingQuotesTemplateInput(quotes = listOf("الاقتباس الأول", "الاقتباس الثاني", "الثالث"))

        val storyboard = template.build(input, VideoOrientation.SQUARE)

        assertEquals(3, storyboard.scenes.size)
        assertEquals(1080, storyboard.width)
        assertEquals(1080, storyboard.height)
    }

    @Test
    fun `كل مشهد يحوي نص اقتباسه ويستخدم خلفية داكنة`() {
        val input = MovingQuotesTemplateInput(quotes = listOf("الجملة الأولى", "الجملة الثانية"))

        val scenes = template.build(input, VideoOrientation.PORTRAIT).scenes

        assertEquals("الجملة الأولى", scenes[0].textLayers.first().text)
        assertEquals("الجملة الثانية", scenes[1].textLayers.first().text)
        scenes.forEach {
            assertEquals(BackgroundType.SOLID_COLOR, it.background.type)
            assertTrue(it.textLayers.first().glow)
        }
    }

    @Test
    fun `قائمة فارغة تنتج مشهدًا واحدًا بـ placeholder واضح`() {
        val input = MovingQuotesTemplateInput(quotes = emptyList())

        val scenes = template.build(input, VideoOrientation.PORTRAIT).scenes

        assertEquals(1, scenes.size)
        assertEquals("أدخل اقتباسك هنا", scenes.first().textLayers.first().text)
    }

    @Test
    fun `fromProject يأخذ الاقتباسات من مشاهد المشروع دون اختلاق أي نص`() {
        val project = createProjectWithTexts(listOf("حكمة من المشروع", "حكمة ثانية"))

        val input = template.fromProject(project)

        assertEquals(listOf("حكمة من المشروع", "حكمة ثانية"), input.quotes)
        assertEquals("قبس", input.brandName)
    }

    private fun createProjectWithTexts(texts: List<String>) =
        com.example.domain.model.studio.VideoProject(
            idea = com.example.domain.model.studio.VideoIdea(
                ideaText = "النص الاحتياطي",
                orientation = VideoOrientation.PORTRAIT,
                editingStyle = com.example.domain.model.studio.EditingStyle.MOVING_QUOTES
            ),
            plan = com.example.domain.model.studio.VideoPlan(
                scenes = texts.map { text ->
                    com.example.domain.model.studio.VideoScene(onScreenText = text)
                }
            )
        )
}
