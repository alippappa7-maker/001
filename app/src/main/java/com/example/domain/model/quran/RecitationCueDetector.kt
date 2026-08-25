package com.example.domain.model.quran

/**
 * يبني [PhraseTimeline] من [RecitationTimeline] بتجميع الكلمات في عبارات
 * وفق فجوات الصمت الفعلية بين نهاية كلمة وبداية التالية — لا وفق أي تحليل
 * لمعنى النص. هذا هو "قارئ إيقاع الوقفات" في وضع قبس الذكي.
 *
 * القاعدة: فجوة صمت بين كلمتين أكبر من أو تساوي [WAQFA_GAP_THRESHOLD_MS]
 * تُعامل كوقفة حقيقية (نهاية عبارة)، فتبدأ عبارة جديدة بعدها. الفجوات
 * الأصغر (فواصل نطق طبيعية بين الكلمات المتتالية) لا تكسر العبارة.
 *
 * هذا الحد (250ms) مبني على أن أقصر كلمة مرصودة (نحو 200-600ms) تفصلها
 * فجوات طبيعية أصغر من ذلك عادة، بينما الوقفات الفعلية عند القارئين
 * الرسميين (كالعفاسي) تتجاوزها بوضوح. يمكن ضبطه لاحقًا تجريبيًا لكل قارئ
 * إن ثبتت الحاجة، لكنه ثابت افتراضي معقول للبدء.
 */
object RecitationCueDetector {

    /** الحد الأدنى (بالميلي ثانية) لفجوة الصمت لتُعامل كوقفة حقيقية. */
    const val WAQFA_GAP_THRESHOLD_MS = 250L

    /**
     * يبني توزيع العبارات لخط زمني كامل. لا يمكن أن يفشل لأن
     * [RecitationTimeline] يضمن بالفعل أن words غير فارغة (تحقق شرعي
     * سابق في init الخاص بها).
     */
    fun detect(
        timeline: RecitationTimeline,
        gapThresholdMs: Long = WAQFA_GAP_THRESHOLD_MS
    ): PhraseTimeline {
        val words = timeline.words
        val phrases = mutableListOf<RecitationPhrase>()

        var phraseStart = 0
        for (i in 1 until words.size) {
            val gap = words[i].startMs - words[i - 1].endMs
            if (gap >= gapThresholdMs) {
                phrases += buildPhrase(words, phraseStart, i - 1)
                phraseStart = i
            }
        }
        phrases += buildPhrase(words, phraseStart, words.lastIndex)

        return PhraseTimeline(timeline = timeline, phrases = phrases)
    }

    private fun buildPhrase(
        words: List<TimedQuranWord>,
        startIndex: Int,
        endIndex: Int
    ): RecitationPhrase = RecitationPhrase(
        words = words.subList(startIndex, endIndex + 1),
        startIndex = startIndex,
        endIndex = endIndex
    )
}
