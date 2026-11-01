package com.example.streetset.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.SessionLogDao
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.todayIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

sealed interface StatsState {
    data object Loading : StatsState

    data class Error(val error: String) : StatsState

    data class Success(val stats: StatsData) : StatsState
}

class StatsViewModel : ViewModel(), KoinComponent {

    private val sessionLogDao: SessionLogDao by inject()

    private val _state = MutableStateFlow<StatsState>(StatsState.Loading)
    val state: StateFlow<StatsState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() {
        _state.value = StatsState.Loading
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            delay(3.seconds)
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val since = heatmapStart(today).atTime(0, 0)

            combine(
                sessionLogDao.getSince(since),
                sessionLogDao.getProtocolSets(since),
                sessionLogDao.getTopExercises(since),
            ) { logs, protocols, exercises ->
                StatsState.Success(buildStats(today, logs, protocols, exercises))
            }
                .catch { throwable ->
                    Napier.e("Failed to load stats", throwable, TAG)
                    _state.value = StatsState.Error("Loading stats failed")
                }
                .collect { state -> _state.value = state }
        }
    }

    companion object {
        private const val TAG = "StatsViewModel"
    }
}