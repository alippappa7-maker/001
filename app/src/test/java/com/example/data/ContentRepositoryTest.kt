package com.example.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.repository.LocalContentRepository
import com.example.domain.model.content.ContentCategory
import com.example.domain.model.content.ContentItem
import com.example.domain.model.content.ContentSource
import com.example.domain.model.content.ContentValidationResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentRepositoryTest {

    private lateinit var repository: LocalContentRepository

    @Before
    fun setup() {
        repository = LocalContentRepository(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `initializeLocalContent populates demo data`() = runTest {
        repository.initializeLocalContent()
        
        val publishedContent = repository.observePublishedContent().first()
        assertTrue("Published content should not be empty", publishedContent.isNotEmpty())
        
        val allQuran = repository.getContentByCategory(ContentCategory.QURAN).first()
        assertTrue("Should have Quran content", allQuran.isNotEmpty())
    }

    @Test
    fun `validateForPublishing rejects religious content without source`() {
        val article = ContentItem.Article(
            id = "test_1",
            titleAr = "حديث شريف",
            category = ContentCategory.HADITH,
            source = null // No source
        )
        
        val result = repository.validateForPublishing(article)
        assertTrue(result is ContentValidationResult.Invalid)
        assertEquals("Religious content must have a source.", (result as ContentValidationResult.Invalid).reason)
    }

    @Test
    fun `validateForPublishing rejects religious content with unverified source`() {
        val article = ContentItem.Article(
            id = "test_2",
            titleAr = "حديث شريف",
            category = ContentCategory.HADITH,
            source = ContentSource("كتاب غير معروف", verified = false) // Unverified
        )
        
        val result = repository.validateForPublishing(article)
        assertTrue(result is ContentValidationResult.Invalid)
        assertEquals("Religious content source must be verified.", (result as ContentValidationResult.Invalid).reason)
    }

    @Test
    fun `validateForPublishing accepts religious content with verified source`() {
        val article = ContentItem.Article(
            id = "test_3",
            titleAr = "حديث شريف",
            category = ContentCategory.HADITH,
            source = ContentSource("صحيح البخاري", verified = true)
        )
        
        val result = repository.validateForPublishing(article)
        assertTrue(result is ContentValidationResult.Valid)
    }

    @Test
    fun `validateForPublishing accepts non-religious content without source`() {
        val initiative = ContentItem.ImpactInitiative(
            id = "test_4",
            titleAr = "تنظيف البيئة",
            category = ContentCategory.ENVIRONMENT,
            source = null
        )
        
        val result = repository.validateForPublishing(initiative)
        assertTrue(result is ContentValidationResult.Valid)
    }
}
