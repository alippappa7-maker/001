package com.example.data.api

import com.example.data.api.model.quran.ChapterTimingResponse
import com.example.data.api.model.quran.RecitationFilesResponse
import com.example.data.api.model.quran.VerseResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * واجهة API القرآن (Quran.com v4 + QuranCDN):
 * - النص العثماني والكلمات: api.quran.com/api/v4
 * - توقيت الكلمات بالميلي ثانية: api.qurancdn.com/api/qdc (إزاحات داخل ملف السورة)
 * - ملفات الصوت: verses.quran.com
 */
interface QuranApiService {

    /** نص الآية الكامل + كلماتها (للحصول على text_uthmani وقائمة الكلمات). */
    @GET("verses/by_key/{verseKey}")
    suspend fun fetchVerse(
        @Path("verseKey") verseKey: String,
        @Query("words") words: Boolean = true,
        @Query("fields") fields: String = "text_uthmani"
    ): VerseResponse

    /** المسار النسبي لملف الآية المعزول لقارئ محدد. */
    @GET("quran/recitations/{reciterId}")
    suspend fun fetchRecitationFile(
        @Path("reciterId") reciterId: Int,
        @Query("verse_key") verseKey: String
    ): RecitationFilesResponse

    /**
     * توقيت الكلمات داخل ملف السورة الكاملة لقارئ معين.
     * segments=true هو ما يُرجع مصفوفة [position, startMs, endMs] لكل كلمة.
     */
    @GET("https://api.qurancdn.com/api/qdc/audio/reciters/{reciterId}/audio_files")
    suspend fun fetchChapterTiming(
        @Path("reciterId") reciterId: Int,
        @Query("chapter") chapter: Int,
        @Query("segments") segments: Int = 1
    ): ChapterTimingResponse
}
