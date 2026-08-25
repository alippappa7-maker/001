@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class]
)

package com.example.domain.model.studio

import android.graphics.Bitmap

/**
 * نماذج محرك التركيب (Composition Engine).
 *
 * تفصل بين "وصف المشهد بصريًا" (هذه النماذج) و "كيفية الرندر" (المحرك).
 * المحرك يأخذ [CompositionStoryboard] ويحوّله إلى ملف MP4 عبر Media3 Transformer.
 */

enum class LayerHorizontalAlignment { START, CENTER, END }
enum class LayerVerticalAlignment { TOP, CENTER, BOTTOM }

/**
 * أنواع الحركة البسيطة المدعومة للنص. متعمد أن تكون بسيطة وحقيقية التنفيذ
 * (لا تعتمد على توليد ذكاء اصطناعي للفيديو، بل على تحريك طبقات حقيقية).
 */
enum class TextAnimation(val durationMs: Long) {
    NONE(0),
    FADE_IN(800),
    SLIDE_UP(700),
    GLOW_PULSE(1200),
    TYPEWRITER(0); // المدة تُحسب من طول النص في البناء

    val arLabel: String
        get() = when (this) {
            NONE -> "بدون"
            FADE_IN -> "ظهور تدريجي"
            SLIDE_UP -> "صعود من الأسفل"
            GLOW_PULSE -> "توهج نابض"
            TYPEWRITER -> "كتابة حرفية"
        }
}

/**
 * طبقة نص واحدة. النص يُرسم كـ Bitmap عبر [TextBitmapRenderer] لضمان
 * تشكيل الحروف العربية والاتجاه RTL الصحيح، ثم يُركّب فوق الفيديو.
 */
data class TextLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val fontSizeSp: Int = 42,
    val textColorArgb: Int = 0xFFFFFFFF.toInt(),
    val glow: Boolean = false,
    val glowColorArgb: Int = 0xFFFFFFFF.toInt(),
    val alignment: LayerHorizontalAlignment = LayerHorizontalAlignment.CENTER,
    val verticalAnchor: LayerVerticalAlignment = LayerVerticalAlignment.CENTER,
    val yOffsetPercent: Float = 0f, // إزاحة عمودية نسبية (-1..1) من نقطة الإرساء
    val marginPercent: Float = 0.08f, // هامش جانبي نسبي من عرض الإطار
    val maxLines: Int = 6,
    val animation: TextAnimation = TextAnimation.FADE_IN,
    val animationStartMs: Long = 0L,
    val animationDurationMs: Long = animation.durationMs.coerceAtLeast(1L)
) {
    init {
        require(marginPercent in 0f..0.45f) { "marginPercent must be in 0..0.45" }
        require(yOffsetPercent in -1f..1f) { "yOffsetPercent must be in -1..1" }
    }
}

/**
 * طبقة صورة شفافة تُركّب فوق الخلفية (مثل الإطار الزخرفي حول الآية، أو شعار).
 */
data class ImageOverlayLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bitmap: Bitmap,
    val widthPercent: Float = 1f, // نسبة العرض من عرض الفيديو (0..1)
    val alignment: LayerHorizontalAlignment = LayerHorizontalAlignment.CENTER,
    val verticalAnchor: LayerVerticalAlignment = LayerVerticalAlignment.CENTER,
    val yOffsetPercent: Float = 0f,
    val alpha: Float = 1f
) {
    init {
        require(widthPercent in 0f..1f) { "widthPercent must be in 0..1" }
        require(alpha in 0f..1f) { "alpha must be in 0..1" }
    }
}

enum class BackgroundType { SOLID_COLOR, IMAGE, VIDEO }

/**
 * خلفية المشهد. إما لون ثابت، أو صورة ثابتة، أو مقطع فيديو.
 * الصورة الثابتة تُمدد لتغطي مدة المشهد عبر إعداد المدة في MediaItem.
 */
data class BackgroundLayer(
    val type: BackgroundType = BackgroundType.SOLID_COLOR,
    val colorArgb: Int = 0xFF0B1020.toInt(),
    val staticImage: Bitmap? = null,
    val videoUri: String? = null,
    /** مسار ملف صورة محلي (مُنزّل أو اختاره المستخدم) — يُستخدم عند type=IMAGE. */
    val imageUri: String? = null
) {
    fun isUsable(): Boolean = when (type) {
        BackgroundType.SOLID_COLOR -> true
        BackgroundType.IMAGE -> staticImage != null || !videoUri.isNullOrBlank() || !imageUri.isNullOrBlank()
        BackgroundType.VIDEO -> !videoUri.isNullOrBlank()
    }
}

/**
 * مشهد واحد في لوحة القصة: خلفية + طبقات نص/صورة + مدة + انتقال.
 *
 * @param dynamicOverlay overlay خام يتبدّل محتواه حسب presentationTimeUs
 * (مثل [SyncedAyahOverlay] لمزامنة الكلمات مع التلاوة). يُركَّب فوق كل
 * الطبقات الأخرى في المشهد. اختياري ولا يتعارض مع textLayers/overlayLayers.
 */
data class CompositionScene(
    val id: String = java.util.UUID.randomUUID().toString(),
    val durationMs: Long = 3000L,
    val background: BackgroundLayer = BackgroundLayer(),
    val textLayers: List<TextLayer> = emptyList(),
    val overlayLayers: List<ImageOverlayLayer> = emptyList(),
    val transitionMs: Long = 400L,
    val dynamicOverlay: androidx.media3.effect.BitmapOverlay? = null,
    /**
     * نيّة مورد بصري يطلبها القالب لهذا المشهد. تُحلّ لاحقًا بواسطة
     * [com.example.domain.service.studio.resource.SceneResourceResolver]:
     * إن وُجد مورد محلي/خارجي مطابق تُستبدل الخلفية اللونية بصورة/فيديو،
     * وإلا تبقى الخلفية اللونية الاحتياطية.
     */
    val resourceIntent: ResourceIntent? = null
) {
    init {
        require(durationMs > 0) { "durationMs must be positive" }
    }
}

/**
 * لوحة القصة الكاملة القابلة للرندر: أبعاد + معدل إطارات + مشاهد + صوت اختياري.
 */
data class CompositionStoryboard(
    val width: Int = 720,
    val height: Int = 1280,
    val fps: Int = 30,
    val scenes: List<CompositionScene>,
    val audioUri: String? = null
) {
    init {
        require(width > 0 && height > 0) { "dimensions must be positive" }
        require(fps in 1..60) { "fps must be in 1..60" }
        require(scenes.isNotEmpty()) { "storyboard must have at least one scene" }
    }

    val totalDurationMs: Long
        get() = scenes.sumOf { it.durationMs }

    val textLayerCount: Int
        get() = scenes.sumOf { it.textLayers.size }
}
