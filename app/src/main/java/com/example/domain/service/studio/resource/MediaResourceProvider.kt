package com.example.domain.service.studio.resource

import com.example.domain.model.studio.ResourceIntent

/**
 * مورد بصري محلّي جاهز للاستخدام في خلفية المشهد.
 *
 * إمّا ملف محلي (صورة/فيديو مُنزّل أو اختاره المستخدم)،
 * أو لون صلب احتياطي عندما لا يتوفّر أي مورد.
 */
sealed class MediaResource {
    /** صورة محلية جاهزة كخلفية. */
    data class LocalImage(val path: String, val width: Int = 0, val height: Int = 0) : MediaResource()

    /** فيديو محلي جاهز كخلفية. */
    data class LocalVideo(val path: String, val durationMs: Long = 0L) : MediaResource()

    /** لا يوجد مورد — استخدم لونًا صلبًا. */
    data class SolidColor(val colorArgb: Int) : MediaResource()

    /** هل هذا مورد حقيقي (صورة/فيديو) أم احتياطي لوني؟ */
    val isRealAsset: Boolean
        get() = this is LocalImage || this is LocalVideo
}

/**
 * مزود موارد بصريّة. يأخذ [ResourceIntent] ويحاول جلب مورد مناسب.
 *
 * التطبيقات:
 * - [LocalResourceProvider]: يفحص وسائط اختارها المستخدم محليًا.
 * - [StockResourceProvider]: يبحث في Pexels/Pixabay (يتطلب إنترنت/مفتاح).
 * - [CompositeResourceProvider]: يدمج عدة مزودات بسلسلة fallback.
 *
 * جميع الدوال معلّقة لأن المزودات الخارجية قد تحتاج شبكة/إدخال-إخراج.
 */
interface MediaResourceProvider {
    /** هل هذا المزود متاح الآن (للمزودات الخارجية: هل يوجد مفتاح وإنترنت)؟ */
    val isAvailable: Boolean

    /** اسم المزود لأغراض التشخيص. */
    val displayName: String

    /**
     * يحاول جلب مورد مطابق للنيّة. إن لم يجد، يرجع null
     * (ليتابع المزود التالي في السلسلة).
     */
    suspend fun resolve(intent: ResourceIntent): MediaResource?
}
