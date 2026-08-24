package com.example.domain.model.impact

enum class ImpactCategory(val titleAr: String) {
    HELPING_OTHERS("مساعدة الآخرين"),
    VOLUNTEERING("التطوع"),
    EDUCATION("التعليم"),
    ENVIRONMENT("البيئة"),
    KINSHIP("صلة الرحم"),
    NON_FINANCIAL_CHARITY("الصدقة غير المالية")
}

enum class EffortLevel(val titleAr: String) {
    LOW("بسيط"),
    MEDIUM("متوسط"),
    HIGH("عالٍ")
}

data class ImpactInitiative(
    val id: String,
    val title: String,
    val description: String,
    val category: ImpactCategory,
    val effortLevel: EffortLevel,
    val approximateTimeMinutes: Int,
    val detailedSteps: String,
    val source: String,
    val isFavorite: Boolean = false
)
