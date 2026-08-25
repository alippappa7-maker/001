package com.example.domain.model.quran

/**
 * درجة التظليل البصري لكلمة واحدة داخل الآية أثناء عرضها في فيديو
 * مزامنة كلمة بكلمة — وهو جوهر "وضع قبس الذكي".
 *
 * بدل أن نضيء الكلمة النشطة فقط بنفس الشدّة، نقسّم الكلمات بصريًا إلى ثلاث
 * طبقات بحسب إيقاع التلاوة، فيبدو الفيديو مصمَّمًا على نفس التلاوة لا مجرد
 * متزامن آلي:
 *
 * 1. [ACTIVE]   الكلمة التي تُنطق الآن — أشد وضوحًا (لون ذهبي + توهّج).
 * 2. [CURRENT]  العبارة الحالية (مجموعة الكلمات حول الكلمة النشطة داخل
 *                نفس الوقفة) — أوضح من الخلفية لكن أقل من النشطة.
 * 3. [PAST]     ما سبق نطقه من العبارة أو من الآية — أخف بصريًا.
 *
 * الترتيب من الأقل حِدّة إلى الأشد، يُستخدم لاختيار درجة الشفافية/اللون
 * في [SyncedAyahOverlay] ولم يكن ليتحقق دون هذا التقسيم.
 *
 * لاحظ: التقسيم لا يأتي من "فهم معنى الآية" أبدًا (هذا ممنوع شرعًا)،
 * بل من **قراءة الفجوات الزمنية بين الكلمات فقط** — وهي قراءة لغوية
 * صرف للتوقيت الصوتي، لا تأويل للنص.
 */
enum class WordVisualState {
    /** الكلمة الجاري نطقها الآن في هذه اللحظة. أشد وضوحًا. */
    ACTIVE,

    /** داخل العبارة الحالية (نفس الوقفة) لكنها ليست الكلمة النشطة. */
    CURRENT,

    /** نُطقت بالفعل (ماضية) — تُخفت بصريًا. */
    PAST
}

/**
 * إيقاع الوقفات: مجموعة كلمات مترابطة بصريًا ضمن فترة صوتية متواصلة
 * تفصلها فجوة صمت أكبر من حدّ [RecitationCueDetector.WAQFA_GAP_THRESHOLD_MS]
 * عن المجموعة التالية.
 *
 * الفكرة من "وضع قبس الذكي": بدل أن يعامل الفيديو كل كلمة كوحدة منعزلة،
 * يجمّع الكلمات في "عبارات" بناءً على فترات الصمت الطبيعية بينها،
 * فيستطيع تظليل العبارة الحالية كاملةً ككتلة واحدة مع توهّج إضافي على
 * الكلمة النشطة، وإطلاق تأثير بصري عند نهاية الوقفة فقط (حين يتنفّس
 * القارئ أو يتوقّف) — لا مع كل كلمة.
 *
 * هذا يجعل الحركة البصرية تنسجم مع الإيقاع الطبيعي للتلاوة، وهو ما يفتقر
 * إليه المنافسون عادةً.
 */
data class RecitationPhrase(
    val words: List<TimedQuranWord>,
    val startIndex: Int, // فهرس أول كلمة في العبارة داخل قائمة الكلمات الكاملة
    val endIndex: Int // فهرس آخر كلمة في العبارة (شامل)
) {
    init {
        require(words.isNotEmpty()) { "phrase words must not be empty" }
        require(startIndex >= 0) { "startIndex must be >= 0" }
        require(endIndex >= startIndex) { "endIndex must be >= startIndex" }
        require(endIndex - startIndex + 1 == words.size) {
            "phrase span mismatch: got ${words.size} words for [$startIndex..$endIndex]"
        }
    }

    /** هل فهرس الكلمة [index] (0-based) يقع داخل هذه العبارة؟ */
    fun contains(index: Int): Boolean = index in startIndex..endIndex
}

/**
 * خط زمني موزّع إلى عبارات وفق إيقاع الوقفات، مبني فوق [RecitationTimeline]
 * الأساسي. هو الأساس الذي يُبنى عليه تظليل "قبس الذكي" متعدد الطبقات.
 */
data class PhraseTimeline(
    val timeline: RecitationTimeline,
    val phrases: List<RecitationPhrase>
) {
    init {
        require(phrases.isNotEmpty()) { "phrases must not be empty" }
        require(phrases.first().startIndex == 0) {
            "first phrase must start at index 0"
        }
        require(phrases.last().endIndex == timeline.words.lastIndex) {
            "last phrase must end at the last word index"
        }
    }

    /** العبارة التي تحتوي على كلمة ذات الفهرس [index] (0-based). */
    fun phraseAt(index: Int): RecitationPhrase? =
        phrases.firstOrNull { it.contains(index) }

    /**
     * يعيد حالات التظليل البصري لكل الكلمات عند لحظة زمنية [timeMs]:
     * الكلمة النشطة = [WordVisualState.ACTIVE]، بقية كلمات عبارتها الحالية
     * (قبلها وبعدها في نفس الوقفة) = [WordVisualState.CURRENT]، وكل ما هو
     * خارج العبارة الحالية (عبارات سابقة أو تالية) = [WordVisualState.PAST]
     * (أخفّ بصريًا). قبل بداية أول كلمة (لم يبدأ النطق بعد) تُعامل كل
     * الكلمات كـ PAST.
     */
    fun visualStatesAt(timeMs: Long): List<WordVisualState> {
        val activePosition = timeline.activeWordPositionAt(timeMs)
        val activeIndex = activePosition?.let { pos -> timeline.words.indexOfFirst { it.position == pos } }
        val activePhrase = activeIndex?.let { phraseAt(it) }

        return timeline.words.mapIndexed { idx, _ ->
            when {
                activePhrase == null || !activePhrase.contains(idx) -> WordVisualState.PAST
                idx == activeIndex -> WordVisualState.ACTIVE
                else -> WordVisualState.CURRENT
            }
        }
    }
}
