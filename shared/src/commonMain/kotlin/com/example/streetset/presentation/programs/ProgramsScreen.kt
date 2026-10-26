package com.example.streetset.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.example.streetset.data.dao.ProgramSessionProgress
import com.example.streetset.data.entities.Program
import com.example.streetset.navigation.ProgramDetailRoute
import com.example.streetset.presentation.programs.components.EmptyState
import com.example.streetset.presentation.programs.components.ProgramFormSheet
import com.example.streetset.presentation.programs.components.ProgramList
import com.example.streetset.presentation.programs.components.ProgramOptionsSheet
import com.example.streetset.ui.components.ConfirmSheet
import com.example.streetset.ui.components.ModalCard
import com.example.streetset.ui.components.ProcessingModal
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
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
    ) {
        content()
    }
}

@Composable
fun ProgramsScreen(
    navController: NavHostController,
    programsViewModel: ProgramsViewModel = koinViewModel<ProgramsViewModel>(),
    sheetViewModel: SheetViewModel = koinViewModel<SheetViewModel>(),
    hazeState: HazeState,
) {
    val programsState = programsViewModel.state.collectAsState().value
    val sheetState = sheetViewModel.sheetState.collectAsState().value
    val listState = rememberLazyListState()

    val scrolled by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset > 0 }
    }

    ProgramsScreenStructure {
        Box(modifier = Modifier.fillMaxSize()) {
            when (programsState) {
                is ProgramsState.Loading -> ProgramsScreenLoadingContent()

                is ProgramsState.Success -> {
                    if (programsState.programs.isEmpty()) {
                        ProgramsScreenEmptyContent()
                    } else {
                        ProgramsScreenListContent(
                            programs = programsState.programs,
                            sessionProgress = programsState.sessionProgress,
                            hazeState = hazeState,
                            listState = listState,
                            onProgramClick = { program ->
                                val id = program.id ?: return@ProgramsScreenListContent
                                navController.navigate(ProgramDetailRoute(id))
                            },
                            onProgramLongClick = sheetViewModel::showOptions,
                        )
                    }
                }

                is ProgramsState.Error -> ProgramsScreenErrorContent(programsState.error)
            }

            ProgramsTopBar(
                state = programsState,
                hazeState = hazeState,
                scrolled = scrolled,
                onAddClick = sheetViewModel::showCreateForm,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }

    when (sheetState) {
        is SheetState.Options -> {
            ProgramOptionsSheet(
                visible = true,
                program = sheetState.program,
                onEdit = { sheetViewModel.showEditForm(sheetState.program) },
                onDelete = { sheetViewModel.requestDelete(sheetState.program) },
                onDismiss = sheetViewModel::dismiss,
            )
        }

        is SheetState.ConfirmDelete -> {
            ConfirmSheet(
                visible = true,
                title = "Delete Program?",
                message = "Are you sure you want to delete \"${sheetState.program.name}\"? This action cannot be undone.",
                confirmText = "Delete",
                isDestructive = true,
                onConfirm = sheetViewModel::confirmDelete,
                onDismiss = sheetViewModel::dismiss,
            )
        }

        is SheetState.Form -> {
            ProgramFormSheet(
                visible = true,
                title = if (sheetState.program != null) "Edit Program" else "New Program",
                confirmText = if (sheetState.program != null) "Save" else "Create",
                initialInput = sheetState.program?.let { ProgramInput.fromProgram(it) },
                error = sheetState.error,
                onConfirm = sheetViewModel::submitForm,
                onDismiss = sheetViewModel::dismiss,
                onErrorCleared = sheetViewModel::clearFormError,
            )
        }

        is SheetState.Deleting -> {
            ProcessingModal(text = "Deleting program...")
        }

        is SheetState.Submitting -> {
            ProcessingModal(
                text = if (sheetState.program == null) "Creating program..." else "Saving program...",
            )
        }

        SheetState.None -> Unit
    }
}

@Composable
private fun ProgramsTopBar(
    state: ProgramsState,
    hazeState: HazeState,
    scrolled: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSuccessWithPrograms = state is ProgramsState.Success && state.programs.isNotEmpty()
    val isSuccessEmpty = state is ProgramsState.Success && state.programs.isEmpty()

    TopBar(
        title = "Programs",
        scrolled = isSuccessWithPrograms && scrolled,
        hazeState = if (isSuccessWithPrograms) hazeState else null,
        actionIcon = if (isSuccessWithPrograms || isSuccessEmpty) Lucide.Plus else null,
        onActionClick = onAddClick,
        modifier = modifier,
    )
}

@Composable
private fun ProgramsScreenListContent(
    programs: List<Program>,
    sessionProgress: Map<Int, ProgramSessionProgress>,
    hazeState: HazeState,
    listState: LazyListState,
    onProgramClick: (Program) -> Unit,
    onProgramLongClick: (Program) -> Unit,
) {
    ProgramList(
        programs = programs,
        sessionProgress = sessionProgress,
        onProgramClick = onProgramClick,
        onProgramLongClick = onProgramLongClick,
        state = listState,
        topPadding = TopBarDefaults.Height + 16.dp,
        modifier = Modifier.hazeSource(hazeState),
    )
}

@Composable
private fun ProgramsScreenEmptyContent() {
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

@Composable
private fun ProgramsScreenErrorContent(message: String) {
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

@Composable
private fun ProgramsScreenLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
