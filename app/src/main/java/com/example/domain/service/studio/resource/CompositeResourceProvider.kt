package com.example.domain.service.studio.resource

import com.example.domain.model.studio.ResourceIntent

/**
 * يدمج عدة مزودات في سلسلة fallback مرتبة:
 *
 * 1. المزودات المحلية (أصول المستخدم) — أعلى أولوية.
 * 2. ذاكرة الموارد المنزّلة (ملفات Pexels/Pixabay المخزّنة محليًا).
 * 3. مزودات الموارد الخارجية (Pexels ثم Pixabay).
 * 4. لون صلب احتياطي — لا يفشل أبدًا.
 *
 * يرجع أول مورد حقيقي يجده، أو لونًا صلبًا احتياطيًا إن لم يجد أي مورد.
 * هذا يضمن أن التصدير يعمل دائمًا حتى بدون إنترنت/مفاتيح.
 */
class CompositeResourceProvider(
    private val providers: List<MediaResourceProvider>
) : MediaResourceProvider {

    override val isAvailable: Boolean = providers.any { it.isAvailable }
    override val displayName: String = "مزود مركّب"

    override suspend fun resolve(intent: ResourceIntent): MediaResource? {
        // جرّب كل مزود متاح بالترتيب حتى يجد أحدها موردًا حقيقيًا.
        for (provider in providers) {
            if (!provider.isAvailable) continue
            val resource = provider.resolve(intent)
            if (resource != null && resource.isRealAsset) {
                return resource
            }
        }
        // لم يُوجد أي مورد حقيقي — ارجع للون الصلب الاحتياطي.
        return MediaResource.SolidColor(intent.fallbackColorArgb)
    }

    /** أسماء المزودات المتاحة حاليًا (للتشخيص والواجهة). */
    fun availableProviderNames(): List<String> =
        providers.filter { it.isAvailable }.map { it.displayName }
}
