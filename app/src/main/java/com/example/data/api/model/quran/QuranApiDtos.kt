package com.example.data.api.model.quran

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * استجابة: GET /api/v4/verses/by_key/{chapter}:{verse}?words=true
 * يحتوي النص العثماني الكامل ومصفوفة كلمات الآية.
 */
@JsonClass(generateAdapter = true)
data class VerseResponse(
    @field:Json(name = "verse") val verse: VerseDto
)

@JsonClass(generateAdapter = true)
data class VerseDto(
    @field:Json(name = "verse_key") val verseKey: String,
    @field:Json(name = "text_uthmani") val textUthmani: String?,
    @field:Json(name = "words") val words: List<WordDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WordDto(
    @field:Json(name = "position") val position: Int,
    // ملاحظة: حقل text في API يحوي رمز خط واحد وليس نص كلمة عثمانية حقيقية.
    // النص العثماني الكامل للآية متوفر فقط على مستوى verse.text_uthmani.
    // لذلك نحتفظ بالكلمات للترتيب ونقسم النص الكامل لاحقًا في QuranRepository.
    @field:Json(name = "char_type_name") val charType: String,
    @field:Json(name = "audio_url") val audioUrl: String? // "wbw/001_001_001.mp3"
)

/**
 * استجابة: GET /api/v4/quran/recitations/{id}?verse_key={key}
 * يحتوي المسار النسبي لملف الآية المعزول.
 */
@JsonClass(generateAdapter = true)
data class RecitationFilesResponse(
    @field:Json(name = "audio_files") val audioFiles: List<RecitationFileDto> = emptyList(),
    @field:Json(name = "meta") val meta: RecitationMetaDto? = null
)

@JsonClass(generateAdapter = true)
data class RecitationFileDto(
    @field:Json(name = "verse_key") val verseKey: String,
    @field:Json(name = "url") val url: String // "Alafasy/mp3/002255.mp3"
)

@JsonClass(generateAdapter = true)
data class RecitationMetaDto(
    @field:Json(name = "reciter_name") val reciterName: String?
)

/**
 * استجابة: GET /api.qurancdn.com/.../reciters/{id}/audio_files?chapter={n}&segments=1
 * يحتوي التوقيت الدقيق لكل كلمة كمصفوفة segments داخل ملف السورة الكاملة.
 */
@JsonClass(generateAdapter = true)
data class ChapterTimingResponse(
    @field:Json(name = "audio_files") val audioFiles: List<ChapterAudioFileDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChapterAudioFileDto(
    @field:Json(name = "duration") val duration: Long,
    @field:Json(name = "verse_timings") val verseTimings: List<VerseTimingDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VerseTimingDto(
    @field:Json(name = "verse_key") val verseKey: String,
    @field:Json(name = "timestamp_from") val timestampFrom: Long,
    @field:Json(name = "timestamp_to") val timestampTo: Long,
    @field:Json(name = "duration") val duration: Long,
    // segment = [position, startMs, endMs] ؛ أحيانًا الأخير يأتي بزاوية واحدة فقط (tail).
    @field:Json(name = "segments") val segments: List<List<Long>> = emptyList()
)
