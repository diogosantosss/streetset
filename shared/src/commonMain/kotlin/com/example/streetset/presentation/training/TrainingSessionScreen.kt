package com.example.streetset.presentation.training

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.streetset.presentation.training.components.CurrentSetPhase
import com.example.streetset.presentation.training.components.EmomPhase
import com.example.streetset.presentation.training.components.FinishedPhase
import com.example.streetset.presentation.training.components.RestPhase
import com.example.streetset.presentation.training.components.TrainingBottomBar
import com.example.streetset.presentation.training.components.UpcomingRow
import com.example.streetset.presentation.training.components.UpcomingRowState
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.ui.components.BOTTOM_BAR_HEIGTH
import com.example.streetset.ui.components.ConfirmSheet
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TrainingSessionScreen(
    sessionId: Int,
    title: String,
    hazeState: HazeState,
    onBack: () -> Unit,
    viewModel: TrainingSessionViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val colors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()

    var showExitConfirm by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.currentIndex, uiState.phase) {
        if (uiState.phase is TrainingPhase.Finished) return@LaunchedEffect
        if (uiState.queue.isEmpty()) return@LaunchedEffect
        scope.launch {
            delay(160)
            val target = uiState.currentIndex.coerceIn(0, uiState.queue.size - 1)
            listState.animateScrollToItem(
                index = target,
                scrollOffset = -24,
            )
        }
    }

    val scrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(TopBarDefaults.Height + 16.dp))

            val current = uiState.currentStep

            AnimatedContent(
                targetState = uiState.phase,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInVertically(tween(260), initialOffsetY = { it / 14 })).togetherWith(
                        fadeOut(tween(200)) + slideOutVertically(tween(200), targetOffsetY = { -it / 14 }),
                    )
                },
                label = "training_phase",
            ) { phase ->
                when (phase) {
                    is TrainingPhase.Rest -> {
                        RestPhase(
                            restSecondsLeft = uiState.restSecondsLeft,
                            restEndsAtMillis = uiState.restEndsAtMillis,
                        )
                    }
                    is TrainingPhase.Working, TrainingPhase.Loading -> {
                        if (current != null) {
                            when (val protocol = uiState.protocol) {
                                is ActiveProtocolState.Emom -> {
                                    EmomPhase(
                                        step = current,
                                        emomUiState = protocol.uiState,
                                        currentRound = protocol.currentRound,
                                        totalRounds = protocol.totalRounds,
                                        secondsLeft = protocol.secondsLeft,
                                        prepareSecondsLeft = protocol.prepareSecondsLeft,
                                        intervalSeconds = protocol.intervalSeconds,
                                        onStart = viewModel::startActiveProtocol,
                                    )
                                }
                                ActiveProtocolState.Idle -> {
                                    CurrentSetPhase(step = current)
                                }
                                is ActiveProtocolState.Hold,
                                is ActiveProtocolState.Metronome -> {
                                    CurrentSetPhase(step = current)
                                }
                            }
                        }
                    }
                    TrainingPhase.Finished -> {
                        FinishedPhase()
                    }
                }
            }

            if (uiState.phase !is TrainingPhase.Finished && uiState.queue.isNotEmpty()) {
                Text(
                    text = "WORKOUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(top = 40.dp, bottom = 8.dp),
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = BOTTOM_BAR_HEIGTH + 32.dp),
                ) {
                    itemsIndexed(
                        items = uiState.queue,
                        key = { _, step -> "tl_${step.blockWithSets.block.id ?: 0}_${step.set.id ?: step.setIndex}_${step.setIndex}" },
                    ) { index, step ->
                        val rowState = when {
                            index < uiState.currentIndex -> UpcomingRowState.Done
                            index == uiState.currentIndex -> UpcomingRowState.Current
                            else -> UpcomingRowState.Upcoming
                        }
                        val previous = uiState.queue.getOrNull(index - 1)
                        val showDivider = index > 0 && rowState != UpcomingRowState.Current && previous?.let { prev ->
                            prev.blockWithSets.block.id != step.blockWithSets.block.id || prev.setIndex != step.setIndex - 1
                        } != false

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = tween(220),
                                    fadeOutSpec = tween(180),
                                    placementSpec = tween(220),
                                ),
                        ) {
                            UpcomingRow(
                                step = step,
                                showDivider = showDivider,
                                state = rowState,
                            )
                        }
                    }
                }
            } else if (uiState.phase !is TrainingPhase.Finished) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        TopBar(
            title = uiState.sessionTimer,
            showBack = true,
            onBackClick = { showExitConfirm = true },
            hazeState = hazeState,
            scrolled = scrolled,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (uiState.phase !is TrainingPhase.Finished) {
            val showBottomBar = when (val phase = uiState.phase) {
                is TrainingPhase.Rest -> true
                is TrainingPhase.Working -> {
                    val protocol = uiState.protocol
                    !(protocol is ActiveProtocolState.Emom && protocol.uiState != com.example.streetset.presentation.training.components.EmomUiState.COMPLETED)
                }
                else -> false
            }
            TrainingBottomBar(
                isSessionPaused = uiState.sessionPaused,
                actionText = if (uiState.phase is TrainingPhase.Rest) "Skip rest" else "Complete set",
                onPauseClick = viewModel::togglePause,
                onActionClick = viewModel::onPrimaryAction,
                hazeState = hazeState,
                isVisible = showBottomBar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding(),
            )
        }
    }

    ConfirmSheet(
        visible = showExitConfirm,
        title = "Leave training?",
        message = "You'll lose your current training session progress if you leave now.",
        confirmText = "Leave",
        isDestructive = true,
        onConfirm = {
            showExitConfirm = false
            onBack()
        },
        onDismiss = { showExitConfirm = false },
    )
}

