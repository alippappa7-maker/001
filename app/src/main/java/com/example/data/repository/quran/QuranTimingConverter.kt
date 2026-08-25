package com.example.data.repository.quran

import com.example.data.api.model.quran.VerseTimingDto
import com.example.domain.model.quran.TimedQuranWord

/**
 * يحوّل توقيتات الكلمات الخام من واجهة QuranCDN (إزاحات داخل ملف السورة الكاملة)
 * إلى توقيتات نسبية لبداية ملف الآية المعزول، ويطابقها بكلمات الآية.
 *
 * هذا الكائن منفصل عن [QuranRepository] عمدًا: لا يحتاج Context أو Android،
 * لذا يمكن اختباره كاختبار JVM صرف، ونتأكد من صحة التحويل الزمني (القلب
 * التقني للميزة) دون الاعتماد على الشبكة أو أجهزة أندرويد.
 */
object QuranTimingConverter {

    /**
     * يبني قائمة [TimedQuranWord] من DTO الكلمات وDTO التوقيت.
     *
     * قواعد التحقق (مهمة شرعياً):
     * 1. كل كلمة يجب أن يكون لها توقيت مطابق. لو نقص توقيت كلمة واحدة،
     *    نُرجع null بالكامل بدل إصدار توقيت ناقص يخطئ المستخدم في التلاوة.
     * 2. بعض segments النهائية قد تأتي بعنصرين فقط (بدون نهاية)؛ نعاملها
     *    كأن نهايتها = نهاية الآية (timestamp_to).
     * 3. نطرح timestampFrom من كل توقيت لجعله نسبيًا لبداية ملف الآية المعزول،
     *    ثم نضمن ألا يكون أقل من صفر.
     *
     * @param wordTexts نصوص الكلمات بالترتيب (position 1-based).
     * @param verseTiming DTO التوقيت الخام من QuranCDN.
     */
    fun buildTimedWords(
        wordTexts: List<Pair<Int, String>>, // (position, text)
        verseTiming: VerseTimingDto
    ): List<TimedQuranWord>? {
        val verseStart = verseTiming.timestampFrom
        val segments = parseSegments(verseTiming, verseStart)

        // التحقق الشرعي الصارم: توقيت كل كلمة إجباري، ولا توجد كلمات بلا
        // توقيت. أي عدم تطابق يبطل التصدير بالكامل. نرفض أيضًا الحالات التي يزيد
        // فيها عدد التوقيتات عن عدد الكلمات (عنصر زائد في المصدر).
        if (segments.keys != wordTexts.map { it.first }.toSet()) return null

        val result = mutableListOf<TimedQuranWord>()
        for ((position, text) in wordTexts) {
            val (start, end) = segments[position] ?: return null
            result += TimedQuranWord(
                position = position,
                text = text,
                startMs = start,
                endMs = end
            )
        }
        return if (result.size == wordTexts.size) result else null
    }

    /**
     * يحوّل segments الخام إلى خريطة position -> (start, end) نسبية لبداية الآية.
     */
    private fun parseSegments(
        verseTiming: VerseTimingDto,
        verseStart: Long
    ): Map<Int, Pair<Long, Long>> {
        val map = mutableMapOf<Int, Pair<Long, Long>>()
        for (seg in verseTiming.segments) {
            if (seg.size < 2) continue
            val position = seg[0].toInt()
            val startMs = (seg[1] - verseStart).coerceAtLeast(0)
            val endMs = if (seg.size >= 3) {
                (seg[2] - verseStart).coerceAtLeast(startMs)
            } else {
                (verseTiming.timestampTo - verseStart).coerceAtLeast(startMs)
            }
            map[position] = startMs to endMs
        }
        return map
    }
}
