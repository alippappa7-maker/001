package com.example.domain.model.quran

/**
 * كلمة واحدة من آية مع نطاقها الزمني داخل ملف الصوت (بالميلي ثانية)،
 * منسوبة إلى بداية ملف الآية المعزول (لا ملف السورة الكاملة).
 *
 * المصدر: واجهة QuranCDN (api.qurancdn.com) تُرجع segments كإزاحات داخل ملف
 * السورة الكاملة؛ نطرح verseTimestampFromMs عند البناء لتصبح نسبية للآية.
 * راجع [QuranRepository] لتفاصيل هذا التحويل.
 */
data class TimedQuranWord(
    val position: Int, // 1-based، يطابق position في مصفوفة words من verses/by_key
    val text: String, // النص العثماني للكلمة (من words[].text_uthmani)
    val startMs: Long, // بداية نطق الكلمة، نسبية لبداية ملف الآية
    val endMs: Long // نهاية نطق الكلمة، نسبية لبداية ملف الآية
) {
    init {
        require(position >= 1) { "position must be >= 1" }
        require(endMs >= startMs) { "endMs must be >= startMs" }
    }

    fun contains(timeMs: Long): Boolean = timeMs in startMs until endMs
}

/**
 * خط زمني كامل لتلاوة آية واحدة: النص الكامل + كلماتها المؤقتة + رابط الصوت
 * المعزول لهذه الآية وحدها (وليس السورة كاملة).
 */
data class RecitationTimeline(
    val verseKey: String, // مثل "2:255"
    val textUthmani: String, // النص الكامل للآية (من verse.text_uthmani)
    val reciterId: Int,
    val reciterName: String,
    val verseAudioUrl: String, // رابط MP3 معزول لهذه الآية فقط
    val durationMs: Long, // مدة ملف الآية المعزول
    val words: List<TimedQuranWord>
) {
    init {
        require(words.isNotEmpty()) { "words must not be empty" }
        require(durationMs > 0) { "durationMs must be positive" }
    }

    /**
     * يعيد فهرس الكلمة النشطة (position) عند لحظة زمنية معينة داخل التلاوة،
     * أو null إن كانت اللحظة قبل أول كلمة أو بعد آخر كلمة (فجوات صمت).
     */
    fun activeWordPositionAt(timeMs: Long): Int? {
        // البحث الخطي مقبول لأن عدد كلمات الآية صغير جدًا (نادرًا يتجاوز 30).
        val exact = words.firstOrNull { it.contains(timeMs) }
        if (exact != null) return exact.position

        // في الفجوات الصغيرة بين الكلمات (فواصل صمت طبيعية)، نبقي آخر كلمة
        // منتهية مظلّلة حتى بداية الكلمة التالية، بدل إطفاء التظليل لحظيًا.
        val lastEndedBefore = words.lastOrNull { it.endMs <= timeMs }
        val nextStartsAfter = words.firstOrNull { it.startMs > timeMs }
        return when {
            lastEndedBefore == null -> null // قبل أول كلمة
            nextStartsAfter == null -> lastEndedBefore.position // بعد آخر كلمة
            else -> lastEndedBefore.position // في فجوة بين كلمتين
        }
    }
}
