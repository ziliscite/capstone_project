package com.capstone.aiyam.presentation.core.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.aiyam.data.dto.ResponseWrapper
import com.capstone.aiyam.domain.model.Alerts
import com.capstone.aiyam.domain.model.Classification
import com.capstone.aiyam.domain.model.WeeklySummary
import com.capstone.aiyam.domain.repository.AlertRepository
import com.capstone.aiyam.domain.repository.ChickenRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val chickenRepository: ChickenRepository
) : ViewModel() {
    private val pageSize = 7

    var isLoadingAlerts = MutableStateFlow(false)
        private set

    var isLoadingScans = MutableStateFlow(false)
        private set

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var alertsCount = MutableStateFlow(0)
        private set

    var scansCount = MutableStateFlow(0)
        private set

    private fun warmUp() {
        viewModelScope.launch {
            val blankFile = File.createTempFile("empty", ".txt")
            try {
                chickenRepository.warmUp(blankFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        warmUp()
        fetchWeeklySummaries()
        fetchWeeklyScans()
    }

    private val _weeklyAlerts = MutableStateFlow<List<WeeklySummary>>(emptyList())
    private val _currentAlertsPage = MutableStateFlow(0)

    val currentPageAlertsSummaries: StateFlow<List<WeeklySummary>> = combine(
        _weeklyAlerts, _currentAlertsPage
    ) { summaries, page ->
        val start = page * pageSize
        val end = minOf(start + pageSize, summaries.size)
        if (start >= summaries.size) {
            emptyList()
        } else {
            summaries.subList(start, end)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val canNavigateAlertsNext: StateFlow<Boolean> = combine(
        _weeklyAlerts, _currentAlertsPage
    ) { summaries, page ->
        val totalPages = (summaries.size + pageSize - 1) / pageSize
        page < totalPages - 1
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val canNavigateAlertsPrevious: StateFlow<Boolean> = _currentAlertsPage.map { page ->
        page > 0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private fun aggregateAlertsByDay(alerts: List<Alerts>): List<WeeklySummary> {
        return alerts.groupBy { alert ->
            val dateTime = LocalDateTime.parse(alert.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            dateTime.toLocalDate()
        }.map { (date, alerts) ->
            WeeklySummary(date = date, count = alerts.size)
        }.sortedByDescending { it.date }
    }

    fun goToAlertsNextPage() {
        val totalPages = (_weeklyAlerts.value.size + pageSize - 1) / pageSize
        if (_currentAlertsPage.value < totalPages - 1) {
            _currentAlertsPage.value += 1
        }
    }

    fun goToAlertsPreviousPage() {
        if (_currentAlertsPage.value > 0) {
            _currentAlertsPage.value -= 1
        }
    }

    private fun fetchWeeklySummaries() { viewModelScope.launch {
        alertRepository.getAlerts()
            .onStart {
                isLoadingAlerts.value = true
                _errorMessage.value = null
            }
            .catch { e ->
                isLoadingAlerts.value = false
                _errorMessage.value = e.message ?: "An unexpected error occurred."
            }
            .collect { response ->
                when (response) {
                    is ResponseWrapper.Success -> {
                        isLoadingAlerts.value = false
                        val summaries = aggregateAlertsByDay(response.data)
                        alertsCount.value = response.data.size
                        _weeklyAlerts.value = summaries
                        _currentAlertsPage.value = 0
                    }
                    is ResponseWrapper.Error -> {
                        isLoadingAlerts.value = false
                        _errorMessage.value = response.error
                    }
                    is ResponseWrapper.Loading -> {
                        isLoadingAlerts.value = true
                        _errorMessage.value = null
                    }
                }
            }
    }}

    // ------------------------------------------------

    private val _weeklyScans = MutableStateFlow<List<WeeklySummary>>(emptyList())
    private val _currentScansPage = MutableStateFlow(0)

    val currentPageScansSummaries: StateFlow<List<WeeklySummary>> = combine(
        _weeklyScans, _currentScansPage
    ) { summaries, page ->
        val start = page * pageSize
        val end = minOf(start + pageSize, summaries.size)
        if (start >= summaries.size) {
            emptyList()
        } else {
            summaries.subList(start, end)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val canNavigateScansNext: StateFlow<Boolean> = combine(
        _weeklyScans, _currentScansPage
    ) { summaries, page ->
        val totalPages = (summaries.size + pageSize - 1) / pageSize
        page < totalPages - 1
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val canNavigateScansPrevious: StateFlow<Boolean> = _currentScansPage.map { page ->
        page > 0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private fun aggregateScansByDay(scans: List<Classification>): List<WeeklySummary> {
        return scans.groupBy { alert ->
            val dateTime = LocalDateTime.parse(alert.createdAt, DateTimeFormatter.ISO_DATE_TIME)
            dateTime.toLocalDate()
        }.map { (date, alerts) ->
            WeeklySummary(date = date, count = alerts.size)
        }.sortedByDescending { it.date }
    }

    fun goToScansNextPage() {
        val totalPages = (_weeklyScans.value.size + pageSize - 1) / pageSize
        if (_currentScansPage.value < totalPages - 1) {
            _currentScansPage.value += 1
        }
    }

    fun goToScansPreviousPage() {
        if (_currentScansPage.value > 0) {
            _currentScansPage.value -= 1
        }
    }

    private fun fetchWeeklyScans() { viewModelScope.launch {
        chickenRepository.getHistories()
            .onStart {
                isLoadingScans.value = true
                _errorMessage.value = null
            }
            .catch { e ->
                isLoadingScans.value = false
                _errorMessage.value = e.message ?: "An unexpected error occurred."
            }
            .collect { response ->
                when (response) {
                    is ResponseWrapper.Success -> {
                        isLoadingScans.value = false
                        val summaries = aggregateScansByDay(response.data)
                        scansCount.value = response.data.size
                        _weeklyScans.value = summaries
                        _currentScansPage.value = 0
                    }
                    is ResponseWrapper.Error -> {
                        isLoadingScans.value = false
                        _errorMessage.value = response.error
                    }
                    is ResponseWrapper.Loading -> {
                        isLoadingScans.value = true
                        _errorMessage.value = null
                    }
                }
            }
    }}

    fun refreshData() {
        fetchWeeklySummaries()
        fetchWeeklyScans()
    }
}
