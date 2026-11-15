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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.streetset.presentation.settings.VolumeViewModel
import com.example.streetset.presentation.training.components.CurrentGroupPhase
import com.example.streetset.presentation.training.components.CurrentSetPhase
import com.example.streetset.presentation.training.components.EmomPhase
import com.example.streetset.presentation.training.components.FinishedPhase
import com.example.streetset.presentation.training.components.FullWorkoutSheet
import com.example.streetset.presentation.training.components.FullWorkoutTrigger
import com.example.streetset.presentation.training.components.HoldPhase
import com.example.streetset.presentation.training.components.MetronomePhase
import com.example.streetset.presentation.training.components.RestPhase
import com.example.streetset.presentation.training.components.TrainingBottomBar
import com.example.streetset.presentation.training.components.VolumeSheet
import com.example.streetset.presentation.training.components.volumeIcon
import com.example.streetset.presentation.training.model.ActiveProtocolState
import com.example.streetset.ui.components.BOTTOM_BAR_HEIGHT
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
    onFinish: () -> Unit,
    viewModel: TrainingSessionViewModel = koinViewModel { parametersOf(sessionId) },
    volumeViewModel: VolumeViewModel = koinViewModel<VolumeViewModel>(),
) {
    val colors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()

    val volumeState by volumeViewModel.volume.collectAsState()
    var showVolume by remember { mutableStateOf(false) }

    var showExitConfirm by remember { mutableStateOf(false) }
    var showFullWorkout by remember { mutableStateOf(false) }

    val heroScrollState = rememberScrollState()

    LaunchedEffect(uiState.currentIndex, uiState.phase) {
        heroScrollState.scrollTo(0)
    }

    val scrolled by remember {
        derivedStateOf { heroScrollState.value > 0 }
    }

    val isFinished = uiState.phase is TrainingPhase.Finished
    val showBottomBar = when (uiState.phase) {
        is TrainingPhase.Finished -> true
        is TrainingPhase.Rest -> true
        is TrainingPhase.Working -> uiState.awaitingFinish || uiState.protocol.canComplete
        else -> false
    }

    val showTrigger = !isFinished && uiState.queue.isNotEmpty()
    val bottomInset = if (showBottomBar) BOTTOM_BAR_HEIGHT else 24.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        val current = uiState.currentStep
        val nextStep = uiState.queue.getOrNull(uiState.currentIndex + 1)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(TopBarDefaults.Height + 16.dp))

            // Hero: the whole screen belongs to the current phase. It scrolls
            // instead of overflowing for tall content (big circuits, etc.).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(
                        if (isFinished) {
                            Modifier
                        } else {
                            Modifier.verticalScroll(heroScrollState)
                        },
                    ),
            ) {
                AnimatedContent(
                    targetState = uiState.phase,
                    modifier = Modifier.fillMaxWidth(),
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
                                restTotalSeconds = uiState.restTotalSeconds,
                                nextStep = nextStep,
                                onAdjust = viewModel::adjustRest,
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
                                            nextStep = nextStep,
                                            intervalSeconds = protocol.intervalSeconds,
                                            onStart = viewModel::startActiveProtocol,
                                        )
                                    }

                                    is ActiveProtocolState.Hold -> HoldPhase(
                                        step = current,
                                        holdUiState = protocol.uiState,
                                        secondsLeft = protocol.secondsLeft,
                                        targetSeconds = protocol.targetSeconds,
                                        prepareSecondsLeft = protocol.prepareSecondsLeft,
                                        nextStep = nextStep,
                                        onStart = viewModel::startActiveProtocol,
                                    )

                                    is ActiveProtocolState.Group -> CurrentGroupPhase(
                                        step = current,
                                        group = protocol,
                                        nextStep = nextStep,
                                    )

                                    is ActiveProtocolState.Metronome -> MetronomePhase(
                                        step = current,
                                        metronome = protocol,
                                        nextStep = nextStep,
                                        onStart = viewModel::startActiveProtocol,
                                    )

                                    ActiveProtocolState.Idle -> CurrentSetPhase(
                                        step = current,
                                        nextStep = nextStep,
                                    )
                                }
                            }
                        }

                        TrainingPhase.Finished -> {
                            FinishedPhase(
                                sessionTitle = title,
                                startedAtMillis = uiState.startedAtMillis,
                                finishedAtMillis = uiState.finishedAtMillis,
                                elapsedSeconds = uiState.elapsedSeconds,
                                setsCompleted = uiState.setsCompleted,
                                totalReps = uiState.totalReps,
                                volumeKg = uiState.volumeKg,
                                notes = uiState.notes,
                                onNotesChange = viewModel::onNotesChange,
                            )
                        }
                    }
                }

                // Sits after the "Up next" card and scrolls with the phase.
                if (showTrigger) {
                    FullWorkoutTrigger(
                        currentStep = uiState.currentIndex + 1,
                        totalSteps = uiState.queue.size,
                        onClick = { showFullWorkout = true },
                        modifier = Modifier.padding(top = 20.dp, bottom = bottomInset),
                    )
                } else if (!isFinished) {
                    Spacer(modifier = Modifier.height(bottomInset))
                }
            }
        }

        TopBar(
            title = if (isFinished) title else uiState.sessionTimer,
            showBack = true,
            onBackClick = {
                if (isFinished) {
                    viewModel.saveAndExit(onFinish)
                } else {
                    showExitConfirm = true
                }
            },
            actionIcon = volumeIcon(volumeState),
            onActionClick = { showVolume = true },
            hazeState = hazeState,
            scrolled = scrolled,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        TrainingBottomBar(
            isSessionPaused = uiState.sessionPaused,
            actionText = when {
                isFinished -> "Back to calendar"
                uiState.awaitingFinish -> "Finish"
                uiState.phase is TrainingPhase.Rest -> "Skip rest"
                uiState.protocol is ActiveProtocolState.Group -> "Complete round"
                else -> "Complete set"
            },
            showPause = !isFinished,
            onPauseClick = viewModel::togglePause,
            onActionClick = {
                if (isFinished) viewModel.saveAndExit(onFinish) else viewModel.onPrimaryAction()
            },
            hazeState = hazeState,
            isVisible = showBottomBar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
        )

        FullWorkoutSheet(
            visible = showFullWorkout,
            queue = uiState.queue,
            currentIndex = uiState.currentIndex,
            awaitingFinish = uiState.awaitingFinish,
            hazeState = hazeState,
            onDismiss = { showFullWorkout = false },
        )
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

    VolumeSheet(
        visible = showVolume,
        volume = volumeState,
        onVolumeChange = volumeViewModel::setVolume,
        onVolumeChangeFinished = volumeViewModel::commit,
        onDismiss = { showVolume = false },
    )
}
