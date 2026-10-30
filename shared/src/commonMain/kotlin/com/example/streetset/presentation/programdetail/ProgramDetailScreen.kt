package com.example.streetset.presentation.programdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Lucide
import com.example.streetset.data.entities.Session
import com.example.streetset.data.entities.Week
import com.example.streetset.data.entities.Program
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.components.ProgressBar
import com.example.streetset.ui.theme.LocalAppColors
import com.example.streetset.ui.theme.AppTheme
import com.example.streetset.util.shortDisplay
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlinx.datetime.LocalDate

@Composable
fun ProgramDetailScreen(
    hazeState: HazeState,
    programId: Int,
    programDetailViewModel: ProgramDetailViewModel = koinViewModel { parametersOf(programId) },
    createSessionViewModel: CreateSessionViewModel = koinViewModel(),
    sessionOptionsViewModel: SessionOptionsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onSessionClick: (Session) -> Unit = {},
) {
    val programDetailState by programDetailViewModel.state.collectAsState()
    val createSessionState by createSessionViewModel.state.collectAsState()
    val sessionOptionsState by sessionOptionsViewModel.state.collectAsState()

    ProgramDetailContent(
        programDetailState = programDetailState,
        createSessionState = createSessionState,
        sessionOptionsState = sessionOptionsState,
        hazeState = hazeState,
        onBack = onBack,
        onSessionClick = onSessionClick,
        onAddSession = { weekId -> createSessionViewModel.showForm(weekId) },
        onSessionLongPress = sessionOptionsViewModel::showOptions,
        onSessionInputChange = createSessionViewModel::updateInput,
        onCreateSession = createSessionViewModel::submit,
        onDismissCreateSession = createSessionViewModel::dismiss,
        onDeleteSession = sessionOptionsViewModel::requestDelete,
        onConfirmDeleteSession = sessionOptionsViewModel::confirmDelete,
        onDismissSessionOptions = sessionOptionsViewModel::dismiss,
    )
}

@Composable
private fun ProgramDetailContent(
    programDetailState: ProgramDetailState,
    createSessionState: CreateSessionState,
    sessionOptionsState: SessionOptionsState,
    hazeState: HazeState,
    onBack: () -> Unit,
    onSessionClick: (Session) -> Unit,
    onAddSession: (Int) -> Unit,
    onSessionLongPress: (Session) -> Unit,
    onSessionInputChange: (SessionInput) -> Unit,
    onCreateSession: () -> Unit,
    onDismissCreateSession: () -> Unit,
    onDeleteSession: () -> Unit,
    onConfirmDeleteSession: () -> Unit,
    onDismissSessionOptions: () -> Unit,
) {
    val colors = LocalAppColors.current

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val listState = rememberLazyListState()

    val scrolled by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset > 0 || listState.firstVisibleItemIndex > 0 }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        when (val current = programDetailState) {
            is ProgramDetailState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.accent,
                )
            }

            is ProgramDetailState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = current.message,
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                    )
                }
            }

            is ProgramDetailState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = TopBarDefaults.Height + statusBarPadding + 12.dp,
                        bottom = 156.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    items(current.weeks) { week ->
                        WeekSection(
                            week = week,
                            sessions = current.sessions.filter { it.weekId == week.id },
                            completedSessionIds = current.completedSessionIds,
                            onAddSession = { week.id?.let(onAddSession) },
                            onSessionClick = onSessionClick,
                            onSessionLongPress = onSessionLongPress,
                        )
                    }
                }

                TopBar(
                    title = current.program.name,
                    hazeState = hazeState,
                    scrolled = scrolled,
                    showBack = true,
                    onBackClick = onBack,
                    modifier = Modifier.align(Alignment.TopCenter),
                )

                val orderedSessions = current.sessions.sortedWith(
                    compareBy<Session>(
                        { session -> current.weeks.firstOrNull { it.id == session.weekId }?.number ?: Int.MAX_VALUE },
                        { session -> session.weekday ?: 8 },
                        { session -> session.id ?: Int.MAX_VALUE },
                    ),
                )
                val completedCount = orderedSessions.count { session ->
                    session.id?.let { it in current.completedSessionIds } == true
                }
                val nextSession = orderedSessions.firstOrNull { session ->
                    session.id?.let { it in current.completedSessionIds } != true
                }
                val nextWeek = nextSession?.let { session -> current.weeks.firstOrNull { it.id == session.weekId } }

                ProgramSessionBottomBar(
                    nextSession = nextSession,
                    nextWeek = nextWeek,
                    completedCount = completedCount,
                    totalCount = orderedSessions.size,
                    hazeState = hazeState,
                    onStartClick = onSessionClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .navigationBarsPadding(),
                )
            }
        }

        CreateSessionSheet(
            state = createSessionState,
            onInputChange = onSessionInputChange,
            onConfirm = onCreateSession,
            onDismiss = onDismissCreateSession,
        )

        SessionOptionsSheet(
            state = sessionOptionsState,
            onDelete = onDeleteSession,
            onConfirmDelete = onConfirmDeleteSession,
            onDismiss = onDismissSessionOptions,
        )
    }
}

@Preview(name = "Program detail · pending session")
@Composable
private fun ProgramDetailPendingSessionPreview() {
    val week = Week(
        id = 1,
        programId = 1,
        number = 1,
        startDate = LocalDate(2026, 10, 19),
        endDate = LocalDate(2026, 10, 25),
    )
    val sessions = listOf(
        Session(id = 1, weekId = 1, weekday = 1, name = "Push", type = "Strength"),
        Session(id = 2, weekId = 1, weekday = 3, name = "Pull", type = "Hypertrophy"),
        Session(id = 3, weekId = 1, weekday = 5, name = "Legs", type = "Strength"),
    )
    AppTheme(theme = AppTheme.Dark) {
        ProgramDetailContent(
            programDetailState = ProgramDetailState.Success(
                program = Program(name = "Strength Plan", weeksCount = 4),
                weeks = listOf(week),
                sessions = sessions,
                completedSessionIds = setOf(1),
            ),
            createSessionState = CreateSessionState.Closed,
            sessionOptionsState = SessionOptionsState.Closed,
            hazeState = rememberHazeState(),
            onBack = {},
            onSessionClick = {},
            onAddSession = {},
            onSessionLongPress = {},
            onSessionInputChange = {},
            onCreateSession = {},
            onDismissCreateSession = {},
            onDeleteSession = {},
            onConfirmDeleteSession = {},
            onDismissSessionOptions = {},
        )
    }
}

@Preview(name = "Program detail · sessions complete")
@Composable
private fun ProgramDetailSessionsCompletePreview() {
    val week = Week(
        id = 1,
        programId = 1,
        number = 1,
        startDate = LocalDate(2026, 10, 19),
        endDate = LocalDate(2026, 10, 25),
    )
    val sessions = listOf(
        Session(id = 1, weekId = 1, weekday = 1, name = "Push", type = "Strength"),
        Session(id = 2, weekId = 1, weekday = 3, name = "Pull", type = "Hypertrophy"),
        Session(id = 3, weekId = 1, weekday = 5, name = "Legs", type = "Strength"),
    )
    AppTheme(theme = AppTheme.Dark) {
        ProgramDetailContent(
            programDetailState = ProgramDetailState.Success(
                program = Program(name = "Strength Plan", weeksCount = 4),
                weeks = listOf(week),
                sessions = sessions,
                completedSessionIds = sessions.mapNotNull { it.id }.toSet(),
            ),
            createSessionState = CreateSessionState.Closed,
            sessionOptionsState = SessionOptionsState.Closed,
            hazeState = rememberHazeState(),
            onBack = {},
            onSessionClick = {},
            onAddSession = {},
            onSessionLongPress = {},
            onSessionInputChange = {},
            onCreateSession = {},
            onDismissCreateSession = {},
            onDeleteSession = {},
            onConfirmDeleteSession = {},
            onDismissSessionOptions = {},
        )
    }
}

@Composable
private fun WeekSection(
    week: Week,
    sessions: List<Session>,
    completedSessionIds: Set<Int>,
    onAddSession: () -> Unit,
    onSessionClick: (Session) -> Unit,
    onSessionLongPress: (Session) -> Unit,
) {
    val colors = LocalAppColors.current
    val orderedSessions = sessions.sortedBy { it.weekday ?: 8 }
    val completedCount = orderedSessions.count { session ->
        session.id?.let { it in completedSessionIds } == true
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Week ${week.number}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            if (week.startDate != null && week.endDate != null) {
                Text(
                    text = "${week.startDate.shortDisplay()} - ${week.endDate.shortDisplay()}",
                    fontSize = 14.sp,
                    color = colors.textTertiary,
                )
            }
        }

        if (sessions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "$completedCount/${sessions.size} done",
                    fontSize = 12.sp,
                    color = colors.textTertiary,
                )
                ProgressBar(
                    progress = completedCount.toFloat() / sessions.size,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (sessions.isEmpty()) {
            Text("No sessions yet", fontSize = 14.sp, color = colors.textSecondary)
        } else {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(64.dp * orderedSessions.size.toFloat()),
                ) {
                    if (orderedSessions.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxHeight()
                                .padding(vertical = 32.dp)
                                .width(1.dp)
                                .background(colors.border),
                        )
                    }
                    Column {
                        orderedSessions.forEach { session ->
                            val isCompleted = session.id?.let { it in completedSessionIds } == true
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(64.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) colors.success else colors.textTertiary),
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    orderedSessions.forEach { session ->
                        val isCompleted = session.id?.let { it in completedSessionIds } == true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { onSessionClick(session) },
                                    onLongClick = { onSessionLongPress(session) },
                                )
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(session.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Row(
                                        modifier = Modifier.width(112.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Lucide.Calendar,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = colors.textSecondary,
                                        )
                                        Text(
                                            text = session.weekday?.let { weekdayNames.getOrNull(it - 1) } ?: "Unscheduled",
                                            fontSize = 13.sp,
                                            color = colors.textSecondary,
                                        )
                                    }
                                    session.type?.takeIf { it.isNotBlank() }?.let {
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 2.dp)
                                                .size(width = 1.dp, height = 13.dp)
                                                .background(colors.border),
                                        )
                                        Icon(
                                            imageVector = Lucide.Dumbbell,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = colors.textTertiary,
                                        )
                                        Text(it, fontSize = 13.sp, color = colors.textTertiary)
                                    }
                                }
                            }
                            Icon(
                                imageVector = if (isCompleted) Lucide.Check else Lucide.ChevronRight,
                                contentDescription = if (isCompleted) "Completed" else "Not completed",
                                tint = if (isCompleted) colors.success else colors.textTertiary,
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAddSession,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            border = BorderStroke(1.dp, colors.border.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
        ) { Text("+ Add session", color = colors.accent) }
    }
}

private val weekdayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
