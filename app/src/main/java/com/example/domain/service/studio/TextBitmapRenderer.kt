package com.example.domain.service.studio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import com.example.domain.model.quran.WordVisualState
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.TextLayer
import kotlin.math.roundToInt

/**
 * يرسم طبقة نص عربية كـ Bitmap شفاف، مع ضمان تشكيل الحروف والاتجاه RTL الصحيح.
 *
 * لماذا Bitmap وليس TextOverlay المباشر من Media3؟ لأن عرض النص العربي عبر
 * محرك الرندر الخاص بالفيديو غالبًا يفقد التشكيل (Harakat) والربط الصحيح للحروف.
 * رسمه هنا عبر StaticLayout (الذي يعتمد على محرك النصوص الأصلي في أندرويد) يضمن
 * ظهور النص مطابقًا تمامًا لما يظهر في التطبيق.
 *
 * الناتج: Bitmap بعرض/ارتفاع محسوبين حول النص فقط، يُركّب لاحقًا في موضع مناسب
 * فوق الفيديو. هذا يحل مشكلة العربية في الـ overlays نهائيًا.
 */
class TextBitmapRenderer(private val context: Context) {

    /**
     * الخط الافتراضي. يمكن استبداله بخط عربي مخصّص (مثل Amiri/Noto Naskh)
     * موضوع في res/fonts/ وتحميله عبر ResourcesCompat.
     */
    private val defaultTypeface: Typeface by lazy { Typeface.create(Typeface.SERIF, Typeface.NORMAL) }

    /**
     * يرسم الطبقة كاملة (مع توهج اختياري) ويعيد Bitmap شفاف بأبعاد النص فقط.
     */
    fun render(layer: TextLayer, videoWidth: Int, videoHeight: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        val textSizePx = (layer.fontSizeSp * density).roundToInt().toFloat()

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = layer.textColorArgb
            textSize = textSizePx
            typeface = defaultTypeface
            textAlign = Paint.Align.CENTER
        }

        // أقصى عرض للسطر = عرض الفيديو ناقص الهامشين الجانبيين
        val maxTextWidthPx = (videoWidth * (1f - 2f * layer.marginPercent)).roundToInt()
            .coerceAtLeast(1)

        val layoutAlignment = when (layer.alignment) {
            LayerHorizontalAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
            LayerHorizontalAlignment.END -> Layout.Alignment.ALIGN_OPPOSITE
            LayerHorizontalAlignment.START -> Layout.Alignment.ALIGN_NORMAL
        }

        val layout = StaticLayout.Builder.obtain(
            layer.text, 0, layer.text.length, textPaint, maxTextWidthPx
        )
            .setAlignment(layoutAlignment)
            .setIncludePad(true)
            .setLineSpacing(0f, 1.1f)
            .build()

        val bitmapWidth = layout.width.coerceAtLeast(1)
        val bitmapHeight = layout.height.coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        // طبقة التوهج (Glow) إن طُلبت: نرسم نسخة من النص بشفافية منخفضة مع ShadowLayer
        if (layer.glow) {
            val glowPaint = TextPaint(textPaint).apply {
                color = layer.glowColorArgb
                alpha = 130
                setShadowLayer(textSizePx * 0.6f, 0f, 0f, layer.glowColorArgb)
            }
            val glowLayout = StaticLayout.Builder.obtain(
                layer.text, 0, layer.text.length, glowPaint, maxTextWidthPx
            ).setAlignment(layoutAlignment).setIncludePad(true).build()
            glowLayout.draw(canvas)
        }

        // النص الأساسي فوق طبقة التوهج
        layout.draw(canvas)

        return bitmap
    }

    /**
     * يرسم الآية مع تظليل كلمة واحدة فقط بلون مختلف (ذهبي افتراضيًا)،
     * بينما تبقى بقية الكلمات باللون الأساسي.
     *
     * الاستخدام: محرك المزامنة كلمة بكلمة يولّد Bitmap لكل كلمة نشطة
     * دون إعادة حساب حجم الصفحة — فقط يبدّل لون الكلمة الفعلية.
     *
     * يبني SpannableStringBuilder بكلمة كلمة بدل البحث النصي (indexOf)،
     * ما يلغي مشاكل الكلمات المتكررة واختلاف التشكيل/الرسم، ويضمن تطابق
     * الفهرس الملوّن تمامًا مع ترتيب الكلمة في القائمة.
     *
     * @param words نصوص الكلمات بالترتيب الصحيح.
     * @param activeWordIndex فهرس الكلمة النشطة (0-based) داخل قائمة [words]،
     *        أو -1 لتلوين النص بالكامل بلون الكلمات العادية.
     * @param videoWidth عرض الفيديو (لحساب الهامش).
     * @param videoHeight ارتفاع الفيديو.
     * @param fontSizeSp حجم الخط.
     * @param baseColorArgb لون الكلمات العادية.
     * @param highlightColorArgb لون الكلمة النشطة (الذهبي افتراضيًا).
     */
    fun renderHighlighted(
        words: List<String>,
        activeWordIndex: Int,
        videoWidth: Int,
        videoHeight: Int,
        fontSizeSp: Int = 44,
        baseColorArgb: Int = 0xFFE8E0D0.toInt(),
        highlightColorArgb: Int = 0xFFE6B800.toInt()
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val textSizePx = (fontSizeSp * density).roundToInt().toFloat()

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = baseColorArgb
            textSize = textSizePx
            typeface = defaultTypeface
            textAlign = Paint.Align.CENTER
        }

        val maxTextWidthPx = (videoWidth * 0.84f).roundToInt().coerceAtLeast(1)

        // بناء SpannableStringBuilder كلمة بكلمة مع فاصل مسافة، وتطبيق لون التظليل
        // على الكلمة النشطة فقط أثناء الإضافة. هذا يضمن تطابق الفهرس مع الترتيب.
        val spannable = SpannableStringBuilder()
        for (i in words.indices) {
            if (i > 0) spannable.append(" ")
            val wordText = words[i]
            val start = spannable.length
            spannable.append(wordText)
            if (i == activeWordIndex) {
                spannable.setSpan(
                    ForegroundColorSpan(highlightColorArgb),
                    start, spannable.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        val layout = StaticLayout.Builder.obtain(
            spannable, 0, spannable.length, textPaint, maxTextWidthPx
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(true)
            .setLineSpacing(0f, 1.15f)
            .build()

        val bitmapWidth = layout.width.coerceAtLeast(1)
        val bitmapHeight = layout.height.coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        // ظل خفيف خلف النص لزيادة الوضوح فوق الخلفيات المتنوعة.
        textPaint.setShadowLayer(
            textSizePx * 0.12f, 0f, textSizePx * 0.08f, 0x80000000.toInt()
        )
        layout.draw(canvas)

        return bitmap
    }

    /**
     * يرسم النص في حالة "كشف كلمة بكلمة" لـ [KineticTextOverlay]:
     * يبني العبارة كاملة (بكل كلماتها) كي يبقى التخطيط والتفاف الأسطر ثابتًا عبر
     * كل الإطارات (لا قفز في الموضع)، ثم يطبّق شفافية على الكلمات غير المكشوفة.
     *
     * - الكلمات ذات الفهرس < [revealingWordIndex]: مرئية بالكامل (ألفا 255).
     * - الكلمة رقم [revealingWordIndex]: شفافيتها = [revealProgress] (0..1).
     * - الكلمات بعدها: شفافة تمامًا (ألفا 0) — مخفية لكنها تحجز مكانها.
     *
     * التقسيم يتم بالمسافات (لا بالأحرف) في [KineticTextOverlay]، ما يحفظ تشكيل
     * الكلمات العربية ملتصقًا بكلمته. لا يُرسم توهج هنا للحفاظ على حدة النص
     * المطابق للنمط الحركي المرجعي.
     *
     * @param words نصوص الكلمات بالترتيب.
     * @param revealingWordIndex فهرس الكلمة قيد الكشف (0-based)، أو [words].size
     *        لترميز "كل الكلمات مكشوفة".
     * @param revealProgress تقدّم كشف الكلمة الحالية في النطاق [0,1].
     * @param videoWidth عرض الفيديو.
     * @param videoHeight ارتفاع الفيديو.
     * @param fontSizeSp حجم الخط.
     * @param colorArgb لون النص (أبيض افتراضيًا).
     */
    fun renderWordReveal(
        words: List<String>,
        revealingWordIndex: Int,
        revealProgress: Float,
        videoWidth: Int,
        videoHeight: Int,
        fontSizeSp: Int = 44,
        colorArgb: Int = 0xFFFFFFFF.toInt()
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val textSizePx = (fontSizeSp * density).roundToInt().toFloat()

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = colorArgb
            textSize = textSizePx
            typeface = defaultTypeface
            textAlign = Paint.Align.CENTER
        }

        val maxTextWidthPx = (videoWidth * 0.84f).roundToInt().coerceAtLeast(1)

        val spannable = SpannableStringBuilder()
        for (i in words.indices) {
            if (i > 0) spannable.append(" ")
            val start = spannable.length
            spannable.append(words[i])
            val end = spannable.length
            val alpha: Int = when {
                i < revealingWordIndex -> 255
                i == revealingWordIndex -> (255 * revealProgress.coerceIn(0f, 1f)).toInt()
                else -> 0
            }
            val colorWithAlpha = (alpha shl 24) or (colorArgb and 0x00FFFFFF)
            spannable.setSpan(
                ForegroundColorSpan(colorWithAlpha),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val layout = StaticLayout.Builder.obtain(
            spannable, 0, spannable.length, textPaint, maxTextWidthPx
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(true)
            .setLineSpacing(0f, 1.1f)
            .build()

        val bitmapWidth = layout.width.coerceAtLeast(1)
        val bitmapHeight = layout.height.coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        // ظل خفيف خلف النص لزيادة الوضوح فوق الخلفيات المتنوعة.
        textPaint.setShadowLayer(
            textSizePx * 0.12f, 0f, textSizePx * 0.08f, 0x80000000.toInt()
        )
        layout.draw(canvas)

        return bitmap
    }

    /**
     * يرسم الآية بتظليل متعدد الطبقات وفق [WordVisualState] — جوهر "وضع قبس الذكي":
     * الكلمة النشطة ذهبية مع توهّج، وبقية كلمات العبارة الحالية أوضح، وما خارجها
     * أخفّ بصريًا. يُستخدم من [SmartAyahOverlay] لتوليد Bitmap لكل حالة نشطة
     * ممكنة، فتُرسم الطبقات فعليًا داخل الفيديو المُصدَّر (وليس فقط في الواجهة).
     *
     * @param words نصوص الكلمات بالترتيب.
     * @param states الحالة البصرية لكل كلمة (نفس طول [words]).
     * @param videoWidth عرض الفيديو.
     * @param videoHeight ارتفاع الفيديو.
     * @param fontSizeSp حجم الخط.
     * @param activeColorArgb لون الكلمة النشطة (ذهبي).
     * @param currentColorArgb لون كلمات العبارة الحالية.
     * @param pastColorArgb لون الكلمات السابقة/الخارجية (أخفّ).
     */
    fun renderMultiLayer(
        words: List<String>,
        states: List<WordVisualState>,
        videoWidth: Int,
        videoHeight: Int,
        fontSizeSp: Int = 44,
        activeColorArgb: Int = 0xFFE6B800.toInt(),
        currentColorArgb: Int = 0xFFE8E0D0.toInt(),
        pastColorArgb: Int = 0xFF7A7268.toInt()
    ): Bitmap {
        require(words.size == states.size) { "words and states must have same size" }
        require(words.isNotEmpty()) { "words must not be empty" }

        val density = context.resources.displayMetrics.density
        val textSizePx = (fontSizeSp * density).roundToInt().toFloat()

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = currentColorArgb
            textSize = textSizePx
            typeface = defaultTypeface
            textAlign = Paint.Align.CENTER
        }

        val maxTextWidthPx = (videoWidth * 0.84f).roundToInt().coerceAtLeast(1)

        // بناء SpannableStringBuilder كلمة بكلمة مع تلوين كل كلمة حسب حالتها البصرية.
        val spannable = SpannableStringBuilder()
        for (i in words.indices) {
            if (i > 0) spannable.append(" ")
            val start = spannable.length
            spannable.append(words[i])
            val color = when (states[i]) {
                WordVisualState.ACTIVE -> activeColorArgb
                WordVisualState.CURRENT -> currentColorArgb
                WordVisualState.PAST -> pastColorArgb
            }
            spannable.setSpan(
                ForegroundColorSpan(color),
                start, spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val layout = StaticLayout.Builder.obtain(
            spannable, 0, spannable.length, textPaint, maxTextWidthPx
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(true)
            .setLineSpacing(0f, 1.15f)
            .build()

        val bitmapWidth = layout.width.coerceAtLeast(1)
        val bitmapHeight = layout.height.coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        textPaint.setShadowLayer(
            textSizePx * 0.12f, 0f, textSizePx * 0.08f, 0x80000000.toInt()
        )
        layout.draw(canvas)

        return bitmap
    }
    fun renderGradientBackground(
        width: Int,
        height: Int,
        topArgb: Int,
        bottomArgb: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                topArgb, bottomArgb,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }
}
