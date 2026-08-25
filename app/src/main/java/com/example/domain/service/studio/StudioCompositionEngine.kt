@file:androidx.annotation.OptIn(
    markerClass = [androidx.media3.common.util.UnstableApi::class]
)

package com.example.domain.service.studio

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.OverlaySettings
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import com.example.domain.model.studio.BackgroundType
import com.example.domain.model.studio.CompositionScene
import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.LayerHorizontalAlignment
import com.example.domain.model.studio.LayerVerticalAlignment
import com.example.domain.service.studio.template.AnimatedTextOverlay
import com.example.domain.model.studio.TextLayer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * محرك التركيب الفعلي: يأخذ [CompositionStoryboard] ويصدّره كملف MP4 عبر Media3 Transformer.
 *
 * استراتيجية ضمان النص العربي الصحيح (السبب الجذري لمشاكل العربية في الفيديو):
 * 1) كل طبقة نص تُرسم مسبقًا كـ Bitmap عبر [TextBitmapRenderer] (تشكيل + RTL مضمون
 *    لأن الرسم يتم عبر StaticLayout/محرك النصوص الأصلي لأندرويد).
 * 2) تُركّب كل Bitmap كـ BitmapOverlay فوق الخلفية عبر [OverlayEffect].
 * 3) موضع الطبقة يُحدّد عبر نقاط الإرساء (anchors) في إحداثيات نسبية [−1,1].
 *
 * حزم Media3 المستخدمة (وفق التوثيق الرسمي):
 *  - androidx.media3.effect.BitmapOverlay / OverlayEffect
 *  - androidx.media3.effect.OverlaySettings
 *  - androidx.media3.transformer.* (Transformer, Composition, EditedMediaItem, Effects)
 *
 * التموضع: OverlaySettings.Builder يأخذ:
 *  - setOverlayFrameAnchor(x,y): نقطة الإرساء على الـ overlay (0,0 = مركزه).
 *  - setBackgroundFrameAnchor(x,y): الموضع في إطار الفيديو (0,0 = المركز).
 *  النص يُوضع عند backgroundFrameAnchor المحسوب من محاذاة الطبقة.
 *
 * ملاحظة: إن تغيّر اسم أي دالة في إصدار Media3 أحدث، المطابقة تكون سهلة لأن
 * نقاط التماس مع الـ API محصورة هنا في buildEditedMediaItem وbuildOverlays.
 */
class StudioCompositionEngine(
    private val context: Context,
    private val textRenderer: TextBitmapRenderer = TextBitmapRenderer(context)
) {

    /**
     * يصدّر لوحة القصة إلى ملف MP4. دالة معلّقة تنتظر اكتمال التصدير.
     * تعيد [ExportResult] عند النجاح، أو ترمي [ExportException] عند الفشل.
     */
    suspend fun export(storyboard: CompositionStoryboard, outputFile: File): ExportResult {
        outputFile.parentFile?.mkdirs()
        if (outputFile.exists()) outputFile.delete()

        val listener = TransformerListener()
        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .addListener(listener)
            .build()

        val editedItems = storyboard.scenes.map { scene ->
            buildEditedMediaItem(scene, storyboard)
        }
        // في Media3 1.4.1 لا يوجد EditedMediaItemSequence.Builder؛ يُمرّر List مباشرةً.
        val videoSequence = EditedMediaItemSequence(editedItems)

        // ربط الصوت (مثل تلاوة القرآن) إن وُجد. نبني تسلسلًا صوتيًا منفصلًا
        // يحمل ملف التلاوة بلا فيديو، ونضمّه إلى نفس التركيب (Composition) إلى جانب
        // تسلسل الفيديو. Media3 يدمج التسلسلات ويزامن الصوت مع الفيديو المُصدَّر.
        val sequences = mutableListOf(videoSequence)
        storyboard.audioUri?.let { audioUri ->
            val audioMediaItem = MediaItem.fromUri(Uri.parse(audioUri))
            val audioItem = EditedMediaItem.Builder(audioMediaItem)
                .setEffects(Effects(emptyList(), emptyList()))
                .build()
            sequences += EditedMediaItemSequence(listOf(audioItem))
        }

        val composition = Composition.Builder(sequences).build()

        return suspendCancellableCoroutine { cont ->
            listener.attach(cont)
            transformer.start(composition, outputFile.absolutePath)
            cont.invokeOnCancellation { transformer.cancel() }
        }
    }

    // --------------------------------------------------------------------- private

    private fun buildEditedMediaItem(
        scene: CompositionScene,
        storyboard: CompositionStoryboard
    ): EditedMediaItem {
        val mediaItem = when (scene.background.type) {
            BackgroundType.VIDEO -> {
                val uri = requireNotNull(scene.background.videoUri) { "مشهد فيديو بدون رابط خلفية" }
                MediaItem.fromUri(Uri.parse(uri))
            }
            BackgroundType.IMAGE -> {
                // أولوية: Bitmap في الذاكرة ← مسار صورة محلي ← رابط فيديو ← لون صلب.
                val bitmapUri = scene.background.staticImage?.let { writeBitmapToCache(it, scene.id) }
                    ?: scene.background.imageUri
                    ?: scene.background.videoUri
                if (bitmapUri != null) {
                    MediaItem.Builder().setUri(Uri.parse(bitmapUri)).setImageDurationMs(scene.durationMs).build()
                } else {
                    solidColorMediaItem(scene)
                }
            }
            BackgroundType.SOLID_COLOR -> solidColorMediaItem(scene)
        }

        val overlays = buildOverlays(scene, storyboard)
        val effects = if (overlays.isEmpty()) {
            Effects(emptyList(), emptyList())
        } else {
            // OverlayEffect يتطلب ImmutableList<TextureOverlay> (وليس List<BitmapOverlay>).
            val textureOverlays: ImmutableList<TextureOverlay> =
                ImmutableList.copyOf(overlays)
            Effects(emptyList(), listOf(OverlayEffect(textureOverlays)))
        }

        // ملاحظة: لا نحدد المدة هنا. مدة خلفية الصورة تأتي من setImageDurationMs أعلاه،
        // وخلفية الفيديو تعمل بمدتها الطبيعية (يمكن قصها لاحقًا عبر ClippingConfiguration إن لزم).
        return EditedMediaItem.Builder(mediaItem)
            .setEffects(effects)
            .build()
    }

    /**
     * يكتب Bitmap إلى ملف PNG مؤقت في cacheDir ويعيد مساره كـ URI صالح (file://).
     */
    private fun writeBitmapToCache(bitmap: Bitmap, sceneId: String): String {
        val tempFile = File(context.cacheDir, "bg_img_$sceneId.png")
        tempFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return Uri.fromFile(tempFile).toString()
    }

    /**
     * خلفية لون ثابت كصورة 16×16 تُمدد لتغطي الإطار.
     */
    private fun solidColorMediaItem(scene: CompositionScene): MediaItem {
        val colorBitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888).apply {
            eraseColor(scene.background.colorArgb)
        }
        val tempFile = File(context.cacheDir, "bg_${scene.id}.png")
        tempFile.outputStream().use { colorBitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return MediaItem.Builder().setUri(Uri.fromFile(tempFile)).setImageDurationMs(scene.durationMs).build()
    }

    private fun buildOverlays(
        scene: CompositionScene,
        storyboard: CompositionStoryboard
    ): List<BitmapOverlay> {
        val textOverlays = scene.textLayers.map { layer ->
            val bitmap = textRenderer.render(layer, storyboard.width, storyboard.height)
            val (ax, ay) = computeAnchor(layer.alignment, layer.verticalAnchor, layer.yOffsetPercent)
            AnimatedTextOverlay(
                bitmap = bitmap,
                baseAnchorX = ax,
                baseAnchorY = ay,
                animation = layer.animation,
                animStartMs = layer.animationStartMs,
                animDurationMs = layer.animationDurationMs
            )
        }
        val imageOverlays = scene.overlayLayers.map { layer ->
            val (ax, ay) = computeAnchor(layer.alignment, layer.verticalAnchor, layer.yOffsetPercent)
            val settings = OverlaySettings.Builder()
                .setOverlayFrameAnchor(0f, 0f)
                .setBackgroundFrameAnchor(ax, ay)
                .setAlphaScale(layer.alpha)
                .build()
            BitmapOverlay.createStaticBitmapOverlay(layer.bitmap, settings)
        }
        // overlay ديناميكي اختياري (مثل مزامنة القران كلمة بكلمة): يُركَّب فوق كل ما سبق.
        val dynamicOverlays = listOfNotNull(scene.dynamicOverlay)
        // الإطار الزخرفي (صورة) أولاً ثم النص فوقه ثم الـ overlay الديناميكي فوق الكل.
        return imageOverlays + textOverlays + dynamicOverlays
    }

    /**
     * يحوّل المحاذاة إلى إحداثيات إرساء نسبية [−1,1].
     * (0,0) = مركز الفيديو؛ x>0 يمين، y>0 أعلى.
     */
    private fun computeAnchor(
        hAlign: LayerHorizontalAlignment,
        vAlign: LayerVerticalAlignment,
        yOffsetPercent: Float
    ): Pair<Float, Float> {
        val ax = when (hAlign) {
            LayerHorizontalAlignment.CENTER -> 0f
            LayerHorizontalAlignment.START -> -0.82f
            LayerHorizontalAlignment.END -> 0.82f
        }
        val ayBase = when (vAlign) {
            LayerVerticalAlignment.CENTER -> 0f
            LayerVerticalAlignment.TOP -> 0.82f
            LayerVerticalAlignment.BOTTOM -> -0.82f
        }
        val ay = (ayBase + yOffsetPercent).coerceIn(-0.95f, 0.95f)
        return ax to ay
    }

    /**
     * مستمع Transformer يحوّل ردود النداء إلى استكمال/خطأ لكوروتين معلّق.
     */
    private class TransformerListener : Transformer.Listener {
        private var cont: kotlinx.coroutines.CancellableContinuation<ExportResult>? = null

        fun attach(c: kotlinx.coroutines.CancellableContinuation<ExportResult>) {
            cont = c
        }

        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
            cont?.resume(exportResult)
        }

        override fun onError(
            composition: Composition,
            exportResult: ExportResult,
            exception: ExportException
        ) {
            cont?.resumeWithException(exception)
        }
    }
}
