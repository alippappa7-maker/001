package com.example.domain.service.studio.template

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Shader

/**
 * يوفّر خلفية غروب إجرائية (مرسومة عبر Canvas) لمشاهد قالب \"النعيم والزهد\" —
 * تدرّج دافئ من الأعلى (داكن) إلى الأفق (كهرماني متوهّج) مع قرص شمس. لا يعتمد
 * على أي صورة خارجية، فيبقى التطبيق أوفلاين بالكامل وبدون أصول ملكية لجهة أخرى.
 *
 * الفصل عبر واجهة يسمح بالعمل بدون [android.content.Context] في الاختبارات عبر
 * [NoOpSunsetBackgroundProvider] التي تعيد null (فيُستخدم لون ثابت بديل).
 */
interface SunsetBackgroundProvider {

    /**
     * يرسم خلفية غروب بحجم الفيديو المُعطى. يعيد Bitmap كامل الأبعاد أو null
     * إن لم يكن الرسم متاحًا (فيُلجأ إلى لون ثابت بديل في القالب).
     */
    fun renderSunset(videoWidthPx: Int, videoHeightPx: Int, variant: SunsetVariant): Bitmap?
}

/** تنويعات الغروب لتعدّد المشاهد دون تكرار نفس اللقطة. */
enum class SunsetVariant {
    /** غروب كامل مشرق — للمشهد الافتتاحي. */
    GOLDEN,
    /** غروب أعمق دفئًا — للمشاهد التأملية. */
    AMBER,
    /** شفق متأخّر قاتم — للاقتراب من الخاتمة. */
    DUSK
}

/** مزوّد لا يرسم شيئًا — يُستخدم في الاختبارات وكمزوّد افتراضي آمن. */
object NoOpSunsetBackgroundProvider : SunsetBackgroundProvider {
    override fun renderSunset(videoWidthPx: Int, videoHeightPx: Int, variant: SunsetVariant): Bitmap? = null
}

/**
 * مزوّد يرسم الغروب فعليًا عبر [Canvas]: تدرّج خطي عمودي + قرص شمس متوهّج +
 * نطاق أفق. الألوان دافئة (ذهبي/كهرماني/بني داكن) لتحاكي الفيديوهات التأملية.
 */
class CanvasSunsetBackgroundProvider : SunsetBackgroundProvider {

    override fun renderSunset(videoWidthPx: Int, videoHeightPx: Int, variant: SunsetVariant): Bitmap {
        val w = videoWidthPx.coerceAtLeast(2)
        val h = videoHeightPx.coerceAtLeast(2)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val (top, mid, horizon, bottom) = palette(variant)
        val horizonY = h * HORIZON_RATIO

        drawSkyGradient(canvas, w, h, horizonY, top, mid, horizon)
        drawSun(canvas, w, h, horizonY, variant)
        drawGround(canvas, w, h, horizonY, horizon, bottom)

        return bitmap
    }

    private fun drawSkyGradient(
        canvas: Canvas, w: Int, h: Int, horizonY: Float,
        top: Int, mid: Int, horizon: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
            0f, 0f, 0f, horizonY,
            intArrayOf(top, mid, horizon),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), horizonY, paint)
    }

    private fun drawSun(canvas: Canvas, w: Int, h: Int, horizonY: Float, variant: SunsetVariant) {
        val cx = w * 0.5f
        val cy = horizonY - h * SUN_LIFT_RATIO
        val radius = h * SUN_RADIUS_RATIO

        // هالة متوهّجة خلف قرص الشمس
        val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = sunColor(variant)
            alpha = 70
        }
        canvas.drawCircle(cx, cy, radius * 2.6f, halo)

        // قرص الشمس
        val disc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = sunColor(variant)
            alpha = 235
        }
        canvas.drawCircle(cx, cy, radius, disc)
    }

    private fun drawGround(
        canvas: Canvas, w: Int, h: Int, horizonY: Float, horizon: Int, bottom: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
            0f, horizonY, 0f, h.toFloat(),
            intArrayOf(horizon, bottom),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, horizonY, w.toFloat(), h.toFloat(), paint)
    }

    private fun palette(variant: SunsetVariant): Quadruple = when (variant) {
        SunsetVariant.GOLDEN -> Quadruple(0xFF1F1208.toInt(), 0xFF5A2E10.toInt(), 0xFFC9762A.toInt(), 0xFF120A04.toInt())
        SunsetVariant.AMBER -> Quadruple(0xFF1A0E06.toInt(), 0xFF4A2410.toInt(), 0xFFB5651D.toInt(), 0xFF0E0703.toInt())
        SunsetVariant.DUSK -> Quadruple(0xFF120A08.toInt(), 0xFF2E1A12.toInt(), 0xFF6E3B22.toInt(), 0xFF080503.toInt())
    }

    private fun sunColor(variant: SunsetVariant): Int = when (variant) {
        SunsetVariant.GOLDEN -> 0xFFFFD27A.toInt()
        SunsetVariant.AMBER -> 0xFFFFBE5E.toInt()
        SunsetVariant.DUSK -> 0xFFE8A24B.toInt()
    }

    private data class Quadruple(val top: Int, val mid: Int, val horizon: Int, val bottom: Int)

    private companion object {
        const val HORIZON_RATIO = 0.68f
        const val SUN_LIFT_RATIO = 0.04f
        const val SUN_RADIUS_RATIO = 0.075f
    }
}
