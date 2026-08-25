package com.example.domain.model.quran

import java.io.File

/**
 * بيانات التلاوة الجاهزة للتصدير بعد اجتياز البوابة الشرعية. تُبنى عبر
 * [QuranExportPreparation.prepare] الذي يُجري التحقق الصارم أولًا، ثم يُغذّى
 * منها إلى [com.example.domain.service.studio.template.QuranRecitationTemplate.build].
 *
 * تحوي [phraseTimeline] الموزّعة إلى عبارات — وهي الأساس الذي يُبنى عليه التظليل
 * متعدد الطبقات داخل الفيديو المُصدَّر.
 */
data class QuranExportPlan(
    val timeline: RecitationTimeline,
    val phraseTimeline: PhraseTimeline,
    val sourceCard: SourceCard,
    val audioFile: File,
    val actualAudioDurationMs: Long
)

/**
 * يُرمى عندما يفشل التحقق الشرعي قبل التصدير — فيُمنع التصدير تمامًا ولا يُنشأ
 * ملف. تحمل [report] قائمة الأسباب لعرضها على المستخدم أو تسجيلها.
 */
class ShariaValidationException(val report: ShariaValidationReport) :
    RuntimeException(report.errors.joinToString("؛ "))

/**
 * البوابة الفاصلة بين "بيانات التلاوة الخام" و"خط التصدير": تُجري
 * [ShariaExportValidator.validate] صراحةً، فإن فشل أي شرط تُرمى [ShariaValidationException]
 * ولا يصل أي فيديو قرآني ناقص إلى محرك التصدير. عند النجاح تُعيد [QuranExportPlan]
 * الذي يحوي [PhraseTimeline] الجاهزة للتظليل متعدد الطبقات.
 *
 * هذه هي نقطة ربط الـ Overlay بمحرك التصدير فعليًا: لا يُبنى المشهد ولا يُصدَّر
 * فيديو قبل اجتياز هذه البوابة. كل من [timeline] و[phraseTimeline] و[sourceCard]
 * و[audioFile] مضمونة صالحة بعد النجاح بفضل قيود [ShariaExportValidator] الصارمة.
 */
object QuranExportPreparation {

    fun prepare(
        timeline: RecitationTimeline,
        sourceCard: SourceCard?,
        audioFile: File?,
        actualAudioDurationMs: Long?
    ): QuranExportPlan {
        val report = ShariaExportValidator.validate(
            timeline = timeline,
            audioFile = audioFile,
            actualAudioDurationMs = actualAudioDurationMs,
            sourceCard = sourceCard
        )
        if (!report.isValid) throw ShariaValidationException(report)

        // بعد isValid، يضمن المُحقق أن المصدر والصوت والمدة غير معدومة.
        return QuranExportPlan(
            timeline = timeline,
            phraseTimeline = RecitationCueDetector.detect(timeline),
            sourceCard = requireNotNull(sourceCard),
            audioFile = requireNotNull(audioFile),
            actualAudioDurationMs = requireNotNull(actualAudioDurationMs)
        )
    }
}
