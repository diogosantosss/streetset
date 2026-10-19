package com.example.streetset.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streetset.data.DatabaseSeeder
import com.example.streetset.navigation.Tab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppState(
    val selectedTab: Tab = Tab.Programs,
    val showCreateProgram: Boolean = false,
)

class AppViewModel(
    private val seeder: DatabaseSeeder,
) : ViewModel() {

    private val _state = MutableStateFlow<AppState>(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun selectTab(tab: Tab) {
        _state.update { curr ->
            curr.copy(
                selectedTab = tab,
                // reset sheet state when navigating away
                showCreateProgram = false,
            )
        }
    }

    fun showCreateProgram() = _state.update { it.copy(showCreateProgram = true) }

    fun dismissCreateProgram() = _state.update { it.copy(showCreateProgram = false) }
}
