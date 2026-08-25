package com.example.domain.service.studio.template

import android.graphics.Bitmap

/**
 * يوفر طبقات زخرفية مرسومة برمجيًا (دوائر متداخلة، نجمة، زوايا مزخرفة) كـ Bitmap شفاف.
 *
 * الفصل عبر واجهة يسمح بالعمل بدون [android.content.Context] في الاختبارات عبر
 * [NoOpOrnamentalFrameProvider] التي تعيد null (فيُتجاهل الرسم الزخرفي)،
 * بينما يُستخدم [CanvasOrnamentalFrameProvider] في الإنتاج لرسم زخرفة حقيقية.
 */
interface OrnamentalFrameProvider {

    /**
     * يرسم الإطار الزخرفي حول بطاقة الآية بحجم يناسب عرض الفيديو المُعطى.
     * يعيد Bitmap شفافًا (بأبعاد مستقلة يختارها المزوّد) أو null إن لم يكن الرسم متاحًا.
     */
    fun renderVerseFrame(videoWidthPx: Int, videoHeightPx: Int): Bitmap?
}

/** مزوّد لا يرسم شيئًا — يُستخدم في الاختبارات وكمزوّد افتراضي آمن. */
object NoOpOrnamentalFrameProvider : OrnamentalFrameProvider {
    override fun renderVerseFrame(videoWidthPx: Int, videoHeightPx: Int): Bitmap? = null
}
