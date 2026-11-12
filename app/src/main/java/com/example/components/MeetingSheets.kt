package com.example.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.example.model.formatAmount
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.NearBlack
import com.example.ui.theme.WiseLime
import com.example.model.getDisplayMonth
import com.example.model.getDisplayDay
import com.example.model.getDisplayTime

@Composable
fun ScheduleOptionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "schedule_card_scale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (colors.isDark) Color(0xFF2E322A) else Color(0xFFDFE3DA)
        ),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .height(64.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ScheduleMorphSheet(
    clientName: String = "Sarah Jenkins",
    onClose: () -> Unit,
    onScheduleMeeting: () -> Unit,
    onViewMeetings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    val iconBg = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEAF4E8)
    val iconTint = if (colors.isDark) WiseLime else Color(0xFF335832)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAmountY = 0f },
                    onDragEnd = {
                        if (dragAmountY > 30f) {
                            onClose()
                        }
                        dragAmountY = 0f
                    },
                    onDragCancel = { dragAmountY = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f) {
                            dragAmountY += dragAmount
                        }
                    }
                )
            }
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp)
    ) {
        // Drag handle indicator bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
        )

        // Header Row: Title, Subtitle, and Top-Right Calendar Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Schedule",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Plan something with $clientName",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
                    .testTag("schedule_header_badge"),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Schedule",
                        tint = iconTint,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopStart)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(11.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 1: Schedule a meeting
        ScheduleOptionCard(
            title = "Schedule a meeting",
            subtitle = "Choose a date, time and duration",
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier
                                .size(21.dp)
                                .align(Alignment.TopStart)
                        )
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            },
            onClick = onScheduleMeeting,
            testTag = "schedule_meeting_option_card"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Card 2: View scheduled meetings
        ScheduleOptionCard(
            title = "View scheduled meetings",
            subtitle = "See upcoming and previous meetings",
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier
                                .size(21.dp)
                                .align(Alignment.TopStart)
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.Center)
                                .offset(y = 2.dp)
                        )
                    }
                }
            },
            onClick = onViewMeetings,
            testTag = "view_meetings_option_card"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Centered bottom caption
        Text(
            text = "Meetings are associated with this conversation.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textSecondary.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMeetingMorphSheet(
    clientName: String = "Sarah Jenkins",
    onCancel: () -> Unit,
    onSchedule: (
        title: String,
        date: String,
        time: String,
        duration: String,
        durationMinutes: Int,
        notes: String,
        scheduledAtMillis: Long?,
        meetingId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }
    var attemptId by remember { mutableStateOf("m_${java.util.UUID.randomUUID()}") }

    var meetingTitle by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedHour by remember { mutableIntStateOf(10) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }

    val durationOptions = listOf(5, 10, 15, 30, 45, 60)
    var selectedDurationMinutes by remember { mutableIntStateOf(30) }
    var durationDropdownExpanded by remember { mutableStateOf(false) }

    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun formatDisplayTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val displayMinute = minute.toString().padStart(2, '0')
        return "$displayHour:$displayMinute $amPm"
    }

    val startTimeFormatted = remember(selectedHour, selectedMinute) {
        formatDisplayTime(selectedHour, selectedMinute)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val instant = java.time.Instant.ofEpochMilli(selectedMillis)
                            val formatted = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US)
                                .withZone(java.time.ZoneId.of("UTC"))
                                .format(instant)
                            selectedDate = formatted
                            selectedDateMillis = selectedMillis
                            validationError = null
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = DarkGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("OK", color = DarkGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(24.dp)
        )
    }

    val iconBg = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEAF4E8)
    val iconTint = if (colors.isDark) WiseLime else Color(0xFF335832)

    val inputBg = if (colors.isDark) Color(0xFF1E211C) else Color(0xFFF9FAF7)
    val inputBorder = if (colors.isDark) Color(0xFF2E322A) else Color(0xFFDFE3DA)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAmountY = 0f },
                    onDragEnd = {
                        if (dragAmountY > 35f) {
                            onCancel()
                        }
                        dragAmountY = 0f
                    },
                    onDragCancel = { dragAmountY = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f) {
                            dragAmountY += dragAmount
                        }
                    }
                )
            }
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 12.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
        )

        // Header: "New meeting", "With $clientName", Mint squircle badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "New meeting",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "With $clientName",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCancel
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "New Meeting",
                        tint = iconTint,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopStart)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(11.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Meeting title
        Column {
            Text(
                text = "Meeting title",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = inputBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (meetingTitle.isEmpty()) {
                        Text(
                            text = "e.g. Project review",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textSecondary.copy(alpha = 0.65f)
                        )
                    }
                    BasicTextField(
                        value = meetingTitle,
                        onValueChange = {
                            meetingTitle = it
                            validationError = null
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meeting_title_input")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Date
        Column {
            Text(
                text = "Date",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Surface(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                color = inputBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("meeting_date_picker")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Date",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (selectedDate.isEmpty()) "Select date" else selectedDate,
                        fontSize = 13.sp,
                        fontWeight = if (selectedDate.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                        color = if (selectedDate.isEmpty()) colors.textSecondary.copy(alpha = 0.7f) else (if (colors.isDark) WiseLime else DarkGreen),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Start time and Duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Start time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Start time",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Surface(
                    onClick = { showTimePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("meeting_time_picker")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Time",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = startTimeFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Duration Dropdown
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "Duration",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Surface(
                        onClick = { durationDropdownExpanded = true },
                        shape = RoundedCornerShape(16.dp),
                        color = inputBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("meeting_duration_picker")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$selectedDurationMinutes min",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = durationDropdownExpanded,
                    onDismissRequest = { durationDropdownExpanded = false },
                    modifier = Modifier.background(colors.cardBackground)
                ) {
                    durationOptions.forEach { minutes ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$minutes min",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedDurationMinutes == minutes) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedDurationMinutes == minutes) (if (colors.isDark) WiseLime else DarkGreen) else colors.textPrimary
                                )
                            },
                            onClick = {
                                selectedDurationMinutes = minutes
                                durationDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("duration_option_$minutes")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Times shown in your local time zone.
        Text(
            text = "Times shown in your local time zone.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textSecondary.copy(alpha = 0.75f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Notes (optional)
        Column {
            Text(
                text = "Notes (optional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = inputBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (notes.isEmpty()) {
                        Text(
                            text = "Anything $clientName should know?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textSecondary.copy(alpha = 0.65f)
                        )
                    }
                    BasicTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meeting_notes_input")
                    )
                }
            }
        }

        // Validation / Submission Error Banner
        if (validationError != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (colors.isDark) Color(0xFF3B1F21) else Color(0xFFFDE8E8),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = validationError ?: "",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (colors.isDark) Color(0xFFFF8B8B) else Color(0xFFD03238),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Schedule meeting button (Dark/Black Pill)
        val submitInteractionSource = remember { MutableInteractionSource() }
        val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
        val submitScale by animateFloatAsState(
            targetValue = if (isSubmitPressed && !isSaving) 0.97f else 1.0f,
            animationSpec = spring(stiffness = 500f),
            label = "submit_btn_scale"
        )

        Surface(
            onClick = {
                if (isSaving) return@Surface
                if (meetingTitle.trim().isBlank()) {
                    validationError = "Please enter a meeting title."
                    return@Surface
                }
                if (selectedDate.isBlank() || selectedDate == "Select date") {
                    validationError = "Please select a date."
                    return@Surface
                }

                validationError = null
                isSaving = true

                val finalMillis: Long? = if (selectedDateMillis != null) {
                    val localDate = java.time.Instant.ofEpochMilli(selectedDateMillis!!)
                        .atZone(java.time.ZoneId.of("UTC"))
                        .toLocalDate()
                    val localDateTime = localDate.atTime(selectedHour, selectedMinute)
                    val zonedDateTime = localDateTime.atZone(java.time.ZoneId.systemDefault())
                    zonedDateTime.toInstant().toEpochMilli()
                } else null

                onSchedule(
                    meetingTitle.trim(),
                    selectedDate,
                    startTimeFormatted,
                    "$selectedDurationMinutes min",
                    selectedDurationMinutes,
                    notes.trim(),
                    finalMillis,
                    attemptId,
                    {
                        isSaving = false
                        attemptId = "m_${java.util.UUID.randomUUID()}"
                    },
                    { err ->
                        isSaving = false
                        validationError = err
                    }
                )
            },
            enabled = !isSaving,
            shape = RoundedCornerShape(9999.dp),
            color = if (colors.isDark) Color(0xFF262923) else Color(0xFF1B1E19),
            modifier = Modifier
                .fillMaxWidth()
                .scale(submitScale)
                .height(46.dp)
                .testTag("submit_schedule_meeting_btn")
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isSaving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = WiseLime,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Scheduling...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "Schedule meeting",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 6. Cancel text button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCancel
                )
                .padding(vertical = 4.dp)
                .testTag("cancel_schedule_meeting_btn"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancel",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
        }
    }
}

data class ScheduledMeetingItemData(
    val id: String,
    val month: String,
    val day: String,
    val title: String,
    val time: String,
    val duration: String,
    val status: String,
    val isConfirmed: Boolean
)

@Composable
fun ScheduledMeetingsMorphSheet(
    clientName: String = "Sarah Jenkins",
    meetings: List<com.example.model.Meeting> = emptyList(),
    currentUid: String? = null,
    onEditMeeting: (com.example.model.Meeting, String, String, Long, Int) -> Unit = { _, _, _, _, _ -> },
    onCancelMeeting: (com.example.model.Meeting) -> Unit = {},
    onClose: () -> Unit,
    onScheduleNewMeeting: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }
    var selectedTab by remember { mutableStateOf("Upcoming") }
    var openSwipeId by remember { mutableStateOf<String?>(null) }
    var editTarget by remember { mutableStateOf<com.example.model.Meeting?>(null) }
    var cancelTarget by remember { mutableStateOf<com.example.model.Meeting?>(null) }

    editTarget?.let { m ->
        EditMeetingDialog(
            meeting = m,
            onDismiss = { editTarget = null },
            onSave = { t, n, millis, mins -> editTarget = null; onEditMeeting(m, t, n, millis, mins) }
        )
    }
    cancelTarget?.let { m ->
        ConfirmDialog(
            title = "Cancel \"${m.title}\"?",
            message = "Everyone invited will see it as cancelled.",
            confirmLabel = "Cancel meeting",
            onConfirm = { cancelTarget = null; onCancelMeeting(m) },
            onDismiss = { cancelTarget = null }
        )
    }

    val iconBg = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEAF4E8)
    val iconTint = if (colors.isDark) WiseLime else Color(0xFF335832)

    val currentMillis = remember { System.currentTimeMillis() }
    val filteredMeetings = remember(meetings, selectedTab) {
        when (selectedTab) {
            "Upcoming" -> meetings.filter { m ->
                val meetingTime = m.scheduledAt?.toDate()?.time ?: Long.MAX_VALUE
                (meetingTime >= currentMillis - 3600_000L || m.scheduledAt == null) &&
                        m.status.equals("Confirmed", ignoreCase = true)
            }
            "Pending" -> meetings.filter { m ->
                m.status.equals("Pending", ignoreCase = true) ||
                        m.status.equals("Awaiting response", ignoreCase = true)
            }
            "Past" -> meetings.filter { m ->
                val meetingTime = m.scheduledAt?.toDate()?.time ?: 0L
                (meetingTime < currentMillis - 3600_000L && m.scheduledAt != null) ||
                        m.status.equals("Cancelled", ignoreCase = true)
            }
            else -> meetings
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAmountY = 0f },
                    onDragEnd = {
                        if (dragAmountY > 35f) {
                            onClose()
                        }
                        dragAmountY = 0f
                    },
                    onDragCancel = { dragAmountY = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f) {
                            dragAmountY += dragAmount
                        }
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
        )

        // Header: "Scheduled meetings", "With $clientName", Mint badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scheduled meetings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "With $clientName",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
                    .testTag("scheduled_meetings_badge"),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Calendar",
                        tint = iconTint,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopStart)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(11.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Upcoming, Pending, Past
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Upcoming", "Pending", "Past").forEach { tab ->
                val isSelected = selectedTab == tab
                Text(
                    text = tab,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.textPrimary else colors.textSecondary,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = tab }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("scheduled_tab_$tab")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredMeetings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No $selectedTab meetings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No meetings scheduled with $clientName.",
                        fontSize = 12.5.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    if (selectedTab == "Upcoming") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            onClick = onScheduleNewMeeting,
                            shape = RoundedCornerShape(9999.dp),
                            color = WiseLime,
                            modifier = Modifier.height(38.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Schedule meeting",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Meeting Cards List
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredMeetings.forEach { meeting ->
                    val monthStr = meeting.getDisplayMonth()
                    val dayStr = meeting.getDisplayDay()
                    val timeStr = meeting.getDisplayTime()

                    val itemData = ScheduledMeetingItemData(
                        id = meeting.id,
                        month = monthStr,
                        day = dayStr,
                        title = meeting.title,
                        time = timeStr,
                        duration = meeting.duration,
                        status = meeting.status,
                        isConfirmed = meeting.status.equals("Confirmed", ignoreCase = true)
                    )

                    val canManage = meeting.organizerUid == currentUid &&
                        !meeting.status.equals("Cancelled", ignoreCase = true)
                    SwipeRevealRow(
                        isOpen = openSwipeId == meeting.id,
                        onOpenChange = { open ->
                            if (open) openSwipeId = meeting.id else if (openSwipeId == meeting.id) openSwipeId = null
                        },
                        actions = if (canManage) listOf(
                            SwipeAction(Icons.Outlined.Edit, "Edit meeting", WiseLime, DarkGreen, "swipe_action_edit_meeting") { editTarget = meeting },
                            SwipeAction(Icons.Outlined.Close, "Cancel meeting", colors.cardBackground, com.example.ui.theme.SentimentNegative, "swipe_action_cancel_meeting") { cancelTarget = meeting }
                        ) else emptyList(),
                        fromStart = true,
                        enabled = canManage
                    ) {
                        ScheduledMeetingCard(
                            meeting = itemData,
                            onClick = {}
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun ScheduledMeetingCard(
    meeting: ScheduledMeetingItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "meeting_card_scale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (colors.isDark) Color(0xFF2E322A) else Color(0xFFDFE3DA)
        ),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .height(86.dp)
            .testTag("scheduled_card_${meeting.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Squircle Tile
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (colors.isDark) Color(0xFF252922) else Color(0xFFEFF3EA)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = meeting.month,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) WiseLime else Color(0xFF335832)
                    )
                    Text(
                        text = meeting.day,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = meeting.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${meeting.time} · ${meeting.duration}",
                    fontSize = 12.5.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (meeting.isConfirmed) {
                        if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)
                    } else {
                        if (colors.isDark) Color(0xFF242721) else Color(0xFFEFF1EA)
                    },
                    border = if (meeting.isConfirmed) {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (colors.isDark) WiseLime.copy(alpha = 0.35f) else Color(0xFFC7ECB0)
                        )
                    } else null
                ) {
                    Text(
                        text = meeting.status,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (meeting.isConfirmed) {
                            if (colors.isDark) WiseLime else DarkGreen
                        } else {
                            colors.textSecondary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
