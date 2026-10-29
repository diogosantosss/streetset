package com.example.streetset.presentation.sessiondetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.streetset.presentation.sessiondetail.components.AddBlockInline
import com.example.streetset.presentation.sessiondetail.components.BlockOptionsSheet
import com.example.streetset.presentation.sessiondetail.components.BlockSection
import com.example.streetset.presentation.sessiondetail.components.BlockUiState
import com.example.streetset.ui.components.ConfirmSheet
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.example.streetset.navigation.TrainingSessionRoute
import com.example.streetset.presentation.sessiondetail.components.SessionDetailBottomBar
import com.example.streetset.ui.components.BOTTOM_BAR_HEIGTH
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SessionDetailScreen(
    navController: NavHostController,
    sessionId: Int,
    title: String,
    hazeState: HazeState,
    blocksViewModel: SessionBlocksViewModel = koinViewModel<SessionBlocksViewModel> { parametersOf(sessionId) },
    addBlockViewModel: AddBlockViewModel = koinViewModel<AddBlockViewModel> { parametersOf(sessionId) },
    optionsViewModel: BlockOptionsViewModel = koinViewModel<BlockOptionsViewModel>(),
) {
    val colors = LocalAppColors.current
    val blocksState by blocksViewModel.state.collectAsState()
    val addBlockState by addBlockViewModel.state.collectAsState()
    val optionsState by optionsViewModel.state.collectAsState()
    val listState = rememberLazyListState()

    val scrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val isSuccess = blocksState is SessionBlocksState.Success
    val deletingBlockId = (optionsState as? BlockOptionsState.Deleting)?.block?.id

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TopBarDefaults.Height + 16.dp,
                bottom = BOTTOM_BAR_HEIGTH,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .hazeSource(hazeState),
        ) {
            when (val state = blocksState) {
                SessionBlocksState.Loading -> {
                    items(count = 2) { BlockSection(state = BlockUiState.Loading) }
                }

                is SessionBlocksState.Error -> {
                    item {
                        BlockSection(
                            state = BlockUiState.Error(state.error),
                            onRetry = blocksViewModel::retry,
                        )
                    }
                }

                is SessionBlocksState.Success -> {
                    items(state.blocks, key = { it.block.id ?: it.hashCode() }) { item ->
                        BlockSection(
                            state = BlockUiState.Content(
                                item = item,
                                isBusy = deletingBlockId != null && item.block.id == deletingBlockId,
                            ),
                            onLongClick = { optionsViewModel.showOptions(item.block) },
                        )
                    }

                    item(key = "add-block") {
                        AddBlockInline(
                            state = addBlockState,
                            onOpen = addBlockViewModel::open,
                            onClose = addBlockViewModel::close,
                            onDraftChange = addBlockViewModel::updateDraft,
                            onSave = addBlockViewModel::save,
                        )
                    }
                }
            }
        }

        TopBar(
            title = title,
            scrolled = isSuccess && scrolled,
            hazeState = if (isSuccess) hazeState else null,
            showBack = true,
            onBackClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        SessionDetailBottomBar(
            title = title,
            hazeState = hazeState,
            isVisible = isSuccess,
            onStartClick = {
                navController.navigate(TrainingSessionRoute(sessionId = sessionId, title = title))
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        )

    }

    BlockOptionsSheets(
        state = optionsState,
        viewModel = optionsViewModel,
        addBlockViewModel = addBlockViewModel,
        blocksState = blocksState
    )
}

@Composable
private fun BlockOptionsSheets(
    state: BlockOptionsState,
    viewModel: BlockOptionsViewModel,
    addBlockViewModel: AddBlockViewModel,
    blocksState: SessionBlocksState,
) {
    when (state) {
        is BlockOptionsState.Options -> {
            BlockOptionsSheet(
                state = state,
                onEdit = {
                    val block = state.block
                    viewModel.dismiss()
                    (blocksState as? SessionBlocksState.Success)?.blocks?.let { addBlockViewModel.startEdit(block, it) }
                },
                onDelete = viewModel::requestDelete,
                onConfirmDelete = viewModel::confirmDelete,
                onDismiss = viewModel::dismiss,
            )
        }

        is BlockOptionsState.ConfirmDelete -> {
            val message = listOfNotNull(
                "Are you sure you want to delete this block? This action cannot be undone.",
                state.error,
            ).joinToString("\n\n")

            ConfirmSheet(
                visible = true,
                title = "Delete Block?",
                message = message,
                confirmText = "Delete",
                isDestructive = true,
                onConfirm = viewModel::confirmDelete,
                onDismiss = viewModel::dismiss,
            )
        }

        // The block shows its own busy state while deleting
        is BlockOptionsState.Deleting, BlockOptionsState.Closed -> Unit
    }
}