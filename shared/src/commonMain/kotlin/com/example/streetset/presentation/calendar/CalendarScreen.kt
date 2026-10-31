package com.example.streetset.presentation.calendar

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.example.streetset.data.dao.DaySession
import com.example.streetset.data.dao.DaySummary
import com.example.streetset.ui.components.BOTTOM_BAR_HEIGTH
import com.example.streetset.ui.components.TopBar
import com.example.streetset.ui.components.TopBarDefaults
import com.example.streetset.ui.theme.LocalAppColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel

private const val PageCount = 2400
private const val InitialPage = 1200
private const val GridRows = 6
private val CellHeight = 44.dp
private val DoneColor = Color(0xFF22C55E)
private val MissedColor = Color(0xFFF59E0B)

@Composable
fun CalendarScreen(
    hazeState: HazeState,
    onSessionClick: (sessionId: Int, title: String) -> Unit,
    calendarViewModel: CalendarViewModel = koinViewModel<CalendarViewModel>(),
    createViewModel: CreateSessionCalendarViewModel = koinViewModel<CreateSessionCalendarViewModel>(),
) {
    val colors = LocalAppColors.current
    val state by calendarViewModel.state.collectAsState()
    val createState by createViewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val today = calendarViewModel.today
    val anchor = remember { today.firstOfMonth() }
    val pagerState = rememberPagerState(initialPage = InitialPage) { PageCount }

    // The view model only needs to know which month settled, to load the dots
    LaunchedEffect(pagerState.settledPage) {
        calendarViewModel.onMonthChanged(monthForPage(anchor, pagerState.settledPage))
    }

    val scrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val success = state as? CalendarState.Success

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
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TopBarDefaults.Height + 8.dp,
                bottom = BOTTOM_BAR_HEIGTH,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
        ) {
            when (val current = state) {
                CalendarState.Loading -> item { CalendarSkeleton() }

                is CalendarState.Error -> item {
                    CalendarError(message = current.error, onRetry = calendarViewModel::retry)
                }

                is CalendarState.Success -> {
                    item(key = "month") {
                        MonthPager(
                            pagerState = pagerState,
                            anchor = anchor,
                            today = today,
                            selectedDate = current.selectedDate,
                            summaries = current.summaries,
                            onSelect = calendarViewModel::selectDate,
                            onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                            onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        )
                    }
                    item(key = "day") {
                        DaySection(
                            date = current.selectedDate,
                            today = today,
                            sessions = current.sessions,
                            onSessionClick = onSessionClick,
                            onAdd = { createViewModel.open(current.selectedDate) },
                            onToday = {
                                calendarViewModel.selectDate(today)
                                scope.launch { pagerState.animateScrollToPage(InitialPage) }
                            },
                        )
                    }
                }
            }
        }

        TopBar(
            title = if (success != null) monthForPage(anchor, pagerState.currentPage).monthTitle() else "Calendar",
            scrolled = success != null && scrolled,
            hazeState = if (success != null) hazeState else null,
            actionIcon = if (success != null) Lucide.Plus else null,
            onActionClick = { success?.let { createViewModel.open(it.selectedDate) } },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    CreateSessionSheet(
        state = createState,
        onNameChange = createViewModel::updateName,
        onCreate = createViewModel::create,
        onDismiss = createViewModel::dismiss,
    )
}

private fun monthForPage(anchor: LocalDate, page: Int): LocalDate =
    anchor.plus(DatePeriod(months = page - InitialPage))

// Month grid
@Composable
private fun MonthPager(
    pagerState: PagerState,
    anchor: LocalDate,
    today: LocalDate,
    selectedDate: LocalDate,
    summaries: Map<LocalDate, DaySummary>,
    onSelect: (LocalDate) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = LocalAppColors.current

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            MonthArrow(Lucide.ChevronLeft, onPrevious)
            MonthArrow(Lucide.ChevronRight, onNext)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTertiary,
                    )
                }
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(CellHeight * GridRows),
        ) { page ->
            MonthGrid(
                month = monthForPage(anchor, page),
                today = today,
                selectedDate = selectedDate,
                summaries = summaries,
                onSelect = onSelect,
            )
        }
    }
}

@Composable
private fun MonthArrow(icon: ImageVector, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        Icon(icon, null, tint = LocalAppColors.current.textSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun MonthGrid(
    month: LocalDate,
    today: LocalDate,
    selectedDate: LocalDate,
    summaries: Map<LocalDate, DaySummary>,
    onSelect: (LocalDate) -> Unit,
) {
    // Weeks start on Monday
    val offset = month.dayOfWeek.isoDayNumber - 1
    val daysInMonth = month.daysUntil(month.plus(DatePeriod(months = 1)))

    Column {
        repeat(GridRows) { row ->
            Row(modifier = Modifier.fillMaxWidth().height(CellHeight)) {
                repeat(7) { column ->
                    val number = row * 7 + column - offset + 1
                    if (number in 1..daysInMonth) {
                        val date = LocalDate(month.year, month.month, number)
                        DayCell(
                            number = number,
                            isToday = date == today,
                            isSelected = date == selectedDate,
                            isPast = date < today,
                            summary = summaries[date],
                            onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    number: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isPast: Boolean,
    summary: DaySummary?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.accent else Color.Transparent),
        ) {
            Text(
                text = "$number",
                fontSize = 16.sp,
                fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isSelected -> colors.background
                    isToday -> colors.accent
                    else -> colors.textPrimary
                },
            )
        }

        // One dot per session (up to 3). Space is always reserved so the days stay aligned.
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier
                .padding(top = 3.dp)
                .height(5.dp),
        ) {
            if (summary != null) {
                repeat(summary.total.coerceAtMost(3)) { index ->
                    val color = when {
                        index < summary.completed -> DoneColor
                        isPast -> MissedColor
                        else -> colors.accent
                    }
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }
        }
    }
}

// Selected day
@Composable
private fun DaySection(
    date: LocalDate,
    today: LocalDate,
    sessions: List<DaySession>,
    onSessionClick: (Int, String) -> Unit,
    onAdd: () -> Unit,
    onToday: () -> Unit,
) {
    val colors = LocalAppColors.current
    val done = sessions.count { it.completed }
    val subtitle = when {
        sessions.isEmpty() -> "Nothing planned"
        else -> buildString {
            append(sessions.size)
            append(if (sessions.size == 1) " session" else " sessions")
            if (done > 0) append(", $done done")
        }
    }

    Column {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date.fullTitle(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (date != today) {
                Text(
                    text = "Today",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToday)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GroupedSurface {
            sessions.forEach { session ->
                SessionRow(
                    session = session,
                    isPast = date < today,
                    onClick = { onSessionClick(session.id, session.name) },
                )
                InsetDivider()
            }
            AddSessionRow(onClick = onAdd)
        }
    }
}

@Composable
private fun SessionRow(session: DaySession, isPast: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val blocks = when (session.blockCount) {
        0 -> "No blocks yet"
        1 -> "1 block"
        else -> "${session.blockCount} blocks"
    }
    val status = when {
        session.completed -> listOfNotNull("Done", session.durationSeconds?.let(::formatDuration))
            .joinToString("  ") to DoneColor
        isPast -> "Missed" to MissedColor
        else -> blocks to colors.textTertiary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 60.dp)
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = status.first,
                fontSize = 12.sp,
                color = status.second,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        if (session.completed) {
            Icon(Lucide.Check, null, tint = DoneColor, modifier = Modifier.size(18.dp))
        } else {
            Icon(Lucide.ChevronRight, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}h ${minutes.toString().padStart(2, '0')}m"
        minutes > 0 -> "$minutes min"
        else -> "<1 min"
    }
}

@Composable
private fun AddSessionRow(onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp),
    ) {
        Icon(Lucide.Plus, null, tint = colors.accent, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add session", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.accent)
    }
}

// States
@Composable
private fun CalendarSkeleton() {
    val colors = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "calendarSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "calendarSkeletonAlpha",
    )
    val bar = colors.textTertiary.copy(alpha = alpha)

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(modifier = Modifier.padding(top = 20.dp)) {
            repeat(GridRows) {
                Row(modifier = Modifier.fillMaxWidth().height(CellHeight)) {
                    repeat(7) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(bar),
                            )
                        }
                    }
                }
            }
        }
        Column {
            SkeletonBar(width = 160.dp, height = 18.dp, color = bar, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(16.dp))
            GroupedSurface {
                repeat(2) { index ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .padding(horizontal = 16.dp),
                    ) {
                        SkeletonBar(width = 120.dp, height = 14.dp, color = bar)
                    }
                    if (index == 0) InsetDivider()
                }
            }
        }
    }
}

@Composable
private fun SkeletonBar(width: Dp, height: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(color),
    )
}

@Composable
private fun CalendarError(message: String, onRetry: () -> Unit) {
    val colors = LocalAppColors.current

    GroupedSurface {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = colors.error,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Retry",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent,
                modifier = Modifier.clickable(onClick = onRetry),
            )
        }
    }
}

// Shared
@Composable
private fun GroupedSurface(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LocalAppColors.current.surfaceHigh),
        content = content,
    )
}

@Composable
private fun InsetDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(0.5.dp)
            .background(LocalAppColors.current.textTertiary.copy(alpha = 0.3f)),
    )
}