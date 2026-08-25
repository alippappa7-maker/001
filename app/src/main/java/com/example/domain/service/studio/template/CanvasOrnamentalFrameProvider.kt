package com.example.domain.service.studio.template

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * مزوّد زخارف يرسم بالفعل عبر [Canvas]: دوائر متداخلة، نجمة ثمانية، وزخارف زاويّة،
 * بخطوط ذهبية/خضراء شفافة فوق خلفية شفافة. لا يعتمد على أي صورة خارجية،
 * فيبقى التطبيق أوفلاين بالكامل وبدون أصول ملكية لجهة أخرى.
 */
class CanvasOrnamentalFrameProvider : OrnamentalFrameProvider {

    override fun renderVerseFrame(videoWidthPx: Int, videoHeightPx: Int): Bitmap {
        val frame = (min(videoWidthPx, videoHeightPx) * 0.86f).toInt().coerceAtLeast(120)
        val bitmap = Bitmap.createBitmap(frame, frame, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val cx = frame / 2f
        val cy = frame / 2f

        drawConcentricCircles(canvas, cx, cy, frame)
        drawEightPointStar(canvas, cx, cy, frame)
        drawCornerOrnaments(canvas, frame)
        drawInnerRing(canvas, cx, cy, frame)

        return bitmap
    }

    private fun drawConcentricCircles(canvas: Canvas, cx: Float, cy: Float, frame: Int) {
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            alpha = 190
        }
        val radii = floatArrayOf(0.46f, 0.40f, 0.34f)
        val colors = intArrayOf(0xFFC9A24B.toInt(), 0xFF2E7D5A.toInt(), 0xFFB5862B.toInt())
        radii.forEachIndexed { i, ratio ->
            ring.color = colors[i]
            ring.strokeWidth = frame * (0.012f - i * 0.002f)
            canvas.drawCircle(cx, cy, frame * ratio, ring)
        }
    }

    private fun drawEightPointStar(canvas: Canvas, cx: Float, cy: Float, frame: Int) {
        val star = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xFFC9A24B.toInt()
            strokeWidth = frame * 0.008f
            alpha = 130
        }
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFE8D9A0.toInt()
            alpha = 150
        }
        val radius = frame * 0.30f
        for (i in 0 until 8) {
            val angle = Math.PI * i / 4
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            canvas.drawCircle(x, y, frame * 0.035f, dot)
            canvas.drawLine(cx, cy, x, y, star)
        }
    }

    private fun drawCornerOrnaments(canvas: Canvas, frame: Int) {
        val corner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xFFC9A24B.toInt()
            strokeWidth = frame * 0.010f
            alpha = 160
        }
        val cornerLen = frame * 0.18f
        listOf(
            0f to 0f, frame.toFloat() to 0f, 0f to frame.toFloat(), frame.toFloat() to frame.toFloat()
        ).forEach { (x, y) ->
            val path = Path()
            if (x < frame / 2f && y < frame / 2f) {
                path.moveTo(x, y + cornerLen); path.lineTo(x, y); path.lineTo(x + cornerLen, y)
            } else if (x >= frame / 2f && y < frame / 2f) {
                path.moveTo(x, y + cornerLen); path.lineTo(x, y); path.lineTo(x - cornerLen, y)
            } else if (x < frame / 2f && y >= frame / 2f) {
                path.moveTo(x, y - cornerLen); path.lineTo(x, y); path.lineTo(x + cornerLen, y)
            } else {
                path.moveTo(x, y - cornerLen); path.lineTo(x, y); path.lineTo(x - cornerLen, y)
            }
            canvas.drawPath(path, corner)
        }
    }

    private fun drawInnerRing(canvas: Canvas, cx: Float, cy: Float, frame: Int) {
        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xFF2E7D5A.toInt()
            strokeWidth = frame * 0.006f
            alpha = 120
        }
        canvas.drawCircle(cx, cy, frame * 0.26f, inner)
    }
}
