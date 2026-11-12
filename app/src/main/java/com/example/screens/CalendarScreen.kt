package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.ConfirmDialog
import com.example.components.EditMeetingDialog
import com.example.components.SwipeAction
import com.example.components.SwipeRevealRow
import com.example.components.WiseCard
import com.example.model.Meeting
import com.example.model.getDisplayTime
import com.example.model.getDisplayDate
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.LightMint
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel

@Composable
fun CalendarScreen(
    viewModel: GlobalChatViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val meetings by viewModel.meetings.collectAsState()
    val today = remember { java.time.LocalDate.now() }
    val weekDays = remember { (0..6).map { today.with(java.time.DayOfWeek.MONDAY).plusDays(it.toLong()) } }
    var selectedDay by remember { mutableStateOf(today) }
    var openSwipeId by remember { mutableStateOf<String?>(null) }
    var editTarget by remember { mutableStateOf<Meeting?>(null) }
    var cancelTarget by remember { mutableStateOf<Meeting?>(null) }
    val toastContext = androidx.compose.ui.platform.LocalContext.current
    val dayLabel = selectedDay.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US))
    val dayMeetings = meetings
        .filter { it.getDisplayDate() == dayLabel }
        .sortedBy { it.scheduledAt?.toDate()?.time ?: Long.MAX_VALUE }

    editTarget?.let { m ->
        EditMeetingDialog(
            meeting = m,
            onDismiss = { editTarget = null },
            onSave = { t, n, millis, mins ->
                editTarget = null
                viewModel.editMeeting(m, t, n, millis, mins) { ok ->
                    if (!ok) android.widget.Toast.makeText(toastContext, "Could not update the meeting. Try again.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    cancelTarget?.let { m ->
        ConfirmDialog(
            title = "Cancel \"${m.title}\"?",
            message = "Everyone invited will see it as cancelled.",
            confirmLabel = "Cancel meeting",
            onConfirm = {
                cancelTarget = null
                viewModel.cancelMeeting(m) { ok ->
                    if (!ok) android.widget.Toast.makeText(toastContext, "Could not cancel the meeting. Try again.", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { cancelTarget = null }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "Calendar",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Carousel Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { d ->
                        val day = d.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US)
                        val date = d.dayOfMonth.toString()
                        val isSelected = d == selectedDay
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) WiseLime else Color.Transparent)
                                .clickable { selectedDay = d; openSwipeId = null }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) DarkGreen else colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = date,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) DarkGreen else colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val formattedToday = remember(selectedDay) {
                    selectedDay.format(
                        java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d", java.util.Locale.US)
                    )
                }
                Text(
                    text = formattedToday,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (dayMeetings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEAF4E8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = if (colors.isDark) WiseLime else DarkGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No meetings on this day",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Meetings you schedule with a client appear here on their day.",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dayMeetings, key = { it.id }) { meeting ->
                            val canManage = meeting.organizerUid == viewModel.currentUid &&
                                !meeting.status.equals("Cancelled", ignoreCase = true)
                            SwipeRevealRow(
                                isOpen = openSwipeId == meeting.id,
                                onOpenChange = { open ->
                                    if (open) openSwipeId = meeting.id else if (openSwipeId == meeting.id) openSwipeId = null
                                },
                                actions = if (canManage) listOf(
                                    SwipeAction(Icons.Default.Edit, "Edit meeting", WiseLime, DarkGreen, "swipe_action_edit_meeting") { editTarget = meeting },
                                    SwipeAction(Icons.Default.Close, "Cancel meeting", colors.cardBackground, com.example.ui.theme.SentimentNegative, "swipe_action_cancel_meeting") { cancelTarget = meeting }
                                ) else emptyList(),
                                fromStart = true,
                                enabled = canManage
                            ) {
                                MeetingCard(meeting = meeting)
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun MeetingCard(meeting: Meeting) {
    val colors = GlobalChatTheme.colors

    WiseCard(
        cornerRadius = 18.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meeting_card_${meeting.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${meeting.getDisplayTime()} · ${meeting.duration}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            val isCancelled = meeting.status.equals("Cancelled", ignoreCase = true)
            Text(
                text = meeting.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCancelled) colors.textSecondary else colors.textPrimary,
                textDecoration = if (isCancelled) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isCancelled) {
                Text(text = "Cancelled", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
            } else {

            // Online Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(LightMint)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(WiseLime)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Online",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                }
            }
            }
        }
    }
}
