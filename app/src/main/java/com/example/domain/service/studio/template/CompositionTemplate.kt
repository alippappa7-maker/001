package com.example.domain.service.studio.template

import com.example.domain.model.studio.CompositionStoryboard
import com.example.domain.model.studio.VideoProject

/**
 * عقد القوالب الجاهزة. كل قالب يحوّل [VideoProject] إلى [CompositionStoryboard]
 * قابلة للرندر، مسؤولًا عن تسلسل المشاهد والألوان والتحريك والزخارف.
 *
 * التعمّد: لا يولّد أي قالب نصًا دينيًا (آية/حديث) من تلقاء نفسه — النصوص
 * تأتي حصرًا من مدخلات المستخدم داخل [VideoProject] أو حقول القالب المُملأة يدويًا.
 */
interface CompositionTemplate {
    fun build(project: VideoProject): CompositionStoryboard
}
