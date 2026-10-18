package com.example.streetset.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.example.streetset.data.entities.Program
import com.example.streetset.presentation.programs.components.EmptyState
import com.example.streetset.presentation.programs.components.ProgramList
import com.example.streetset.presentation.programs.components.ProgramsTitle
import com.example.streetset.ui.components.Button
import com.example.streetset.ui.theme.LocalAppColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProgramsScreenStructure(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        content()
    }
}

@Composable
fun ProgramsScreen(
    vm: ProgramsViewModel = koinViewModel<ProgramsViewModel>()
) {
    when (val state =  vm.state.collectAsState().value) {
        is ProgramsState.Idle -> ProgramsScreenLoading()

        is ProgramsState.Loading -> ProgramsScreenLoading()

        is ProgramsState.Success -> {
            if (state.programs.isEmpty()) {
                ProgramsScreenEmpty()
            } else {
                ProgramsScreenView(
                    programs = state.programs,
                    onAddClick = {},
                    onProgramClick = {},
                )
            }
        }

        is ProgramsState.Error -> ProgramsScreenError(state.error)
    }
}

@Composable
fun ProgramsScreenView(
    programs: List<Program>,
    onAddClick: () -> Unit,
    onProgramClick: (Program) -> Unit,
) {
    ProgramsScreenStructure {
        Box(modifier = Modifier.fillMaxSize()) {
            ProgramList(
                programs = programs,
                onProgramClick = { onProgramClick(it) },
                header = {
                    ProgramsTitle(
                        modifier = Modifier.padding(end = 64.dp),
                    )
                },
                modifier = Modifier,
            )

            Button(
                imageVector = Lucide.Plus,
                onClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 8.dp),
            )
        }
    }
}

@Composable
fun ProgramsScreenLoading() {
    ProgramsScreenStructure {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ProgramsScreenEmpty() {
    ProgramsScreenStructure {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState(
                title = "No programs yet",
                subtitle = "Create your first training program to get started",
            )
        }
    }
}

@Composable
fun ProgramsScreenError(message: String) {
    ProgramsScreenStructure {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            EmptyState(
                title = "Something went wrong",
                subtitle = message,
            )
        }
    }
}