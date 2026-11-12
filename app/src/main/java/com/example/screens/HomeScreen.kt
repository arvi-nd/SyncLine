package com.example.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Meeting
import com.example.model.Project
import com.example.model.getDisplayDate
import com.example.model.getDisplayTime
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DisplayBlackFontFamily
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Overview, built from different shapes instead of one repeated card:
 * a lime header that carries the one thing that matters now, a strip of faces, a day timeline on a
 * spine, a 24-hour band that shows when everyone works, and project gauges. Real data only.
 */
@Composable
fun HomeScreen(
    viewModel: GlobalChatViewModel,
    onOpenChat: (String) -> Unit,
    onViewAllChats: () -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val inboxItems by viewModel.inboxItems.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val meetings by viewModel.meetings.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val upcoming = remember(meetings) { upcomingMeetings(meetings) }
    val nextMeeting = upcoming.firstOrNull()
    val unreadItems = inboxItems.filter { it.unreadCount > 0 }
    val unreadTotal = unreadItems.sumOf { it.unreadCount }
    val people = unreadItems + inboxItems.filter { it.unreadCount == 0 }
    val todayLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))
    val dayZone = remember(userProfile?.timezone) {
        runCatching { ZoneId.of(userProfile?.timezone?.takeIf { it.isNotBlank() } ?: ZoneId.systemDefault().id) }
            .getOrDefault(ZoneId.systemDefault())
    }
    val dayStart = (userProfile?.workingStartHour ?: 9).toDouble()
    val dayEnd = (userProfile?.workingEndHour ?: 18).toDouble().let { if (it <= dayStart) dayStart + 9 else it }
    val dayNow = hourNow(dayZone)

    Surface(modifier = modifier.fillMaxSize(), color = colors.screenBackground) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 188.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            // Lime header: the date, the title and the one thing that matters now
            item {
                val headerShape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (colors.isDark) 0.dp else 20.dp,
                            shape = headerShape,
                            ambientColor = Color(0x669FE870),
                            spotColor = Color(0x669FE870)
                        )
                        .clip(headerShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFFB4F08C), WiseLime, Color(0xFF8EDB63))))
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 32.dp)
                ) {
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkGreen.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Overview",
                        fontFamily = DisplayBlackFontFamily,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkGreen,
                        letterSpacing = (-1.2).sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    when {
                        nextMeeting != null -> {
                            val c = countdown(nextMeeting, todayLabel)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .press()
                                    .clickable(onClick = onOpenCalendar)
                                    .testTag("home_see_all_calendar")
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        c.first,
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkGreen,
                                        letterSpacing = (-1.5).sp
                                    )
                                    if (c.second.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            c.second,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkGreen,
                                            modifier = Modifier.padding(bottom = 14.dp)
                                        )
                                    }
                                }
                                Text(
                                    nextMeeting.title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    listOf(timeRange(nextMeeting), nextMeeting.clientName)
                                        .filter { it.isNotBlank() }.joinToString(", "),
                                    fontSize = 15.sp,
                                    color = DarkGreen.copy(alpha = 0.75f)
                                )
                            }
                        }
                        unreadTotal > 0 -> Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .press()
                                .clickable(onClick = onViewAllChats)
                                .testTag("home_unread_tile")
                        ) {
                            Text(
                                "$unreadTotal",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen,
                                letterSpacing = (-1.5).sp
                            )
                            Text(
                                if (unreadTotal == 1) "unread message" else "unread messages",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkGreen
                            )
                            Text(
                                unreadItems.take(3).joinToString(", ") { it.client.name },
                                fontSize = 15.sp,
                                color = DarkGreen.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        else -> Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onOpenCalendar)
                                .testTag("home_see_all_calendar")
                        ) {
                            Text("Nothing scheduled, nothing unread.", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = DarkGreen)
                            Text("Your next meeting will appear here.", fontSize = 15.sp, color = DarkGreen.copy(alpha = 0.75f))
                            Spacer(modifier = Modifier.height(20.dp))
                            DayBar(startHour = dayStart, endHour = dayEnd, nowHour = dayNow)
                        }
                    }
                }
            }

            // Faces: who you talk to, with unread counts on them
            item {
                Column(modifier = Modifier.fillMaxWidth().testTag("home_section_people")) {
                    SectionTitle("People", "See all", onViewAllChats, "home_see_all_chats")
                    Spacer(modifier = Modifier.height(16.dp))
                    if (people.isEmpty()) {
                        Text(
                            "No conversations yet. Add a client from the + button.",
                            fontSize = 15.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            people.take(10).forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .press()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onOpenChat(item.client.id) }
                                        .testTag("home_chat_item_${item.client.id}"),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            WiseLime.copy(alpha = if (colors.isDark) 0.30f else 0.60f),
                                                            WiseLime.copy(alpha = if (colors.isDark) 0.14f else 0.30f)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                item.client.avatarInitial,
                                                fontFamily = DisplayBlackFontFamily,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (colors.isDark) WiseLime else DarkGreen
                                            )
                                        }
                                        if (item.unreadCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 4.dp, y = (-2).dp)
                                                    .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.screenBackground)
                                                    .padding(2.dp)
                                                    .clip(CircleShape)
                                                    .background(DarkGreen)
                                                    .padding(horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${item.unreadCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WiseLime)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        item.client.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (item.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Coming up: meetings hung on a vertical spine
            if (upcoming.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().testTag("home_section_later")) {
                        SectionTitle("Coming up", "Calendar", onOpenCalendar, "home_see_all_calendar_button")
                        Spacer(modifier = Modifier.height(16.dp))
                        val shown = upcoming.take(4)
                        shown.forEachIndexed { index, meeting ->
                            TimelineRow(
                                meeting = meeting,
                                first = index == 0,
                                last = index == shown.lastIndex,
                                todayLabel = todayLabel,
                                onClick = onOpenCalendar,
                                tag = "home_meeting_row_$index"
                            )
                        }
                    }
                }
            }

            // Working hours on one 24-hour band, so overlaps are visible
            item {
                val myZone = userProfile?.timezone?.takeIf { it.isNotBlank() } ?: ZoneId.systemDefault().id
                val myZoneId = remember(myZone) { runCatching { ZoneId.of(myZone) }.getOrDefault(ZoneId.systemDefault()) }
                val myStart = (userProfile?.workingStartHour ?: 9).toDouble()
                val myEnd = (userProfile?.workingEndHour ?: 18).toDouble()
                val rows = buildList {
                    add(BandRow("You", segments(myStart, (myEnd - myStart).let { if (it <= 0) 9.0 else it })))
                    clients.distinctBy { it.id }.take(3).forEach { c ->
                        val zone = runCatching { ZoneId.of(c.timezoneId) }.getOrNull() ?: return@forEach
                        val now = Instant.now()
                        val diff = (zone.rules.getOffset(now).totalSeconds - myZoneId.rules.getOffset(now).totalSeconds) / 3600.0
                        add(BandRow(c.firstName.ifBlank { c.name }, segments(9.0 - diff, 9.0)))
                    }
                }
                Column(modifier = Modifier.fillMaxWidth().testTag("home_time_zones")) {
                    SectionTitle("Working hours", null, {}, "home_time_zones_action")
                    Text(
                        "Lime is when each person works, in your time. The line is now.",
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WorkBand(rows = rows, nowHour = hourNow(myZoneId))
                }
            }

            // Projects as gauges
            if (projects.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().testTag("home_section_projects")) {
                        SectionTitle("Projects", "See all", { onOpenProject(projects.first().id) }, "home_see_all_projects_link")
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            projects.take(6).forEach { project -> Gauge(project) { onOpenProject(project.id) } }
                        }
                    }
                }
            }
        }
    }
}

private data class BandRow(val name: String, val segments: List<Pair<Double, Double>>)

/** A working day of [length] hours starting at [start] (in your time), split where it wraps past midnight. */
private fun segments(start: Double, length: Double): List<Pair<Double, Double>> {
    val s = ((start % 24) + 24) % 24
    val e = s + length
    return if (e <= 24) listOf(s to e) else listOf(s to 24.0, 0.0 to (e - 24))
}

private fun hourNow(zone: ZoneId): Double {
    val n = ZonedDateTime.now(zone)
    return n.hour + n.minute / 60.0
}

private fun upcomingMeetings(meetings: List<Meeting>): List<Meeting> {
    val now = System.currentTimeMillis()
    return meetings
        .filter { m ->
            val t = m.scheduledAt?.toDate()?.time
            t != null && t >= now - 30 * 60_000L &&
                !m.status.equals("Cancelled", true) && !m.status.equals("Declined", true)
        }
        .sortedBy { it.scheduledAt!!.toDate().time }
}

private val rangeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

private fun timeRange(meeting: Meeting): String {
    val start = meeting.scheduledAt?.toDate()?.time ?: return meeting.getDisplayTime()
    val zone = ZoneId.systemDefault()
    val from = Instant.ofEpochMilli(start).atZone(zone).format(rangeFormat)
    val to = Instant.ofEpochMilli(start + meeting.durationMinutes * 60_000L).atZone(zone).format(rangeFormat)
    return "$from to $to"
}

/** The big figure and its unit for the lead: minutes, hours, or the day. */
private fun countdown(meeting: Meeting, todayLabel: String): Pair<String, String> {
    val now = System.currentTimeMillis()
    val start = meeting.scheduledAt?.toDate()?.time ?: now
    val minutes = ((start - now) / 60_000L).coerceAtLeast(0)
    return when {
        start <= now -> "Now" to ""
        minutes < 60 -> "$minutes" to "min to go"
        meeting.getDisplayDate() == todayLabel -> "${minutes / 60}" to (if (minutes % 60 == 0L) "h to go" else "h ${minutes % 60}m to go")
        else -> meeting.getDisplayDate().substringBeforeLast(",") to ""
    }
}

/** Eases down the instant a finger lands and springs back on release; never consumes the touch. */
private fun Modifier.press(pressedScale: Float = 0.97f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val reduceMotion = remember {
        android.provider.Settings.Global.getFloat(
            context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        ) == 0f
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 900f),
        label = "press"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                pressed = true
                waitForUpOrCancellation(pass = PointerEventPass.Initial)
                pressed = false
            }
        }
}

@Composable
private fun SectionTitle(title: String, action: String?, onAction: () -> Unit, actionTag: String) {
    val colors = GlobalChatTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, letterSpacing = (-0.4).sp)
        if (action != null) {
            Text(
                text = action,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .testTag(actionTag)
            )
        }
    }
}

/** One meeting on the spine: the time on the left, a node on the line, the details on the right. */
@Composable
private fun TimelineRow(
    meeting: Meeting,
    first: Boolean,
    last: Boolean,
    todayLabel: String,
    onClick: () -> Unit,
    tag: String
) {
    val colors = GlobalChatTheme.colors
    val lineColor = colors.textPrimary.copy(alpha = 0.14f)
    val nodeFill = if (first) WiseLime else colors.screenBackground
    val nodeRing = if (first) DarkGreen else colors.textPrimary.copy(alpha = 0.35f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp)
            .testTag(tag)
    ) {
        Column(modifier = Modifier.width(76.dp).padding(top = 2.dp, bottom = 22.dp)) {
            Text(meeting.getDisplayTime(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            val day = if (meeting.getDisplayDate() == todayLabel) "Today" else meeting.getDisplayDate().substringBeforeLast(",")
            Text(day, fontSize = 13.sp, color = colors.textSecondary)
        }
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
                .drawBehind {
                    val cx = size.width / 2
                    val cy = 12.dp.toPx()
                    if (!last) drawLine(lineColor, Offset(cx, cy), Offset(cx, size.height), strokeWidth = 2.dp.toPx())
                    if (!first) drawLine(lineColor, Offset(cx, 0f), Offset(cx, cy), strokeWidth = 2.dp.toPx())
                    drawCircle(nodeFill, radius = 7.dp.toPx(), center = Offset(cx, cy))
                    drawCircle(nodeRing, radius = 7.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 2.dp.toPx()))
                }
        )
        Column(modifier = Modifier.weight(1f).padding(bottom = 22.dp)) {
            Text(
                meeting.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val detail = listOf(
                meeting.clientName,
                meeting.duration.ifBlank { "${meeting.durationMinutes} min" },
                if (meeting.isOnline) "Online" else "In person"
            ).filter { it.isNotBlank() }.joinToString(", ")
            Text(detail, fontSize = 14.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** 24 hours across, one thin track per person, lime where they work, one line for now. */
@Composable
private fun WorkBand(rows: List<BandRow>, nowHour: Double) {
    val colors = GlobalChatTheme.colors
    val track = colors.textPrimary.copy(alpha = 0.08f)
    val nowColor = colors.textPrimary
    val limeFill = WiseLime
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(64.dp)
                )
                Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val barH = 14.dp.toPx()
                    val top = (size.height - barH) / 2
                    val r = barH / 2
                    drawRoundRect(track, Offset(0f, top), Size(size.width, barH), androidx.compose.ui.geometry.CornerRadius(r, r))
                    row.segments.forEach { (s, e) ->
                        val x0 = (s / 24.0 * size.width).toFloat()
                        val x1 = (e / 24.0 * size.width).toFloat()
                        drawRoundRect(limeFill, Offset(x0, top), Size((x1 - x0).coerceAtLeast(0f), barH), androidx.compose.ui.geometry.CornerRadius(r, r))
                    }
                    val nx = (nowHour / 24.0 * size.width).toFloat()
                    drawLine(nowColor, Offset(nx, 0f), Offset(nx, size.height), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 64.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("12a", "6a", "12p", "6p", "12a").forEach { Text(it, fontSize = 12.sp, color = colors.textSecondary) }
        }
    }
}

/** A project as an open arc with its percentage, no card around it. */
@Composable
private fun Gauge(project: Project, onClick: () -> Unit) {
    val colors = GlobalChatTheme.colors
    val track = colors.textPrimary.copy(alpha = 0.08f)
    Column(
        modifier = Modifier
            .width(112.dp)
            .press()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("home_project_card_${project.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(112.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val stroke = 12.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(track, 135f, 270f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                val sweep = 270f * (project.progress / 100f).coerceIn(0f, 1f)
                if (sweep > 0f) drawArc(WiseLime, 135f, sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Text(
                "${project.progress}%",
                fontFamily = DisplayBlackFontFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary,
                letterSpacing = (-0.8).sp
            )
        }
        Text(
            project.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (project.dueDate.isNotBlank()) {
            Text("due ${project.dueDate}", fontSize = 12.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun hourLabel(h: Double): String {
    val hh = h.toInt() % 24
    val suffix = if (hh < 12) "AM" else "PM"
    val twelve = if (hh % 12 == 0) 12 else hh % 12
    return "$twelve $suffix"
}

/** The working day as a bar: how much of it has passed, in your own hours. */
@Composable
private fun DayBar(startHour: Double, endHour: Double, nowHour: Double) {
    val fraction = ((nowHour - startHour) / (endHour - startHour)).coerceIn(0.0, 1.0).toFloat()
    val label = when {
        nowHour < startHour -> "Your day starts at ${hourLabel(startHour)}"
        nowHour >= endHour -> "Your working day is over"
        else -> "Your working day is ${(fraction * 100).toInt()}% done"
    }
    Column(modifier = Modifier.fillMaxWidth().testTag("home_day_bar")) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkGreen)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(DarkGreen.copy(alpha = 0.16f))
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(10.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(DarkGreen)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(hourLabel(startHour), fontSize = 12.sp, color = DarkGreen.copy(alpha = 0.7f))
            Text(hourLabel(endHour), fontSize = 12.sp, color = DarkGreen.copy(alpha = 0.7f))
        }
    }
}
