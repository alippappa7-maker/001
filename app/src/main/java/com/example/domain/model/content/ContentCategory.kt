package com.example.domain.model.content

enum class ContentCategory(val titleAr: String, val isReligious: Boolean) {
    QURAN("القرآن وعلومه", true),
    HADITH("الحديث والسيرة", true),
    FIQH("الفقه والعبادات", true),
    MORALS("الأخلاق", true),
    SELF_PURIFICATION("تزكية النفس", true),
    FAMILY("الأسرة والمجتمع", false),
    HELPING_OTHERS("مساعدة الآخرين", false),
    VOLUNTEERING("التطوع", false),
    EDUCATION("التعليم", false),
    ENVIRONMENT("البيئة", false),
    KINSHIP("صلة الرحم", true),
    NON_FINANCIAL_CHARITY("الصدقة غير المالية", true)
}
