package com.example.domain.service.studio.resource

import com.example.domain.model.studio.ResourceIntent
import com.example.domain.model.studio.VideoAsset
import com.example.domain.model.studio.VideoProject
import com.example.domain.model.studio.AssetType
import com.example.domain.model.studio.VisualCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختبارات سلسلة fallback للموارد وطبقة التجريد.
 * كلها خالصة (بدون Android) — تتحقق من منطق الترتيب والاحتياطي.
 */
class ResourceProviderTest {

    @Test
    fun `مزود وهمي غير متاح يُتجاوز في السلسلة`() {
        val unavailable = StubProvider(available = false, result = null)
        val available = StubProvider(available = true, result = MediaResource.LocalImage("/img.png"))
        val composite = CompositeResourceProvider(listOf(unavailable, available))

        val intent = intent()
        val result = kotlinx.coroutines.runBlocking { composite.resolve(intent) }

        assertTrue(result is MediaResource.LocalImage)
        assertEquals("/img.png", (result as MediaResource.LocalImage).path)
    }

    @Test
    fun `لا يوجد مورد حقيقي فيرجع لون صلب احتياطي`() {
        val composite = CompositeResourceProvider(emptyList())

        val intent = intent(fallbackColor = 0xFF112233.toInt())
        val result = kotlinx.coroutines.runBlocking { composite.resolve(intent) }

        assertTrue(result is MediaResource.SolidColor)
        assertEquals(0xFF112233.toInt(), (result as MediaResource.SolidColor).colorArgb)
    }

    @Test
    fun `مزود يرجع null يتابع للمزود التالي`() {
        val empty = StubProvider(available = true, result = null)
        val real = StubProvider(available = true, result = MediaResource.LocalVideo("/v.mp4", 5000L))
        val composite = CompositeResourceProvider(listOf(empty, real))

        val intent = intent()
        val result = kotlinx.coroutines.runBlocking { composite.resolve(intent) }

        assertTrue(result is MediaResource.LocalVideo)
    }

    @Test
    fun `المزود المحلي يطابق أصل المستخدم المرفق`() {
        val asset = VideoAsset(id = "a1", type = AssetType.IMAGE, isLocal = true, sourceUrlOrPath = "/local/img.jpg")
        val project = VideoProject(assets = listOf(asset))
        val provider = LocalResourceProvider(project)

        val intent = ResourceIntent(
            category = VisualCategory.NATURE,
            attachedAssetId = "a1",
            fallbackColorArgb = 0xFF000000.toInt()
        )
        val result = kotlinx.coroutines.runBlocking { provider.resolve(intent) }

        assertTrue(result is MediaResource.LocalImage)
        assertEquals("/local/img.jpg", (result as MediaResource.LocalImage).path)
    }

    @Test
    fun `المزود المحلي يرجع null عند عدم وجود أصل مطابق`() {
        val project = VideoProject(assets = emptyList())
        val provider = LocalResourceProvider(project)

        val intent = ResourceIntent(category = VisualCategory.NATURE, attachedAssetId = "missing")
        val result = kotlinx.coroutines.runBlocking { provider.resolve(intent) }

        assertNull(result)
    }

    @Test
    fun `مزود خارجي بدون مفتاح يعتبر غير متاح`() {
        val provider = PexelsResourceProvider(
            apiKeyProvider = { "none" },
            cache = StockMediaCache(java.io.File(System.getProperty("java.io.tmpdir"), "test_cache"))
        )
        assertFalse(provider.isAvailable)
    }

    @Test
    fun `أسماء المزودات المتاحة فقط`() {
        val a = StubProvider(available = true, name = "أول")
        val b = StubProvider(available = false, name = "ثانٍ")
        val composite = CompositeResourceProvider(listOf(a, b))

        assertEquals(listOf("أول"), composite.availableProviderNames())
    }

    private fun intent(fallbackColor: Int = 0xFF0B1020.toInt()): ResourceIntent =
        ResourceIntent(
            category = VisualCategory.CINEMATIC_NIGHT,
            fallbackColorArgb = fallbackColor
        )

    /** مزود وهمي للاختبارات — يرجع نتيجة ثابتة. */
    private class StubProvider(
        private val available: Boolean,
        private val name: String = "وهمي",
        private val result: MediaResource? = null
    ) : MediaResourceProvider {
        override val isAvailable = available
        override val displayName = name
        override suspend fun resolve(intent: ResourceIntent): MediaResource? = result
    }
}
