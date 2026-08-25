@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class]
)

package com.example.domain.service.studio.template

import android.graphics.Bitmap
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlaySettings

/**
 * طبقة نص متحركة زمنيًا: ترث [BitmapOverlay] وتتجاوز [getOverlaySettings]
 * لتحديث الشفافية والموضع في كل إطار وفق [TextAnimationMath].
 *
 * - المحتوى (الصورة النقطية للنص) ثابت — لا يُنشئ Bitmap جديدًا لكل إطار.
 * - الحركة كلها عبر [getOverlaySettings] (alpha/موضع) كما توثّق واجهة Media3 1.4.1.
 *
 * @param baseAnchorX إحداثي X النسبي للموضع النهائي للنص داخل الإطار.
 * @param baseAnchorY إحداثي Y النسبي للموضع النهائي للنص داخل الإطار.
 * @param animStartMs لحظة بدء الحركة داخل المشهد (بالمللي ثانية).
 * @param animDurationMs مدة الحركة (بالمللي ثانية).
 */
class AnimatedTextOverlay(
    private val bitmap: Bitmap,
    private val baseAnchorX: Float,
    private val baseAnchorY: Float,
    private val animation: com.example.domain.model.studio.TextAnimation,
    private val animStartMs: Long,
    private val animDurationMs: Long
) : BitmapOverlay() {

    override fun getBitmap(presentationTimeUs: Long): Bitmap = bitmap

    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings {
        val tMs = presentationTimeUs / MS_PER_US
        val frame = TextAnimationMath.compute(
            animation = animation,
            presentationTimeMs = tMs,
            startMs = animStartMs,
            durationMs = animDurationMs
        )
        val anchorY = (baseAnchorY + frame.yOffset).coerceIn(MIN_ANCHOR_Y, MAX_ANCHOR_Y)
        return OverlaySettings.Builder()
            .setOverlayFrameAnchor(0f, 0f)
            .setBackgroundFrameAnchor(baseAnchorX, anchorY)
            .setAlphaScale(frame.alphaScale.coerceIn(0f, 1f))
            .build()
    }

    private companion object {
        const val MS_PER_US = 1_000L
        const val MIN_ANCHOR_Y = -0.95f
        const val MAX_ANCHOR_Y = 0.95f
    }
}
