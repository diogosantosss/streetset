package com.example.streetset.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.dao.DaySession
import com.example.streetset.data.dao.DaySummary
import com.example.streetset.data.dao.SessionDao
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock

sealed interface CalendarState {
    data object Loading : CalendarState

    data class Error(val error: String) : CalendarState

    data class Success(
        val selectedDate: LocalDate,
        val summaries: Map<LocalDate, DaySummary>,
        val sessions: List<DaySession>,
    ) : CalendarState
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel : ViewModel(), KoinComponent {

    private val sessionDao: SessionDao by inject()

    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private val _state = MutableStateFlow<CalendarState>(CalendarState.Loading)
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    private val selectedDate = MutableStateFlow(today)
    private val visibleMonth = MutableStateFlow(today.firstOfMonth())
    private var loadJob: Job? = null

    init {
        load()
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun onMonthChanged(monthStart: LocalDate) {
        visibleMonth.value = monthStart

        // Keeps the selected day inside the month being shown
        val selected = selectedDate.value
        if (selected.year != monthStart.year || selected.month != monthStart.month) {
            val todayIsHere = today.year == monthStart.year && today.month == monthStart.month
            selectedDate.value = if (todayIsHere) today else monthStart
        }
    }

    fun retry() {
        _state.value = CalendarState.Loading
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // The date travels with the sessions so the list never shows a day with another day's data
            val sessions = selectedDate.flatMapLatest { date ->
                sessionDao.getDaySessions(date).map { date to it }
            }

            // The visible month plus its neighbours, so the dots are there while swiping
            val summaries = visibleMonth.flatMapLatest { month ->
                sessionDao.getDaySummaries(
                    start = month.minus(DatePeriod(months = 1)),
                    end = month.plus(DatePeriod(months = 2)).minus(DatePeriod(days = 1)),
                )
            }

            combine(sessions, summaries) { (date, list), days ->
                CalendarState.Success(
                    selectedDate = date,
                    summaries = days.associateBy { it.date },
                    sessions = list,
                )
            }
                .catch { throwable ->
                    Napier.e("Failed to load calendar", throwable, TAG)
                    _state.value = CalendarState.Error("Loading calendar failed")
                }
                .collect { state -> _state.value = state }
        }
    }

    companion object {
        private const val TAG = "CalendarViewModel"
    }
}