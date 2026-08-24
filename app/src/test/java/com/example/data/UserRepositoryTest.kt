package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.firebase.FirebaseSafeManager
import com.example.data.repository.UserRepository
import com.example.data.repository.UserRepositoryImpl
import com.example.domain.model.UserAccount
import com.example.domain.model.UserDataSync
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserRepositoryTest {

    private lateinit var context: Context
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        userRepository = UserRepositoryImpl(context)
    }

    @Test
    fun testUserDataSyncModelDefaults() {
        val sync = UserDataSync(
            favoriteZikrIds = listOf("zikr_1", "zikr_2"),
            dailyCompletedTasks = 3,
            dailyTotalTasks = 5,
            isDarkMode = true,
            language = "ar",
            selectedCityId = "aleppo_abu_jurayn"
        )

        assertEquals(2, sync.favoriteZikrIds.size)
        assertEquals(3, sync.dailyCompletedTasks)
        assertEquals(5, sync.dailyTotalTasks)
        assertTrue(sync.isDarkMode == true)
        assertEquals("ar", sync.language)
        assertEquals("aleppo_abu_jurayn", sync.selectedCityId)
    }

    @Test
    fun testUserAccountModel() {
        val user = UserAccount(
            uid = "test_user_123",
            email = "user@qabas.app",
            displayName = "باحث قبس"
        )

        assertEquals("test_user_123", user.uid)
        assertEquals("user@qabas.app", user.email)
        assertEquals("باحث قبس", user.displayName)
        assertFalse(user.isAnonymous)
    }

    @Test
    fun testSafeLocalModeWhenFirebaseNotInitialized() = runBlocking {
        // Without google-services.json initialized in test environment,
        // FirebaseSafeManager safely reports not configured without crashing.
        val isConfigured = FirebaseSafeManager.isConfigured(context)
        val auth = FirebaseSafeManager.getAuth(context)
        val firestore = FirebaseSafeManager.getFirestore(context)

        // Verifies no exception thrown
        if (!isConfigured) {
            assertNull(auth)
            assertNull(firestore)
        }

        // Test sign-in returns safe Result.failure without throwing fatal crashes
        val result = userRepository.signInWithEmail("test@example.com", "123456")
        // Either failure because no firebase or success in mocked env, but no unhandled crash
        assertNotNull(result)

        // Current user flow emits null safely
        val user = userRepository.currentUser.first()
        // In local mode without auth session, user is null
        assertNull(user)
    }
}
