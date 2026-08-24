package com.example.ui

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SettingsRepository
import com.example.data.repository.MihrabRepository
import com.example.data.repository.UserRepository
import com.example.domain.model.Ayah
import com.example.domain.model.DailyProgress
import com.example.domain.model.PrayerTime
import com.example.domain.model.SyncStatus
import com.example.domain.model.UserAccount
import com.example.domain.model.UserDataSync
import com.example.domain.model.Zikr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var fakeUserRepository: FakeUserRepository
    private lateinit var fakeMihrabRepository: FakeMihrabRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        fakeUserRepository = FakeUserRepository()
        fakeMihrabRepository = FakeMihrabRepository()
        viewModel = AuthViewModel(
            application = application,
            userRepository = fakeUserRepository,
            settingsRepository = SettingsRepository(application),
            mihrabRepository = fakeMihrabRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithEmail rejects invalid email format`() = runTest(testDispatcher) {
        viewModel.signInWithEmail("invalid-email", "password123")
        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.user)
    }

    @Test
    fun `signInWithEmail rejects short password`() = runTest(testDispatcher) {
        viewModel.signInWithEmail("user@example.com", "123")
        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.user)
    }

    @Test
    fun `signInWithEmail succeeds with valid credentials`() = runTest(testDispatcher) {
        viewModel.signInWithEmail("user@example.com", "validPass123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.user)
        assertEquals("user@example.com", state.user?.email)
    }

    @Test
    fun `signUpWithEmail creates user with display name`() = runTest(testDispatcher) {
        viewModel.signUpWithEmail("newuser@example.com", "pass123456", "أحمد")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.user)
        assertEquals("أحمد", state.user?.displayName)
        assertEquals("newuser@example.com", state.user?.email)
    }

    @Test
    fun `signOut clears authenticated user session`() = runTest(testDispatcher) {
        viewModel.signInWithEmail("user@example.com", "validPass123")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.user)

        viewModel.signOut()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.user)
        assertEquals(SyncStatus.IDLE, viewModel.uiState.value.syncStatus)
    }

    @Test
    fun `clearMessages resets error and success messages`() = runTest(testDispatcher) {
        viewModel.signInWithEmail("bad", "123")
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}

private class FakeUserRepository : UserRepository {
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    override val currentUser: Flow<UserAccount?> = _currentUser
    override val isFirebaseAvailable: Boolean = true

    override suspend fun signInWithEmail(email: String, password: String): Result<UserAccount> {
        val user = UserAccount(
            uid = "fake_uid_123",
            email = email,
            displayName = "مستخدم قبس",
            photoUrl = null,
            isAnonymous = false,
            createdAt = 1000L,
            lastLoginAt = 2000L
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<UserAccount> {
        val user = UserAccount(
            uid = "fake_uid_new",
            email = email,
            displayName = displayName,
            photoUrl = null,
            isAnonymous = false,
            createdAt = 1000L,
            lastLoginAt = 1000L
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogleCredential(idToken: String): Result<UserAccount> {
        val user = UserAccount(
            uid = "google_uid_123",
            email = "google@example.com",
            displayName = "Google User",
            photoUrl = null,
            isAnonymous = false,
            createdAt = 1000L,
            lastLoginAt = 2000L
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun launchGoogleSignIn(context: Context, webClientId: String): Result<UserAccount> {
        return signInWithGoogleCredential("fake_token")
    }

    override suspend fun signOut(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }

    override suspend fun syncUserData(userData: UserDataSync): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun fetchCloudUserData(): Result<UserDataSync?> {
        return Result.success(null)
    }

    override fun observeCloudUserData(): Flow<UserDataSync?> {
        return flowOf(null)
    }
}

private class FakeMihrabRepository : MihrabRepository {
    private val favorites = MutableStateFlow<List<Zikr>>(emptyList())
    private val progress = MutableStateFlow(DailyProgress(completedTasks = 2, totalTasks = 5))

    override fun getAzkar(query: String): Flow<List<Zikr>> = flowOf(emptyList())
    override fun getFavorites(): Flow<List<Zikr>> = favorites
    override suspend fun toggleFavorite(zikrId: String) {}
    override fun getDailyAyah(): Flow<Ayah> = flowOf(
        Ayah(
            id = "a1",
            textAr = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            textEn = "Indeed, with hardship [will be] ease.",
            surahNameAr = "الشرح",
            surahNameEn = "Ash-Sharh",
            ayahNumber = 6
        )
    )
    override fun getPrayerTimes(): Flow<List<PrayerTime>> = flowOf(emptyList())
    override fun getDailyProgress(): Flow<DailyProgress> = progress
    override suspend fun updateDailyProgress(completed: Int, total: Int) {
        progress.value = DailyProgress(completedTasks = completed, totalTasks = total)
    }
}
