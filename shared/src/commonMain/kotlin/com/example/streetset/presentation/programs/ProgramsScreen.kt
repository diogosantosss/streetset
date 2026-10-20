package com.example.streetset.presentation.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import com.example.streetset.presentation.programs.components.ProgramFormSheet
import com.example.streetset.presentation.programs.components.ProgramList
import com.example.streetset.presentation.programs.components.ProgramOptionsSheet
import com.example.streetset.presentation.programs.components.ProgramsTitle
import com.example.streetset.ui.components.Button
import com.example.streetset.ui.components.ConfirmSheet
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
    vm: ProgramsViewModel = koinViewModel<ProgramsViewModel>(),
) {
    val state = vm.state.collectAsState().value
    val sheet = (state as? ProgramsState.Success)?.sheet ?: ProgramsState.Sheet.None

    when (state) {
        is ProgramsState.Loading -> ProgramsScreenLoading()

        is ProgramsState.Success -> {
            if (state.programs.isEmpty()) {
                ProgramsScreenEmpty(onAddClick = vm::openCreateForm)
            } else {
                ProgramsScreenView(
                    programs = state.programs,
                    onAddClick = vm::openCreateForm,
                    onProgramClick = vm::selectProgram,
                )
            }
        }

        is ProgramsState.Error -> ProgramsScreenError(state.error)
    }

    val options = sheet as? ProgramsState.Sheet.Options
    ProgramOptionsSheet(
        visible = options != null,
        program = options?.program,
        onEdit = { options?.program?.let(vm::openEditForm) },
        onDelete = { options?.program?.let(vm::requestDelete) },
        onDismiss = vm::dismissSheet,
    )

    val confirmDelete = sheet as? ProgramsState.Sheet.ConfirmDelete
    ConfirmSheet(
        visible = confirmDelete != null,
        title = "Delete Program?",
        message = "Are you sure you want to delete \"" +
                (confirmDelete?.program?.name ?: "") +
                "\"? This action cannot be undone.",
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = vm::confirmDelete,
        onDismiss = vm::dismissSheet,
    )

    val form = sheet as? ProgramsState.Sheet.Form
    ProgramFormSheet(
        visible = form != null,
        title = if (form?.program != null) "Edit Program" else "New Program",
        confirmText = if (form?.program != null) "Save" else "Create",
        initialInput = form?.program?.let { ProgramInput.fromProgram(it) },
        error = form?.error,
        onConfirm = vm::submitForm,
        onDismiss = vm::dismissSheet,
        onErrorCleared = vm::clearFormError,
    )
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

            ProgramsAddButton(onAddClick)
        }
    }
}

@Composable
private fun BoxScope.ProgramsAddButton(onClick: () -> Unit) {
    Button(
        imageVector = Lucide.Plus,
        onClick = onClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 16.dp, top = 8.dp),
    )
}

@Composable
fun ProgramsScreenEmpty(
    onAddClick: () -> Unit,
) {
    ProgramsScreenStructure {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    title = "No programs yet",
                    subtitle = "Create your first training program to get started",
                )
            }

            ProgramsAddButton(onAddClick)
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