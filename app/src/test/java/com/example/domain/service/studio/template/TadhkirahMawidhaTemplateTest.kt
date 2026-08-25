package com.example.domain.service.studio.template

import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.model.studio.TextAnimation
import com.example.domain.model.studio.TadhkirahTemplateInput
import com.example.domain.model.studio.VideoIdea
import com.example.domain.model.studio.VideoOrientation
import com.example.domain.model.studio.VideoPlan
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.VideoScene
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات وحدة لـ [TadhkirahMawidhaTemplate] — تتم عبر [NoOpOrnamentalFrameProvider]
 * فلا تُنشئ أي Bitmap (لا حاجة لـ Robolectric). تتحقق من بنية المشاهد والألوان
 * وأن القالب لا يختلق أي نص ديني.
 */
class TadhkirahMawidhaTemplateTest {

    private val template = TadhkirahMawidhaTemplate(NoOpOrnamentalFrameProvider)

    @Test
    fun `build بمدخلات كاملة يولّد الافتتاح والانعكاسات والخاتمة`() {
        val input = TadhkirahTemplateInput(
            openingVerseText = "أَلَمْ يَأْنِ لِلَّذِينَ آمَنُوا",
            reflectiveLines = listOf("ما الذي حصل بنا؟", "والله يناديك"),
            impactWord = "عِظام",
            closingLines = listOf("متى نرد إليه", "متى نبكي على حسرتنا"),
            brandName = "قبس"
        )

        val storyboard = template.build(input, VideoOrientation.PORTRAIT)

        // 1 افتتاح + 2 انعكاس + 1 كلمة مؤثرة + 2 خاتمة = 6 مشاهد
        assertEquals(6, storyboard.scenes.size)
        assertEquals(720, storyboard.width)
        assertEquals(1280, storyboard.height)
        assertEquals(30, storyboard.fps)
    }

    @Test
    fun `مشهد الافتتاح خلفيته خضراء داكنة ويحوي الآية من المدخلات`() {
        val input = TadhkirahTemplateInput(
            openingVerseText = "إن مع العسر يسرًا",
            reflectiveLines = listOf("تفكر"),
            closingLines = listOf("الخاتمة")
        )

        val opening = template.build(input, VideoOrientation.PORTRAIT).scenes.first()

        assertEquals("tadhkirah_opening", opening.id)
        assertEquals(BackgroundType.SOLID_COLOR, opening.background.type)
        assertEquals("إن مع العسر يسرًا", opening.textLayers.first().text)
        assertTrue(opening.textLayers.first().glow)
    }

    @Test
    fun `حقل الآية الفارغ يستخدم placeholder واضح دون اختلاق نص ديني`() {
        val input = TadhkirahTemplateInput(
            openingVerseText = "",
            reflectiveLines = listOf("تفكر"),
            closingLines = listOf("الخاتمة")
        )

        val opening = template.build(input, VideoOrientation.PORTRAIT).scenes.first()

        // placeholder يطلب من المستخدم إدخال الآية — وليس آية مختلقة
        assertEquals("أدخل الآية الكريمة هنا", opening.textLayers.first().text)
    }

    @Test
    fun `مشاهد الانعكاس تأخذ نصوصها من المدخلات بنفس الترتيب`() {
        val input = TadhkirahTemplateInput(
            openingVerseText = "الآية",
            reflectiveLines = listOf("السؤال الأول", "السؤال الثاني"),
            closingLines = listOf("الخاتمة")
        )

        val scenes = template.build(input, VideoOrientation.PORTRAIT).scenes
        val reflections = scenes.filter { it.id.startsWith("tadhkirah_reflection_") }

        assertEquals(2, reflections.size)
        assertEquals("السؤال الأول", reflections[0].textLayers.first().text)
        assertEquals("السؤال الثاني", reflections[1].textLayers.first().text)
    }

    @Test
    fun `مشهد الكلمة المؤثرة يُبنى فقط عند توفّر كلمة مؤثرة`() {
        val withImpact = TadhkirahTemplateInput(
            openingVerseText = "الآية",
            reflectiveLines = listOf("تفكر"),
            impactWord = "عِظام",
            closingLines = listOf("الخاتمة")
        )
        val withoutImpact = withImpact.copy(impactWord = "")

        val withScenes = template.build(withImpact, VideoOrientation.PORTRAIT).scenes
        val withoutScenes = template.build(withoutImpact, VideoOrientation.PORTRAIT).scenes

        assertTrue(withScenes.any { it.id == "tadhkirah_impact" })
        assertTrue(withoutScenes.none { it.id == "tadhkirah_impact" })
        assertEquals(withScenes.size - 1, withoutScenes.size)
    }

    @Test
    fun `مشاهد الخاتمة خلفيتها ليلية داكنة`() {
        val input = TadhkirahTemplateInput(
            openingVerseText = "الآية",
            reflectiveLines = listOf("تفكر"),
            closingLines = listOf("الخاتمة الأولى", "الخاتمة الثانية")
        )

        val closings = template.build(input, VideoOrientation.PORTRAIT).scenes
            .filter { it.id.startsWith("tadhkirah_closing") }

        assertEquals(2, closings.size)
        closings.forEach { scene ->
            assertEquals(BackgroundType.SOLID_COLOR, scene.background.type)
            // لون ليلي داكن (قيم RGB منخفضة جدًا)
            val color = scene.background.colorArgb
            val r = (color shr 16 and 0xFF)
            val g = (color shr 8 and 0xFF)
            val b = (color and 0xFF)
            assertTrue("$r,$g,$b يجب أن تكون داكنة", r < 20 && g < 20 && b < 25)
        }
    }

    @Test
    fun `الشعار يُضاف كطبقة نص في أعلى المشاهد عند توفّر اسم العلامة`() {
        val input = TadhkirahTemplateInput(
            openingVerseText = "الآية",
            reflectiveLines = listOf("تفكر"),
            closingLines = listOf("الخاتمة"),
            brandName = "قبس"
        )

        val opening = template.build(input, VideoOrientation.PORTRAIT).scenes.first()
        val logoLayer = opening.textLayers.firstOrNull { it.text == "قبس" }

        assertTrue(logoLayer != null)
        assertEquals(LayerVerticalAlignment.TOP, logoLayer!!.verticalAnchor)
    }

    @Test
    fun `fromProject يستخرج النصوص من مشاهد الخطة والنصوص المقترحة دون اختراع`() {
        val project = VideoProject(
            idea = VideoIdea(editingStyle = EditingStyle.MEDITATIVE),
            plan = VideoPlan(
                scenes = listOf(
                    VideoScene(onScreenText = "كل نفس ذائقة الموت"),
                    VideoScene(onScreenText = "فأين تذهبون")
                ),
                suggestedTexts = listOf("السؤال المقترح")
            )
        )

        val input = template.fromProject(project)

        assertEquals("كل نفس ذائقة الموت", input.openingVerseText)
        // العنصر الثاني من المشهد، والعنصر المقترح يأتي تاليًا
        assertEquals(2, input.reflectiveLines.size)
        assertEquals("فأين تذهبون", input.reflectiveLines.first())
        assertEquals("قبس", input.brandName)
    }

    @Test
    fun `build من مشروع فارغ يولّد مشهدًا واحدًا على الأقل ولا يرمي`() {
        val project = VideoProject(
            idea = VideoIdea(editingStyle = EditingStyle.MEDITATIVE, ideaText = ""),
            plan = VideoPlan(scenes = emptyList(), suggestedTexts = emptyList())
        )

        val storyboard = template.build(project)

        assertTrue(storyboard.scenes.isNotEmpty())
        // الافتتاح دائمًا موجود ويستخدم placeholder لعدم توفر نص
        assertEquals("أدخل الآية الكريمة هنا", storyboard.scenes.first().textLayers.first().text)
    }

    @Test
    fun `المشهد الانعكاسي الأول يستخدم حركة التوهج النابض`() {
        val input = TadhkirahTemplateInput(
            openingVerseText = "الآية",
            reflectiveLines = listOf("السؤال الأول", "السؤال الثاني"),
            closingLines = listOf("الخاتمة")
        )

        val reflections = template.build(input, VideoOrientation.PORTRAIT).scenes
            .filter { it.id.startsWith("tadhkirah_reflection_") }

        assertEquals(TextAnimation.GLOW_PULSE, reflections[0].textLayers.first().animation)
        assertEquals(TextAnimation.FADE_IN, reflections[1].textLayers.first().animation)
    }

    @Test
    fun `مع مزوّد NoOp لا توجد طبقات زخرفية في مشهد الافتتاح`() {
        val input = TadhkirahTemplateInput(
            openingVerseText = "الآية",
            reflectiveLines = listOf("تفكر"),
            closingLines = listOf("الخاتمة")
        )

        val opening = template.build(input, VideoOrientation.PORTRAIT).scenes.first()

        assertTrue(opening.overlayLayers.isEmpty())
        // طبقة الآية + طبقة الشعار = 2
        assertEquals(2, opening.textLayers.size)
    }

    @Test
    fun `fromProject يستخدم نص الفكرة كعبارة تأملية احتياطية عند غياب النصوص`() {
        val project = VideoProject(
            idea = VideoIdea(editingStyle = EditingStyle.MEDITATIVE, ideaText = "تذكرة بالموت والآخرة"),
            plan = VideoPlan(scenes = emptyList(), suggestedTexts = emptyList())
        )

        val input = template.fromProject(project)

        // الآية الافتتاحية تبقى فارغة (placeholder) لأن نص الفكرة ليس آية
        assertEquals("", input.openingVerseText)
        // لكنه يُستخدم كعبارة تأملية احتياطية
        assertEquals(listOf("تذكرة بالموت والآخرة"), input.reflectiveLines)
    }
}
