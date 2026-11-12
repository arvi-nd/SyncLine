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
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.theme.GlobalChatCustomColors
import com.example.ui.theme.LocalGlobalChatColors
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.NearBlack
import com.example.ui.theme.WiseLime
import com.example.model.getDisplayMonth
import com.example.model.getDisplayDay
import com.example.model.getDisplayTime

enum class BottomNavDestination(val label: String) {
    HOME("Home"),
    CHAT("Chat"),
    ACTIVITY("Activity"),
    ACCOUNT("Account")
}

@Composable
fun BottomNavBar(
    currentDestination: BottomNavDestination,
    onNavigate: (BottomNavDestination) -> Unit,
    onPlusClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    isChatMode: Boolean = currentDestination == BottomNavDestination.CHAT,
    chatText: String = "",
    onChatTextChange: (String) -> Unit = {},
    onSendMessage: () -> Unit = {},
    onActionPillClick: (String) -> Unit = {},
    onConfirmAddClient: (username: String, onError: (String) -> Unit, onSuccess: (clientUid: String) -> Unit) -> Unit = { _, _, _ -> },
    clientName: String = "Sarah Jenkins",
    clientUid: String = "",
    projectsList: List<ProjectItemData> = emptyList(),
    meetingsList: List<com.example.model.Meeting> = emptyList(),
    invoicesList: List<com.example.model.Invoice> = emptyList(),
    currentUid: String? = null,
    onEditMeeting: (com.example.model.Meeting, String, String, Long, Int) -> Unit = { _, _, _, _, _ -> },
    onCancelMeeting: (com.example.model.Meeting) -> Unit = {},
    onScheduleMeetingDirect: (title: String, date: String, time: String, duration: String, durationMinutes: Int, notes: String, scheduledAtMillis: Long?, meetingId: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onCreateInvoiceDirect: (title: String, clientUid: String, clientName: String, projectId: String?, projectName: String?, amount: Double, currencyCode: String, dueDate: String, notes: String?, attemptId: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ -> }
) {
    // Light mode wears the light reference palette; dark mode keeps the app's dark palette with the same shapes
    val bar: @Composable () -> Unit = {
        BottomNavBarContent(
            currentDestination = currentDestination,
            onNavigate = onNavigate,
            onPlusClick = onPlusClick,
            onSearchClick = onSearchClick,
            modifier = modifier,
            isChatMode = isChatMode,
            chatText = chatText,
            onChatTextChange = onChatTextChange,
            onSendMessage = onSendMessage,
            onActionPillClick = onActionPillClick,
            onConfirmAddClient = onConfirmAddClient,
            clientName = clientName,
            clientUid = clientUid,
            projectsList = projectsList,
            meetingsList = meetingsList,
            invoicesList = invoicesList,
            currentUid = currentUid,
            onEditMeeting = onEditMeeting,
            onCancelMeeting = onCancelMeeting,
            onScheduleMeetingDirect = onScheduleMeetingDirect,
            onCreateInvoiceDirect = onCreateInvoiceDirect
        )
    }
    if (GlobalChatTheme.colors.isDark) {
        bar()
    } else {
        val base = GlobalChatTheme.colors
        CompositionLocalProvider(
            LocalGlobalChatColors provides base.copy(
                cardBackground = Color(0xFFF3F5EE), // rows and tiles inside the white bar and sheets
                cardBorder = Color(0xFFD4D8CD)
            )
        ) {
            bar()
        }
    }
}

@Composable
private fun BottomNavBarContent(
    currentDestination: BottomNavDestination,
    onNavigate: (BottomNavDestination) -> Unit,
    onPlusClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    isChatMode: Boolean = currentDestination == BottomNavDestination.CHAT,
    chatText: String = "",
    onChatTextChange: (String) -> Unit = {},
    onSendMessage: () -> Unit = {},
    onActionPillClick: (String) -> Unit = {},
    onConfirmAddClient: (username: String, onError: (String) -> Unit, onSuccess: (clientUid: String) -> Unit) -> Unit = { _, _, _ -> },
    clientName: String = "Sarah Jenkins",
    clientUid: String = "",
    projectsList: List<ProjectItemData> = emptyList(),
    meetingsList: List<com.example.model.Meeting> = emptyList(),
    invoicesList: List<com.example.model.Invoice> = emptyList(),
    currentUid: String? = null,
    onEditMeeting: (com.example.model.Meeting, String, String, Long, Int) -> Unit = { _, _, _, _, _ -> },
    onCancelMeeting: (com.example.model.Meeting) -> Unit = {},
    onScheduleMeetingDirect: (title: String, date: String, time: String, duration: String, durationMinutes: Int, notes: String, scheduledAtMillis: Long?, meetingId: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onCreateInvoiceDirect: (title: String, clientUid: String, clientName: String, projectId: String?, projectName: String?, amount: Double, currencyCode: String, dueDate: String, notes: String?, attemptId: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ -> }
) {
    val colors = GlobalChatTheme.colors

    val appleSpring = remember {
        spring<Dp>(
            dampingRatio = 0.68f,
            stiffness = 380f
        )
    }

    val largeMorphSpring = remember {
        spring<Dp>(
            dampingRatio = 0.72f,
            stiffness = 380f
        )
    }

    val actionPills = remember {
        listOf(
            ChatActionPillItem("attach_file", "Attach file", Icons.Outlined.AttachFile, isPrimary = true),
            ChatActionPillItem("project", "Project", Icons.Outlined.WorkOutline),
            ChatActionPillItem("schedule", "Schedule", Icons.Outlined.Schedule),
            ChatActionPillItem("invoice", "Invoice", Icons.Outlined.ReceiptLong)
        )
    }

    var expandedPillMode by remember { mutableStateOf<String?>(null) }
    var lastActivePillMode by remember { mutableStateOf<String>("attach_file") }
    var searchResultCount by remember { androidx.compose.runtime.mutableIntStateOf(5) }

    if (expandedPillMode != null) {
        lastActivePillMode = expandedPillMode!!
    }

    if (!isChatMode && expandedPillMode != null && expandedPillMode != "search" && expandedPillMode != "create" && expandedPillMode != "add_client") {
        expandedPillMode = null
    }

    BackHandler(enabled = expandedPillMode != null) {
        when (expandedPillMode) {
            "share_link" -> expandedPillMode = "attach_file"
            "create_project" -> expandedPillMode = "project"
            "new_meeting" -> expandedPillMode = "schedule"
            "past_invoices" -> expandedPillMode = "invoice"
            "add_client" -> expandedPillMode = "create"
            else -> expandedPillMode = null
        }
    }

    val context = LocalContext.current

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val (name, size, ext) = extractFileInfo(context, uri, defaultName = "Document.pdf", defaultExt = "PDF")
            expandedPillMode = null
            onActionPillClick("file_attached:$name:$size:$ext")
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val (name, size, ext) = extractFileInfo(context, uri, defaultName = "Photo.jpg", defaultExt = "JPG")
            expandedPillMode = null
            onActionPillClick("photo_attached:$name:$size:$ext")
        }
    }

    val targetCardHeight = when {
        expandedPillMode == "search" -> when {
            searchResultCount <= 0 -> 220.dp
            searchResultCount == 1 -> 285.dp
            searchResultCount == 2 -> 350.dp
            searchResultCount == 3 -> 415.dp
            searchResultCount == 4 -> 480.dp
            else -> 545.dp
        }
        expandedPillMode == "create" -> 160.dp
        expandedPillMode == "add_client" -> 218.dp
        isChatMode && (expandedPillMode == "invoice" || expandedPillMode == "create_project") -> 630.dp
        isChatMode && expandedPillMode == "new_meeting" -> 490.dp
        isChatMode && expandedPillMode == "past_invoices" -> 440.dp
        isChatMode && (expandedPillMode == "project" || expandedPillMode == "scheduled_meetings") -> 390.dp
        isChatMode && (expandedPillMode == "schedule" || expandedPillMode == "share_link") -> 270.dp
        isChatMode && expandedPillMode == "attach_file" -> 154.dp
        isChatMode -> 116.dp
        else -> 128.dp
    }

    val targetPillTopOffset = when {
        expandedPillMode == "search" -> when {
            searchResultCount <= 0 -> 160.dp
            searchResultCount == 1 -> 225.dp
            searchResultCount == 2 -> 290.dp
            searchResultCount == 3 -> 355.dp
            searchResultCount == 4 -> 420.dp
            else -> 485.dp
        }
        expandedPillMode == "create" -> 100.dp
        expandedPillMode == "add_client" -> 158.dp
        isChatMode && (expandedPillMode == "invoice" || expandedPillMode == "create_project") -> 570.dp
        isChatMode && expandedPillMode == "new_meeting" -> 430.dp
        isChatMode && expandedPillMode == "past_invoices" -> 380.dp
        isChatMode && (expandedPillMode == "project" || expandedPillMode == "scheduled_meetings") -> 330.dp
        isChatMode && (expandedPillMode == "schedule" || expandedPillMode == "share_link") -> 210.dp
        isChatMode && expandedPillMode == "attach_file" -> 94.dp
        isChatMode -> 58.dp
        else -> 12.dp
    }

    val cardHeight by animateDpAsState(
        targetValue = targetCardHeight,
        animationSpec = largeMorphSpring,
        label = "card_height"
    )

    val pillTopOffset by animateDpAsState(
        targetValue = targetPillTopOffset,
        animationSpec = largeMorphSpring,
        label = "pill_top_offset"
    )

    val navRowOffsetY by animateDpAsState(
        targetValue = if (isChatMode || expandedPillMode != null) 80.dp else 0.dp,
        animationSpec = appleSpring,
        label = "nav_row_offset_y"
    )

    val navRowAlpha by animateFloatAsState(
        targetValue = if (isChatMode || expandedPillMode != null) 0f else 1f,
        animationSpec = tween(
            durationMillis = if (isChatMode || expandedPillMode != null) 180 else 240,
            easing = FastOutSlowInEasing
        ),
        label = "nav_row_alpha"
    )

    val insets = WindowInsets.ime.union(WindowInsets.navigationBars)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(insets)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val cardShape = remember { RoundedCornerShape(32.dp) }
        val cardBorder = remember(colors.isDark) {
            if (colors.isDark) androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.14f))
            else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4D8CD))
        }
        val sideGlowColor = remember(colors.isDark) {
            if (colors.isDark) {
                WiseLime.copy(alpha = 0.16f)
            } else {
                WiseLime.copy(alpha = 0.22f)
            }
        }

        Surface(
            shape = cardShape,
            color = if (colors.isDark) colors.cardBackground else Color.White,
            border = cardBorder,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .shadow(
                    elevation = if (colors.isDark) 0.dp else 16.dp,
                    shape = cardShape,
                    clip = false,
                    ambientColor = Color(0x400E0F0C),
                    spotColor = Color(0x400E0F0C)
                )
                .graphicsLayer {
                    clip = true
                    shape = cardShape
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Top Row: Action Pills (Attach file, Project, Schedule, Invoice) in active chat mode
                AnimatedVisibility(
                    visible = isChatMode && expandedPillMode == null,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = 0.58f,
                            stiffness = 320f
                        ),
                        initialOffsetY = { -it / 2 }
                    ) + fadeIn(
                        animationSpec = tween(180, delayMillis = 30, easing = FastOutSlowInEasing)
                    ),
                    exit = slideOutVertically(
                        animationSpec = spring(
                            dampingRatio = 0.58f,
                            stiffness = 320f
                        ),
                        targetOffsetY = { -it / 2 }
                    ) + fadeOut(
                        animationSpec = tween(130, easing = FastOutSlowInEasing)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = 12.dp.toPx()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        actionPills.forEach { pill ->
                            ChatActionPill(
                                item = pill,
                                onClick = {
                                    if (pill.id == "project") {
                                        expandedPillMode = if (expandedPillMode == "project") null else "project"
                                    } else if (pill.id == "attach_file") {
                                        expandedPillMode = if (expandedPillMode == "attach_file") null else "attach_file"
                                    } else if (pill.id == "schedule") {
                                        expandedPillMode = if (expandedPillMode == "schedule") null else "schedule"
                                    } else if (pill.id == "invoice") {
                                        expandedPillMode = if (expandedPillMode == "invoice") null else "invoice"
                                    } else {
                                        expandedPillMode = null
                                    }
                                    onActionPillClick(pill.id)
                                }
                            )
                        }
                    }
                }

                // Expanded Projects / Create Project / Attach File / Share Link / Schedule / New Meeting / Scheduled Meetings / Invoice / Past Invoices / Search Sheet
                AnimatedVisibility(
                    visible = expandedPillMode != null,
                    enter = fadeIn(
                        animationSpec = tween(190, easing = FastOutSlowInEasing)
                    ) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = 0.78f,
                            stiffness = 380f
                        ),
                        initialScale = 0.985f,
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(140, easing = FastOutSlowInEasing)
                    ) + scaleOut(
                        animationSpec = spring(
                            dampingRatio = 0.78f,
                            stiffness = 380f
                        ),
                        targetScale = 0.985f,
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = 4.dp.toPx()
                        }
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val allSheetKeys = remember {
                            listOf(
                                "create",
                                "add_client",
                                "attach_file",
                                "share_link",
                                "project",
                                "create_project",
                                "schedule",
                                "new_meeting",
                                "scheduled_meetings",
                                "invoice",
                                "past_invoices",
                                "search"
                            )
                        }
                        val effectiveActiveSheetId = expandedPillMode
                        val sheetOrder = remember(effectiveActiveSheetId, lastActivePillMode) {
                            val active = effectiveActiveSheetId ?: lastActivePillMode
                            if (active != null && allSheetKeys.contains(active)) {
                                allSheetKeys.filter { it != active } + active
                            } else {
                                allSheetKeys
                            }
                        }

                        sheetOrder.forEach { sheetMode ->
                            key(sheetMode) {
                                RetainedMorphSheetItem(
                                    sheetId = sheetMode,
                                    activeSheetId = effectiveActiveSheetId
                                ) {
                                    when (sheetMode) {
                                        "create" -> {
                                            CreateMorphSheet(
                                                onClose = { expandedPillMode = null },
                                                onOptionSelect = { optionId ->
                                                    when (optionId) {
                                                        "new_chat" -> {
                                                            expandedPillMode = null
                                                            onActionPillClick("open_chat:c1")
                                                        }
                                                        "add_client" -> {
                                                            expandedPillMode = "add_client"
                                                        }
                                                        "new_project" -> {
                                                            expandedPillMode = null
                                                            onActionPillClick("open_project:p1")
                                                        }
                                                        "schedule_message" -> {
                                                            expandedPillMode = null
                                                            onActionPillClick("open_schedule:c1")
                                                        }
                                                        else -> {
                                                            expandedPillMode = null
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                        "add_client" -> {
                                            AddClientMorphSheet(
                                                onCancel = { expandedPillMode = "create" },
                                                onConfirmAdd = onConfirmAddClient,
                                                onClientAdded = { clientUid ->
                                                    expandedPillMode = null
                                                    onActionPillClick("open_chat:$clientUid")
                                                }
                                            )
                                        }
                                        "project" -> {
                                            ProjectsMorphSheet(
                                                clientName = clientName,
                                                onClose = { expandedPillMode = null },
                                                onAddProject = { expandedPillMode = "create_project" },
                                                onProjectSelect = { projectPayload ->
                                                    expandedPillMode = null
                                                    if (projectPayload.startsWith("p") || projectPayload.startsWith("proj")) {
                                                        onActionPillClick("open_project:$projectPayload")
                                                    } else {
                                                        onActionPillClick("project_selected:$projectPayload")
                                                    }
                                                },
                                                projects = projectsList
                                            )
                                        }
                                        "create_project" -> {
                                            CreateProjectMorphSheet(
                                                clientName = clientName,
                                                onCancel = { expandedPillMode = "project" },
                                                onCreateProject = { projName, dueDate, hexLight, hexDark, tasks ->
                                                    expandedPillMode = null
                                                    val tasksStr = tasks.joinToString("|||")
                                                    onActionPillClick("project_created:$projName:::$dueDate:::$hexLight:::$hexDark:::$tasksStr")
                                                }
                                            )
                                        }
                                        "attach_file" -> {
                                            AttachFileMorphSheet(
                                                clientName = clientName,
                                                onClose = { expandedPillMode = null },
                                                onOptionSelect = { optionId ->
                                                    when (optionId) {
                                                        "choose_file" -> {
                                                            try {
                                                                documentPickerLauncher.launch(arrayOf("*/*"))
                                                            } catch (_: Exception) {
                                                                expandedPillMode = null
                                                                onActionPillClick("attach_option:choose_file")
                                                            }
                                                        }
                                                        "photo_video" -> {
                                                            try {
                                                                photoPickerLauncher.launch(
                                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                                                )
                                                            } catch (_: Exception) {
                                                                expandedPillMode = null
                                                                onActionPillClick("attach_option:photo_video")
                                                            }
                                                        }
                                                        "share_link" -> {
                                                            expandedPillMode = "share_link"
                                                        }
                                                        else -> {
                                                            expandedPillMode = null
                                                            onActionPillClick("attach_option:$optionId")
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                        "share_link" -> {
                                            ShareLinkMorphSheet(
                                                clientName = clientName,
                                                onCancel = { expandedPillMode = "attach_file" },
                                                onSendLink = { title, url ->
                                                    expandedPillMode = null
                                                    onActionPillClick("link_shared:$title:::$url")
                                                }
                                            )
                                        }
                                        "schedule" -> {
                                            ScheduleMorphSheet(
                                                clientName = clientName,
                                                onClose = { expandedPillMode = null },
                                                onScheduleMeeting = {
                                                    expandedPillMode = "new_meeting"
                                                },
                                                onViewMeetings = {
                                                    expandedPillMode = "scheduled_meetings"
                                                }
                                            )
                                        }
                                        "new_meeting" -> {
                                            NewMeetingMorphSheet(
                                                clientName = clientName,
                                                onCancel = { expandedPillMode = "schedule" },
                                                onSchedule = { title, date, time, duration, durationMinutes, notes, scheduledAtMillis, attemptMeetingId, onSuccess, onError ->
                                                    onScheduleMeetingDirect(
                                                        title, date, time, duration, durationMinutes, notes, scheduledAtMillis, attemptMeetingId,
                                                        {
                                                            expandedPillMode = null
                                                            onSuccess()
                                                        },
                                                        onError
                                                    )
                                                }
                                            )
                                        }
                                        "scheduled_meetings" -> {
                                            ScheduledMeetingsMorphSheet(
                                                clientName = clientName,
                                                meetings = meetingsList,
                                                currentUid = currentUid,
                                                onEditMeeting = onEditMeeting,
                                                onCancelMeeting = onCancelMeeting,
                                                onClose = { expandedPillMode = null },
                                                onScheduleNewMeeting = {
                                                    expandedPillMode = "new_meeting"
                                                }
                                            )
                                        }
                                        "invoice" -> {
                                            CreateInvoiceMorphSheet(
                                                clientName = clientName,
                                                clientUid = clientUid,
                                                projectsList = projectsList,
                                                onClose = { expandedPillMode = null },
                                                onViewPastInvoices = { expandedPillMode = "past_invoices" },
                                                onCreateInvoiceDirect = { title, cUid, cName, pId, pName, amt, currCode, dDate, nts, attemptId, onSuccess, onError ->
                                                    onCreateInvoiceDirect(
                                                        title, cUid, cName, pId, pName, amt, currCode, dDate, nts, attemptId,
                                                        {
                                                            expandedPillMode = null
                                                            onSuccess()
                                                        },
                                                        onError
                                                    )
                                                }
                                            )
                                        }
                                        "past_invoices" -> {
                                            PastInvoicesMorphSheet(
                                                clientName = clientName,
                                                invoices = invoicesList,
                                                onClose = { expandedPillMode = null },
                                                onInvoiceSelect = { inv ->
                                                    expandedPillMode = null
                                                    onActionPillClick("invoice_created:${inv.title}:::${inv.projectName ?: ""}:::${inv.amount}:::${inv.currencyCode}:::${inv.dueDate}:::${inv.notes ?: ""}:::${inv.id}")
                                                }
                                            )
                                        }
                                        "search" -> {
                                            SearchMorphSheet(
                                                onClose = { expandedPillMode = null },
                                                onResultSelect = { payload ->
                                                    expandedPillMode = null
                                                    if (payload.startsWith("open_chat:")) {
                                                        onActionPillClick("open_chat:${payload.removePrefix("open_chat:")}")
                                                    } else if (payload.startsWith("action:")) {
                                                        val actionId = payload.removePrefix("action:")
                                                        if (actionId == "invoice") expandedPillMode = "invoice"
                                                        else if (actionId == "schedule") expandedPillMode = "schedule"
                                                        else if (actionId == "create_project") expandedPillMode = "create_project"
                                                        else if (actionId == "share_link") expandedPillMode = "share_link"
                                                        else if (actionId == "attach_file") expandedPillMode = "attach_file"
                                                    } else {
                                                        onActionPillClick(payload)
                                                    }
                                                },
                                                onCountChanged = { count ->
                                                    searchResultCount = count
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Top Search Pill Bar / Chat Text Box with animated Send Button
                // Hides by sliding down when any menu/pill is opened, reappears when menu closes!
                AnimatedVisibility(
                    visible = expandedPillMode == null,
                    enter = slideInVertically(
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
                        initialOffsetY = { it }
                    ) + fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)),
                    exit = slideOutVertically(
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
                        targetOffsetY = { it }
                    ) + fadeOut(animationSpec = tween(130, easing = FastOutSlowInEasing)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp)
                        .graphicsLayer {
                            translationY = pillTopOffset.toPx()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // Search Bar or Chat Input Field
                    Surface(
                        onClick = {
                            if (!isChatMode) {
                                expandedPillMode = "search"
                            }
                        },
                        enabled = !isChatMode,
                        shape = RoundedCornerShape(9999.dp),
                        color = if (colors.isDark) Color(0xFF222620) else Color(0xFFF1F3EC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (colors.isDark) Color(0xFF31362D) else Color(0xFFD9DDD2)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag(if (isChatMode) "bottom_nav_chat_input_container" else "bottom_nav_search_bar")
                    ) {
                        AnimatedContent(
                            targetState = isChatMode,
                            contentAlignment = Alignment.CenterStart,
                            transitionSpec = {
                                if (targetState) {
                                    // Switching from search to chat: search text slides down out, chat text glides down into place with spring bounce
                                    (slideInVertically(
                                        animationSpec = spring(
                                            dampingRatio = 0.58f,
                                            stiffness = 320f
                                        )
                                    ) { -it / 2 } + fadeIn(animationSpec = tween(180, delayMillis = 30, easing = FastOutSlowInEasing)))
                                        .togetherWith(
                                            slideOutVertically(
                                                animationSpec = spring(
                                                    dampingRatio = 0.58f,
                                                    stiffness = 320f
                                                )
                                            ) { it / 2 } + fadeOut(animationSpec = tween(130, easing = FastOutSlowInEasing))
                                        )
                                } else {
                                    // Switching back from chat to search: chat text slides up out, search text glides up into place with spring bounce
                                    (slideInVertically(
                                        animationSpec = spring(
                                            dampingRatio = 0.58f,
                                            stiffness = 320f
                                        )
                                    ) { it / 2 } + fadeIn(animationSpec = tween(180, delayMillis = 30, easing = FastOutSlowInEasing)))
                                        .togetherWith(
                                            slideOutVertically(
                                                animationSpec = spring(
                                                    dampingRatio = 0.58f,
                                                    stiffness = 320f
                                                )
                                            ) { -it / 2 } + fadeOut(animationSpec = tween(130, easing = FastOutSlowInEasing))
                                        )
                                }
                            },
                            label = "search_chat_transition",
                            modifier = Modifier.fillMaxSize()
                        ) { inChat ->
                            if (inChat) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 12.dp, end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Chat",
                                        tint = if (colors.isDark) WiseLime else NearBlack,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (chatText.isEmpty()) {
                                             Text(
                                                text = "Type a message...",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = colors.textSecondary
                                            )
                                        }

                                        BasicTextField(
                                            value = chatText,
                                            onValueChange = onChatTextChange,
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = colors.textPrimary
                                            ),
                                            cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                            keyboardActions = KeyboardActions(
                                                onSend = { onSendMessage() }
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("chat_message_input")
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 12.dp, end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Search",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Search clients, projects, messages",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Send Button: Pure vertical slide and scale with bouncy Apple spring (strictly no left or right animation)
                    AnimatedVisibility(
                        visible = isChatMode,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = 0.58f,
                                stiffness = 320f
                            ),
                            initialOffsetY = { -it / 2 }
                        ) + fadeIn(
                            animationSpec = tween(180, delayMillis = 30, easing = FastOutSlowInEasing)
                        ) + scaleIn(
                            animationSpec = spring(
                                dampingRatio = 0.58f,
                                stiffness = 320f
                            ),
                            initialScale = 0.70f
                        ),
                        exit = slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = 0.58f,
                                stiffness = 320f
                            ),
                            targetOffsetY = { -it / 2 }
                        ) + fadeOut(
                            animationSpec = tween(130, easing = FastOutSlowInEasing)
                        ) + scaleOut(
                            animationSpec = spring(
                                dampingRatio = 0.58f,
                                stiffness = 320f
                            ),
                            targetScale = 0.70f
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(8.dp))

                            val sendInteractionSource = remember { MutableInteractionSource() }
                            val isPressed by sendInteractionSource.collectIsPressedAsState()

                            val sendBtnScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.88f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                ),
                                label = "send_btn_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(sendBtnScale)
                                    .clip(CircleShape)
                                    .background(WiseLime)
                                    .border(
                                        1.dp,
                                        if (colors.isDark) Color(0xFF8CD860) else Color(0xFF88D45A),
                                        CircleShape
                                    )
                                    .clickable(
                                        interactionSource = sendInteractionSource,
                                        indication = null
                                    ) {
                                        onSendMessage()
                                    }
                                    .testTag("chat_send_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = DarkGreen,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }

                // Bottom Navigation Row: Home, Chat, Radiant Lime Plus Pill, Activity, Account
                // Pinned to the bottom half, slides DOWN and fades out!
                if (navRowAlpha > 0.005f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(start = 14.dp, end = 14.dp, top = 0.dp, bottom = 10.dp)
                            .graphicsLayer {
                                translationY = navRowOffsetY.toPx()
                                alpha = navRowAlpha
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 1. Home
                            NavActionItem(
                                label = "Home",
                                isSelected = currentDestination == BottomNavDestination.HOME,
                                onClick = { onNavigate(BottomNavDestination.HOME) },
                                icon = { isSel, col ->
                                    WiseHomeIcon(
                                        tint = if (isSel) colors.textPrimary else colors.textSecondary,
                                        slitColor = colors.cardBackground,
                                        isSelected = isSel
                                    )
                                },
                                testTag = "nav_item_home"
                            )

                            // 2. Chat
                            NavActionItem(
                                label = "Chat",
                                isSelected = currentDestination == BottomNavDestination.CHAT,
                                onClick = { onNavigate(BottomNavDestination.CHAT) },
                                icon = { isSel, col ->
                                    WiseChatBubbleIcon(
                                        tint = if (isSel) colors.textPrimary else if (colors.isDark) Color(0xFF6E7369) else Color(0xFF8F948A)
                                    )
                                },
                                testTag = "nav_item_chat"
                            )

                            // 3. Central Glowing Lime Plus Pill Action
                            RadiantLimePlusPill(
                                onClick = {
                                    expandedPillMode = if (expandedPillMode == "create") null else "create"
                                },
                                testTag = "nav_item_plus_pill"
                            )

                            // 4. Activity
                            NavActionItem(
                                label = "Activity",
                                isSelected = currentDestination == BottomNavDestination.ACTIVITY,
                                onClick = { onNavigate(BottomNavDestination.ACTIVITY) },
                                icon = { isSel, col ->
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = "Activity",
                                        tint = if (isSel) colors.textPrimary else if (colors.isDark) Color(0xFF7A7F75) else Color(0xFF8F948A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                testTag = "nav_item_activity"
                            )

                            // 5. Account
                            NavActionItem(
                                label = "Account",
                                isSelected = currentDestination == BottomNavDestination.ACCOUNT,
                                onClick = { onNavigate(BottomNavDestination.ACCOUNT) },
                                icon = { isSel, col ->
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Account",
                                        tint = if (isSel) colors.textPrimary else if (colors.isDark) Color(0xFF8A8F86) else Color(0xFF8F948A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                testTag = "nav_item_account"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RetainedMorphSheetItem(
    sheetId: String,
    activeSheetId: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isActive = activeSheetId == sheetId
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = if (isActive) {
            tween(durationMillis = 160, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 120, easing = FastOutSlowInEasing)
        },
        label = "retained_sheet_alpha_$sheetId"
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.985f,
        animationSpec = spring(dampingRatio = 0.80f, stiffness = 420f),
        label = "retained_sheet_scale_$sheetId"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isActive) 0.dp else 6.dp,
        animationSpec = spring(dampingRatio = 0.80f, stiffness = 420f),
        label = "retained_sheet_offset_$sheetId"
    )

    if (alpha > 0.005f || isActive) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                    this.translationY = offsetY.toPx()
                }
        ) {
            content()
        }
    }
}

@Composable
fun RadiantLimePlusPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "plus_pill"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 900f),
        label = "plus_pill_scale"
    )

    // Soft at the ends, most saturated in the middle, like the reference
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFDFF65C),
            Color(0xFFBDF53A),
            Color(0xFFB4F32F),
            Color(0xFFBDF53A),
            Color(0xFFDFF65C)
        )
    )
    val shape = RoundedCornerShape(9999.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .width(80.dp)
            .height(46.dp)
            .shadow(14.dp, shape, ambientColor = WiseLime, spotColor = WiseLime)
            .clip(shape)
            .background(gradientBrush)
            .border(1.5.dp, Color.White.copy(alpha = 0.7f), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val strokeW = 2.dp.toPx()
            val c = size.width / 2f
            drawLine(color = NearBlack, start = Offset(0f, c), end = Offset(size.width, c), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(color = NearBlack, start = Offset(c, 0f), end = Offset(c, size.height), strokeWidth = strokeW, cap = StrokeCap.Round)
        }
    }
}

@Composable
internal fun NavActionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Boolean, Color) -> Unit,
    testTag: String
) {
    val colors = GlobalChatTheme.colors
    val contentColor = if (isSelected) {
        colors.textPrimary
    } else {
        colors.textSecondary
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 900f),
        label = "nav_item_press"
    )

    Column(
        modifier = Modifier
            .scale(pressScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(isSelected, contentColor)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Custom cottage house icon matching the image:
 * Solid silhouette with pointed roof and small center vertical slit.
 */
@Composable
fun WiseHomeIcon(
    tint: Color = NearBlack,
    slitColor: Color = Color.White,
    isSelected: Boolean = true,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.88f, h * 0.38f)
            lineTo(w * 0.88f, h * 0.82f)
            quadraticTo(w * 0.88f, h * 0.92f, w * 0.78f, h * 0.92f)
            lineTo(w * 0.22f, h * 0.92f)
            quadraticTo(w * 0.12f, h * 0.92f, w * 0.12f, h * 0.82f)
            lineTo(w * 0.12f, h * 0.38f)
            close()
        }
        drawPath(path, color = tint)

        // White vertical slit in the middle
        drawRoundRect(
            color = slitColor,
            topLeft = Offset(w * 0.44f, h * 0.48f),
            size = Size(w * 0.12f, h * 0.25f),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )
    }
}

/**
 * Custom chat speech bubble icon with two message lines matching the image.
 */
@Composable
fun WiseChatBubbleIcon(
    tint: Color = Color(0xFF9BA098),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = tint,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.44f)
        )

        val tail = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.24f, h * 0.60f)
            lineTo(w * 0.14f, h * 0.82f)
            lineTo(w * 0.40f, h * 0.76f)
            close()
        }
        drawPath(tail, color = tint)

        // White message lines
        drawLine(
            color = Color.White,
            start = Offset(w * 0.36f, h * 0.38f),
            end = Offset(w * 0.62f, h * 0.38f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.36f, h * 0.50f),
            end = Offset(w * 0.52f, h * 0.50f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

data class ChatActionPillItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isPrimary: Boolean = false
)

@Composable
fun ChatActionPill(
    item: ChatActionPillItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "action_pill_scale"
    )

    val bgColor = if (item.isPrimary) {
        if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)
    } else {
        if (colors.isDark) Color(0xFF20231D) else Color(0xFFE8EBE3)
    }

    val contentColor = if (item.isPrimary) {
        if (colors.isDark) WiseLime else DarkGreen
    } else {
        if (colors.isDark) Color(0xFFE2E6DD) else NearBlack
    }

    val borderColor = if (item.isPrimary) {
        if (colors.isDark) WiseLime.copy(alpha = 0.35f) else Color(0xFFC7ECB0)
    } else {
        if (colors.isDark) Color(0xFF31362D) else Color(0xFFDFE3DA)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
            .scale(scale)
            .height(36.dp)
            .testTag("action_pill_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = item.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
