package com.example.domain.model.studio

/**
 * نيّة مورد بصري يطلبها القالب لخلفية المشهد.
 *
 * بدل أن يعرف القالب عن Pexels أو Pixabay، يصف فقط «ماذا يريد»:
 * فئة بصريّة + كلمات مفتاحية + مدّة الفيديو المطلوبة (إن كانت).
 *
 * تُترجم هذه النيّة لاحقًا إلى استعلام عن مزود الموارد (محلي/خارجي)،
 * فإن لم يُوجد مورد، يرجع القالب إلى لون صلب عبر [fallbackColorArgb].
 */
data class ResourceIntent(
    /** فئة المحتوى البصرية العامة (سماء، طبيعة، مدينة، تعليمي...). */
    val category: VisualCategory,
    /** كلمات مفتاحية بالعربية/الإنجليزية لصقل البحث. */
    val keywords: List<String> = emptyList(),
    /** مدّة الفيديو المطلوبة بالملي ثانية (للخلفيات الفيديوية). */
    val durationMs: Long = 0L,
    /** لون احتياطي إن لم يُوجد أي مورد. */
    val fallbackColorArgb: Int = 0xFF0B1020.toInt(),
    /** هل يُفضَّل الفيديو المتحرك على الصورة الثابتة؟ */
    val preferMotion: Boolean = false,
    /**
     * معرّف أصل محلي أرفقه المستخدم بالمشهد (من project.assets).
     * إن وُجد، يُفضَّل على البحث الخارجي.
     */
    val attachedAssetId: String? = null
)

/**
 * فئات بصرية عامة تربط نمط التحرير بنوع المحتوى المناسب.
 * كل فئة تُترجم إلى كلمات بحث للمزودات الخارجية.
 */
enum class VisualCategory(val searchTerms: List<String>, val fallbackColor: Int) {
    CINEMATIC_NIGHT(
        searchTerms = listOf("night sky", "stars", "cinematic dark", "moon"),
        fallbackColor = 0xFF0B1020.toInt()
    ),
    NATURE(
        searchTerms = listOf("nature", "mountains", "forest", "landscape"),
        fallbackColor = 0xFF10241F.toInt()
    ),
    EDUCATION(
        searchTerms = listOf("books", "study", "knowledge", "library"),
        fallbackColor = 0xFF0F1A2E.toInt()
    ),
    FAST_URBAN(
        searchTerms = listOf("city", "urban", "fast motion", "lights"),
        fallbackColor = 0xFF1A1230.toInt()
    ),
    ABSTRACT_GOLD(
        searchTerms = listOf("abstract", "gold", "particles", "bokeh"),
        fallbackColor = 0xFF1A1408.toInt()
    ),
    PAPER_TEXTURE(
        searchTerms = listOf("paper", "texture", "vintage", "minimal"),
        fallbackColor = 0xFFF5F0E8.toInt()
    ),
    COMPASS_QIBLA(
        searchTerms = listOf("mecca", "kaaba", "mosque", "islamic"),
        fallbackColor = 0xFF0B1416.toInt()
    );

    companion object {
        /** يختار فئة بصريّة مناسبة لنمط تحرير معيّن. */
        fun forEditingStyle(style: EditingStyle): VisualCategory = when (style) {
            EditingStyle.CINEMATIC -> CINEMATIC_NIGHT
            EditingStyle.DOCUMENTARY -> PAPER_TEXTURE
            EditingStyle.EDUCATIONAL -> EDUCATION
            EditingStyle.FAST_REELS -> FAST_URBAN
            EditingStyle.SHORT_AD -> FAST_URBAN
            EditingStyle.ANIMATION -> ABSTRACT_GOLD
            EditingStyle.MEDITATIVE -> NATURE
            EditingStyle.STORYTELLING -> NATURE
            EditingStyle.MOVING_QUOTES -> CINEMATIC_NIGHT
            EditingStyle.QURAN_RECITATION -> COMPASS_QIBLA
        }
    }
}
