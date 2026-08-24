package com.example.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LocalContentRepository
import com.example.data.repository.MihrabRepositoryImpl
import com.example.data.repository.PrayerTimeRepositoryImpl
import com.example.data.repository.StudioRepositoryImpl
import com.example.data.repository.home.DashboardAggregator
import com.example.domain.model.home.DashboardState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val aggregator = DashboardAggregator(
        studioRepository = StudioRepositoryImpl(application),
        mihrabRepository = MihrabRepositoryImpl(application),
        prayerTimeRepository = PrayerTimeRepositoryImpl(application),
        contentRepository = LocalContentRepository(application)
    )

    val uiState: StateFlow<DashboardState> = aggregator.getDashboardState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState()
        )
}
