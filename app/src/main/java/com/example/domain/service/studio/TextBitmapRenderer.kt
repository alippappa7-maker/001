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
import android.text.StaticLayout
import android.text.TextPaint
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
     * نسخة مساعدة: يولّد خلفية متدرجة بسيطة (Gradient) كـ Bitmap.
     * مفيدة كخلفية افتراضية عندما لا يوجد فيديو/صورة للمشهد.
     */
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
