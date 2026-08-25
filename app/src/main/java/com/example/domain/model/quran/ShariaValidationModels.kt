package com.example.domain.model.quran

/**
 * بطاقة المصدر الموثّقة: تُحفظ مع كل فيديو قرآني وتُعرض داخل الفيديو نفسه
 * (وليس فقط في الوصف) لضمان وضوح نسبة التلاوة إلى مصدرها الأصلي — وهي
 * قاعدة شرعية صارمة في تطبيق قبس.
 *
 * الهدف: لا يُصدَّر أي فيديو قرآني دون بطاقة مصدر كاملة موثّقة من بيانات
 * API الفعلية (لا يدخُلها أي بيانات يُدخلها المستخدم يدويًا أو يُخترعها
 * التطبيق). أي حقل ناقص يبطل التصدير بالكامل.
 */
data class SourceCard(
    val reciterName: String,
    val reciterStyle: String,
    val verseKey: String,
    val recitationSourceUrl: String,
    val textSourceUrl: String,
    val timingsSourceUrl: String
) {
    init {
        require(reciterName.isNotBlank()) { "reciterName must not be blank" }
        require(verseKey.isNotBlank()) { "verseKey must not be blank" }
        require(recitationSourceUrl.startsWith("https://")) { "recitation source must be https" }
        require(textSourceUrl.startsWith("https://")) { "text source must be https" }
        require(timingsSourceUrl.startsWith("https://")) { "timings source must be https" }
    }

    /** نسخة جاهزة للعرض النصي البسيط داخل الفيديو. */
    fun displayText(): String =
        "$verseKey — $reciterName ($reciterStyle)"
}

/**
 * تقرير التحقق الشرعي قبل التصدير. كل خطأ هنا يمنع التصدير تمامًا —
 * لا يُصدَّر فيديو قرآني ببيانات ناقصة أو توقيت مكسور أو بلا ملف صوت
 * فعلي على القرص أبدًا.
 */
data class ShariaValidationReport(
    val timelineStructurallyValid: Boolean,
    val audioFileExists: Boolean,
    val audioDurationMatchesTimeline: Boolean,
    val sourceCardPresent: Boolean
) {
    /** صحيح فقط إذا اجتاز كل القيود الشرعية معًا. */
    val isValid: Boolean =
        timelineStructurallyValid &&
            audioFileExists &&
            audioDurationMatchesTimeline &&
            sourceCardPresent

    /** قائمة بالأخطاء الموجودة (لعرضها على المستخدم أو تسجيلها). */
    val errors: List<String>
        get() = buildList {
            if (!timelineStructurallyValid) add("الخط الزمني للتلاوة غير سليم البنية")
            if (!audioFileExists) add("ملف الصوت المعزول غير موجود على القرص")
            if (!audioDurationMatchesTimeline) add("مدة ملف الصوت لا تطابق توقيت الكلمات")
            if (!sourceCardPresent) add("بطاقة المصدر غائبة أو ناقصة")
        }
}

/**
 * ينفّذ التحقق الشرعي الفعلي قبل التصدير. يُستدعى من محرك التصدير مباشرة
 * قبل بدء [com.example.domain.service.studio.StudioCompositionEngine.compose]
 * لأي مشروع قرآني — أي فشل هنا يجب أن يمنع الاستدعاء بالكامل، لا أن يُظهر
 * تحذيرًا فقط.
 *
 * ملاحظة: كثير من هذه الشروط مضمونة أصلاً عبر `init` الصارم في
 * [RecitationTimeline] و[TimedQuranWord] (لا يمكن بناء الكائن أساسًا إن
 * كانت البيانات فاسدة) — لكن هذا المدقق يعيد التحقق صريحًا في نقطة
 * التصدير نفسها، لأن الكائن قد يُبنى في لحظة والتصدير يحدث في لحظة لاحقة
 * (بعد تنقّل المستخدم بين الشاشات)، فلا نعتمد فقط على أن البناء الأصلي
 * كان سليمًا.
 */
object ShariaExportValidator {

    /** هامش القبول بين مدة الصوت الفعلية وتوقيت آخر كلمة (بالميلي ثانية). */
    private const val AUDIO_DURATION_TOLERANCE_MS = 500L

    fun validate(
        timeline: RecitationTimeline,
        audioFile: java.io.File?,
        actualAudioDurationMs: Long?,
        sourceCard: SourceCard?
    ): ShariaValidationReport {
        val timelineValid = timeline.words.isNotEmpty() &&
            timeline.words.all { it.endMs >= it.startMs && it.position >= 1 } &&
            timeline.durationMs > 0

        val fileExists = audioFile != null && audioFile.exists() && audioFile.length() > 0L

        val durationMatches = if (actualAudioDurationMs == null) {
            false
        } else {
            kotlin.math.abs(actualAudioDurationMs - timeline.durationMs) <= AUDIO_DURATION_TOLERANCE_MS
        }

        return ShariaValidationReport(
            timelineStructurallyValid = timelineValid,
            audioFileExists = fileExists,
            audioDurationMatchesTimeline = durationMatches,
            sourceCardPresent = sourceCard != null
        )
    }
}
