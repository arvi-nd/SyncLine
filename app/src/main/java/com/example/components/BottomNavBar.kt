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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
    clientName: String = "Sarah Jenkins"
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
        isChatMode -> 114.dp
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
        isChatMode -> 52.dp
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val cardShape = remember { RoundedCornerShape(28.dp) }
        val cardBorder = remember(colors.cardBorder) {
            androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
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
            color = colors.cardBackground,
            border = cardBorder,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .graphicsLayer {
                    clip = true
                    shape = cardShape
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val glowDepth = 24.dp.toPx()
                        val subtleGlowColor = if (colors.isDark) {
                            WiseLime.copy(alpha = 0.05f)
                        } else {
                            WiseLime.copy(alpha = 0.07f)
                        }

                        // Top side inner glow
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    subtleGlowColor,
                                    subtleGlowColor.copy(alpha = subtleGlowColor.alpha * 0.35f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = glowDepth
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, glowDepth)
                        )

                        // Bottom side inner glow
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    subtleGlowColor.copy(alpha = subtleGlowColor.alpha * 0.35f),
                                    subtleGlowColor
                                ),
                                startY = size.height - glowDepth,
                                endY = size.height
                            ),
                            topLeft = Offset(0f, size.height - glowDepth),
                            size = Size(size.width, glowDepth)
                        )

                        // Left side inner glow
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    subtleGlowColor,
                                    subtleGlowColor.copy(alpha = subtleGlowColor.alpha * 0.35f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = glowDepth
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(glowDepth, size.height)
                        )

                        // Right side inner glow
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    subtleGlowColor.copy(alpha = subtleGlowColor.alpha * 0.35f),
                                    subtleGlowColor
                                ),
                                startX = size.width - glowDepth,
                                endX = size.width
                            ),
                            topLeft = Offset(size.width - glowDepth, 0f),
                            size = Size(glowDepth, size.height)
                        )

                        // Draw content on top of the subtle all-sides inner glow
                        drawContent()
                    }
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
                            translationY = 8.dp.toPx()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
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
                                                onProjectSelect = { projectTitle ->
                                                    expandedPillMode = null
                                                    onActionPillClick("project_selected:$projectTitle")
                                                }
                                            )
                                        }
                                        "create_project" -> {
                                            CreateProjectMorphSheet(
                                                clientName = clientName,
                                                onCancel = { expandedPillMode = "project" },
                                                onCreateProject = { projectName, hexLight, hexDark ->
                                                    expandedPillMode = null
                                                    onActionPillClick("project_created:$projectName:$hexLight:$hexDark")
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
                                                onSchedule = { title, date, time, duration, notes ->
                                                    expandedPillMode = null
                                                    onActionPillClick("meeting_scheduled:$title:$date:$time:$duration:$notes")
                                                }
                                            )
                                        }
                                        "scheduled_meetings" -> {
                                            ScheduledMeetingsMorphSheet(
                                                clientName = clientName,
                                                onClose = { expandedPillMode = null },
                                                onMeetingSelect = { mTitle, mDate, mTime, mDuration, mStatus ->
                                                    expandedPillMode = null
                                                    onActionPillClick("meeting_scheduled:$mTitle:$mDate:$mTime:$mDuration")
                                                }
                                            )
                                        }
                                        "invoice" -> {
                                            CreateInvoiceMorphSheet(
                                                clientName = clientName,
                                                onClose = { expandedPillMode = null },
                                                onViewPastInvoices = { expandedPillMode = "past_invoices" },
                                                onCreateInvoice = { title, project, amount, dueDate, notes ->
                                                    expandedPillMode = null
                                                    onActionPillClick("invoice_created:$title:::$project:::$amount:::$dueDate:::$notes")
                                                }
                                            )
                                        }
                                        "past_invoices" -> {
                                            PastInvoicesMorphSheet(
                                                clientName = clientName,
                                                onClose = { expandedPillMode = null },
                                                onInvoiceSelect = { inv ->
                                                    expandedPillMode = null
                                                    onActionPillClick("invoice_created:${inv.title}:::${inv.project}:::${inv.amount}:::${inv.dueDate}:::${inv.notes}")
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
                        .padding(start = 8.dp, end = 10.dp)
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
                        color = if (colors.isDark) Color(0xFF222620) else Color(0xFFF1F3EE),
                        border = if (isChatMode) {
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (colors.isDark) Color(0xFF333830) else Color(0xFFE2E6DD)
                            )
                        } else null,
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
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Search for actions, people, instruments",
                                        fontSize = 13.5.sp,
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
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.92f else 1.0f,
                                animationSpec = spring(stiffness = 500f),
                                label = "send_btn_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(scale)
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
                                    imageVector = Icons.AutoMirrored.Filled.Send,
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
                                        tint = if (isSel) (if (colors.isDark) WiseLime else NearBlack) else colors.textSecondary,
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
                                        tint = if (isSel) (if (colors.isDark) WiseLime else NearBlack) else Color(0xFF9BA098)
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
                                        tint = if (isSel) (if (colors.isDark) WiseLime else NearBlack) else Color(0xFF9BA098),
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
                                        tint = if (isSel) (if (colors.isDark) WiseLime else NearBlack) else Color(0xFF9BA098),
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
private fun RetainedMorphSheetItem(
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
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "plus_pill_scale"
    )

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFE2FF66), // bright neon lemon lime
            Color(0xFFA5FA38), // intense vibrant lime
            Color(0xFFB4FF48)  // radiant lime highlight
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
            .width(74.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(gradientBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val strokeW = 2.dp.toPx()
            val c = size.width / 2f
            // Horizontal bar
            drawLine(
                color = NearBlack,
                start = Offset(0f, c),
                end = Offset(size.width, c),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            // Vertical bar
            drawLine(
                color = NearBlack,
                start = Offset(c, 0f),
                end = Offset(c, size.height),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun NavActionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Boolean, Color) -> Unit,
    testTag: String
) {
    val colors = GlobalChatTheme.colors
    val contentColor = if (isSelected) {
        if (colors.isDark) WiseLime else NearBlack
    } else {
        colors.textSecondary
    }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
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
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
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
            color = Color.White,
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

data class ProjectItemData(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBgColorLight: Color,
    val iconBgColorDark: Color,
    val iconTintLight: Color,
    val iconTintDark: Color
)

@Composable
fun ProjectsMorphSheet(
    clientName: String,
    onClose: () -> Unit,
    onAddProject: () -> Unit = {},
    onProjectSelect: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    val sampleProjects = remember {
        listOf(
            ProjectItemData(
                title = "Brand Film Edit",
                subtitle = "Due Oct 30 · 65% complete",
                icon = Icons.Outlined.Movie,
                iconBgColorLight = Color(0xFFE2F6D5),
                iconBgColorDark = Color(0xFF2B3A23),
                iconTintLight = DarkGreen,
                iconTintDark = WiseLime
            ),
            ProjectItemData(
                title = "Brand Identity",
                subtitle = "Due Nov 4 · 40% complete",
                icon = Icons.Outlined.Palette,
                iconBgColorLight = Color(0xFFE1F0FA),
                iconBgColorDark = Color(0xFF1E2F3C),
                iconTintLight = Color(0xFF0F5A8A),
                iconTintDark = Color(0xFF73C2F5)
            ),
            ProjectItemData(
                title = "Social Media Reels",
                subtitle = "Due Nov 8 · 20% complete",
                icon = Icons.Outlined.OndemandVideo,
                iconBgColorLight = Color(0xFFFCEBD9),
                iconBgColorDark = Color(0xFF3B2B1E),
                iconTintLight = Color(0xFF8C4A0F),
                iconTintDark = Color(0xFFF5B073)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAmountY = 0f },
                    onDragEnd = {
                        if (dragAmountY > 35f) { // Swipe down to close threshold
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
            .padding(horizontal = 14.dp, vertical = 4.dp)
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

        // Header Row: Title, Subtitle, Count, and Lime Circular Close/Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "Projects",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = clientName,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ONGOING PROJECTS · ${sampleProjects.size}",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = colors.textSecondary.copy(alpha = 0.8f)
                )
            }

            // Circular Lime Plus Button to switch to Create Project form
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(WiseLime)
                    .clickable(onClick = onAddProject)
                    .testTag("projects_sheet_add_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Project",
                    tint = DarkGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Project Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleProjects.forEach { proj ->
                ProjectCardItem(
                    proj = proj,
                    onClick = { onProjectSelect(proj.title) }
                )
            }
        }
    }
}

@Composable
private fun ProjectCardItem(
    proj: ProjectItemData,
    onClick: () -> Unit
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "proj_card_scale"
    )

    val cardBg = if (colors.isDark) Color(0xFF1C1F1A) else Color(0xFFF4F6F1)
    val iconBg = if (colors.isDark) proj.iconBgColorDark else proj.iconBgColorLight
    val iconTint = if (colors.isDark) proj.iconTintDark else proj.iconTintLight

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (colors.isDark) Color(0xFF2D3228) else Color(0xFFE2E6DD)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Square
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = proj.icon,
                    contentDescription = proj.title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = proj.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = proj.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Chevron Right Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "View Project",
                tint = colors.textSecondary.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

data class ProjectColorOption(
    val id: String,
    val name: String,
    val hexLight: Long,
    val hexDark: Long,
    val swatchColor: Color
)

val sampleProjectColorOptions = listOf(
    ProjectColorOption("lime", "Wise Lime", 0xFFE2F6D5L, 0xFF243A1BL, Color(0xFF9FE870)),
    ProjectColorOption("blue", "Slate Blue", 0xFFDCEEEFL, 0xFF1B2D3CL, Color(0xFF64B5F6)),
    ProjectColorOption("amber", "Warm Amber", 0xFFFDE8D3L, 0xFF382618L, Color(0xFFFFB74D)),
    ProjectColorOption("lavender", "Soft Iris", 0xFFEFE5FDL, 0xFF2A1D38L, Color(0xFFB39DDB)),
    ProjectColorOption("sage", "Eucalyptus", 0xFFE0F0E3L, 0xFF1E3123L, Color(0xFF81C784)),
    ProjectColorOption("graphite", "Onyx Tint", 0xFFE4E8ECL, 0xFF21272CL, Color(0xFF90A4AE))
)

@Composable
fun CreateProjectMorphSheet(
    clientName: String,
    onCancel: () -> Unit,
    onCreateProject: (String, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    var projectName by remember { mutableStateOf("Brand Film Edit") }
    var description by remember { mutableStateOf("What are you working on?") }
    var deadline by remember { mutableStateOf("Choose date") }
    var budget by remember { mutableStateOf("e.g. ₹25,000") }
    var selectedStatus by remember { mutableStateOf("In progress") }
    var selectedColorOption by remember { mutableStateOf(sampleProjectColorOptions.first()) }
    var initialTaskText by remember { mutableStateOf("Review raw footage") }
    var tasksList by remember { mutableStateOf(listOf("Review raw footage")) }

    val inputBg = if (colors.isDark) Color(0xFF1C1F1A) else Color(0xFFF4F6F1)
    val inputBorder = if (colors.isDark) Color(0xFF2D3228) else Color(0xFFE2E6DD)

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAmountY = 0f },
                    onDragEnd = {
                        if (dragAmountY > 35f) { // Swipe down to cancel
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
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Drag handle bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
        )

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "Create project",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Create a project for $clientName.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }

            // Top-right "New project" badge
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (colors.isDark) WiseLime.copy(alpha = 0.35f) else Color(0xFFC7ECB0)
                ),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = "New project",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (colors.isDark) WiseLime else DarkGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable Form Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. PROJECT NAME *
            Column {
                Text(
                    text = "PROJECT NAME *",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = projectName,
                            onValueChange = { projectName = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                            modifier = Modifier.fillMaxWidth().testTag("create_project_name_input")
                        )
                    }
                }
            }

            // 2. DESCRIPTION
            Column {
                Text(
                    text = "DESCRIPTION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        BasicTextField(
                            value = description,
                            onValueChange = { description = it },
                            textStyle = TextStyle(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                            modifier = Modifier.fillMaxWidth().testTag("create_project_description_input")
                        )
                    }
                }
            }

            // 3. CLIENT
            Column {
                Text(
                    text = "CLIENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (colors.isDark) Color(0xFF2B3A23) else Color(0xFFE2F6D5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SJ",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (colors.isDark) WiseLime else DarkGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = clientName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Select Client",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 4. DEADLINE & BUDGET
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deadline
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DEADLINE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.textSecondary.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = inputBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Deadline",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = deadline,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Budget
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BUDGET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.textSecondary.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = inputBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = budget,
                                onValueChange = { budget = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                ),
                                cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 5. INITIAL STATUS
            Column {
                Text(
                    text = "INITIAL STATUS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Not started", "In progress", "On hold").forEach { statusOption ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedStatus = statusOption }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedStatus == statusOption,
                                onClick = { selectedStatus = statusOption },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (colors.isDark) WiseLime else DarkGreen,
                                    unselectedColor = colors.textSecondary.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = statusOption,
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedStatus == statusOption) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedStatus == statusOption) colors.textPrimary else colors.textSecondary
                            )
                        }
                    }
                }
            }

            // 6. INITIAL TASKS
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INITIAL TASKS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.textSecondary.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "${tasksList.size} task${if (tasksList.size == 1) "" else "s"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = initialTaskText,
                            onValueChange = { initialTaskText = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                            modifier = Modifier.weight(1f)
                        )

                        if (initialTaskText.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Task",
                                tint = colors.textSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { initialTaskText = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // + Add another task
                Text(
                    text = "+ Add another task",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (colors.isDark) WiseLime else DarkGreen,
                    modifier = Modifier
                        .clickable {
                            if (initialTaskText.isNotBlank()) {
                                tasksList = tasksList + initialTaskText
                                initialTaskText = ""
                            }
                        }
                        .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
                )
            }

            // 7. CARD COLOR
            Column {
                Text(
                    text = "CARD COLOR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.textSecondary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sampleProjectColorOptions.forEach { option ->
                        val isSelected = selectedColorOption.id == option.id
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.12f else 1.0f,
                            animationSpec = spring(stiffness = 500f),
                            label = "color_swatch_scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(option.swatchColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) (if (colors.isDark) Color.White else NearBlack) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorOption = option },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected Color",
                                    tint = NearBlack,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 8. BOTTOM ACTION BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cancel Button
            Surface(
                onClick = onCancel,
                shape = RoundedCornerShape(9999.dp),
                color = if (colors.isDark) Color(0xFF222620) else Color(0xFFE8EBE3),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (colors.isDark) Color(0xFF333830) else Color(0xFFDFE3DA)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("create_project_cancel_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Cancel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            // Create Project Button
            Surface(
                onClick = {
                    onCreateProject(
                        projectName.ifBlank { "New Project" },
                        selectedColorOption.hexLight,
                        selectedColorOption.hexDark
                    )
                },
                shape = RoundedCornerShape(9999.dp),
                color = WiseLime,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (colors.isDark) Color(0xFF8CD860) else Color(0xFF88D45A)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("create_project_submit_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Create",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGreen
                    )
                }
            }
        }
    }
}

data class CreateOptionData(
    val id: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBgLight: Color,
    val iconBgDark: Color,
    val iconTintLight: Color,
    val iconTintDark: Color
)

@Composable
fun CreateMorphSheet(
    onClose: () -> Unit,
    onOptionSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    val options = remember {
        listOf(
            CreateOptionData(
                id = "new_chat",
                title = "New chat",
                icon = Icons.Outlined.ChatBubbleOutline,
                iconBgLight = Color(0xFFE2F6D5),
                iconBgDark = Color(0xFF2B3A23),
                iconTintLight = DarkGreen,
                iconTintDark = WiseLime
            ),
            CreateOptionData(
                id = "add_client",
                title = "Add client",
                icon = Icons.Outlined.PersonAdd,
                iconBgLight = Color(0xFFE1F0FA),
                iconBgDark = Color(0xFF1E2F3C),
                iconTintLight = Color(0xFF0F5A8A),
                iconTintDark = Color(0xFF73C2F5)
            ),
            CreateOptionData(
                id = "new_project",
                title = "New project",
                icon = Icons.Outlined.Folder,
                iconBgLight = Color(0xFFFCEBD9),
                iconBgDark = Color(0xFF3B2B1E),
                iconTintLight = Color(0xFF8C4A0F),
                iconTintDark = Color(0xFFF5B073)
            ),
            CreateOptionData(
                id = "schedule_message",
                title = "Schedule",
                icon = Icons.Outlined.Schedule,
                iconBgLight = Color(0xFFF4EBFB),
                iconBgDark = Color(0xFF2E2238),
                iconTintLight = Color(0xFF5B2C6F),
                iconTintDark = Color(0xFFD7A1F9)
            )
        )
    }

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
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 18.dp)
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

        // Header Row: Title and Subtitle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "Create",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = "Select an action to start",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Options Row with 4 bouncy pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1.0f,
                    animationSpec = spring(stiffness = 500f),
                    label = "create_option_pill_scale"
                )

                Surface(
                    onClick = { onOptionSelect(option.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (colors.isDark) option.iconBgDark else option.iconBgLight,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (colors.isDark) option.iconTintDark.copy(alpha = 0.3f) else option.iconBgLight
                    ),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .height(68.dp)
                        .testTag("create_morph_option_${option.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.title,
                            tint = if (colors.isDark) option.iconTintDark else option.iconTintLight,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = option.title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (colors.isDark) option.iconTintDark else option.iconTintLight,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun AddClientMorphSheet(
    onCancel: () -> Unit,
    onConfirmAdd: (username: String, onError: (String) -> Unit, onSuccess: (clientUid: String) -> Unit) -> Unit,
    onClientAdded: (clientUid: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    var usernameInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isRequestSent by remember { mutableStateOf(false) }
    var addedClientUid by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto-close navigation bar after 5 seconds when request is sent
    LaunchedEffect(isRequestSent) {
        if (isRequestSent) {
            delay(5000)
            onClientAdded(addedClientUid)
        }
    }

    // Auto-reset error message after 3 seconds with a smooth roller animation back to subtitle
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000)
            errorMessage = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAmountY = 0f },
                    onDragEnd = {
                        if (dragAmountY > 30f) {
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
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 18.dp)
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

        // Header Row: Title and Subtitle with roller error animation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAdd,
                        contentDescription = null,
                        tint = if (colors.isDark) WiseLime else NearBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Client",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Roller animation for error messages replacing subtitle
                AnimatedContent(
                    targetState = errorMessage,
                    transitionSpec = {
                        if (targetState != null) {
                            (slideInVertically(animationSpec = tween(320, easing = FastOutSlowInEasing)) { height -> height } +
                                fadeIn(animationSpec = tween(220)))
                                .togetherWith(
                                    slideOutVertically(animationSpec = tween(320, easing = FastOutSlowInEasing)) { height -> -height } +
                                        fadeOut(animationSpec = tween(180))
                                )
                        } else {
                            (slideInVertically(animationSpec = tween(320, easing = FastOutSlowInEasing)) { height -> -height } +
                                fadeIn(animationSpec = tween(220)))
                                .togetherWith(
                                    slideOutVertically(animationSpec = tween(320, easing = FastOutSlowInEasing)) { height -> height } +
                                        fadeOut(animationSpec = tween(180))
                                )
                        }
                    },
                    label = "add_client_subtitle_roller"
                ) { errText ->
                    if (errText != null) {
                        Text(
                            text = errText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD03238),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Enter username to add to SyncLine",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Username input field
        OutlinedTextField(
            value = usernameInput,
            onValueChange = {
                usernameInput = it
                if (errorMessage != null) errorMessage = null
            },
            placeholder = { Text("e.g. sarah_j", color = colors.textSecondary, fontSize = 13.5.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.PersonPin,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            enabled = !isLoading && !isRequestSent,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WiseLime,
                unfocusedBorderColor = colors.cardBorder,
                focusedContainerColor = if (colors.isDark) Color(0xFF1E221C) else Color(0xFFF6F8F3),
                unfocusedContainerColor = if (colors.isDark) Color(0xFF1E221C) else Color(0xFFF6F8F3),
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_client_morph_username_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Morphed Action Buttons: standard 2 buttons morph into 1 single success pill
        AnimatedContent(
            targetState = isRequestSent,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = 400f)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.95f, animationSpec = spring(stiffness = 400f))
                    )
            },
            label = "add_client_button_morph"
        ) { requestSent ->
            if (requestSent) {
                Surface(
                    shape = CircleShape,
                    color = WiseLime,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("add_client_request_sent_pill")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Request sent · Check Activity tab",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WisePillButton(
                        text = "Back",
                        onClick = { if (!isLoading) onCancel() },
                        isPrimary = false,
                        modifier = Modifier.weight(1f),
                        testTag = "add_client_morph_back_btn"
                    )

                    WisePillButton(
                        text = if (isLoading) "Looking up..." else "Add Client",
                        onClick = {
                            if (!isLoading) {
                                val cleanUsername = usernameInput.trim().lowercase()
                                if (cleanUsername.isBlank()) {
                                    errorMessage = "Please enter a username."
                                } else if (cleanUsername.length < 3) {
                                    errorMessage = "Username must be at least 3 characters."
                                } else if (!cleanUsername.matches(Regex("^[a-zA-Z0-9_.]+$"))) {
                                    errorMessage = "Only letters, numbers, underscores, dots allowed."
                                } else {
                                    isLoading = true
                                    errorMessage = null
                                    onConfirmAdd(
                                        cleanUsername,
                                        { err ->
                                            isLoading = false
                                            errorMessage = err
                                        },
                                        { clientUid ->
                                            isLoading = false
                                            addedClientUid = clientUid
                                            isRequestSent = true
                                        }
                                    )
                                }
                            }
                        },
                        isPrimary = true,
                        modifier = Modifier.weight(1.2f),
                        testTag = "add_client_morph_submit_btn"
                    )
                }
            }
        }
    }
}

data class AttachFileOptionData(
    val id: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBgLight: Color,
    val iconBgDark: Color,
    val iconTintLight: Color,
    val iconTintDark: Color
)

@Composable
fun AttachFileMorphSheet(
    clientName: String = "",
    onClose: () -> Unit,
    onOptionSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    val options = remember {
        listOf(
            AttachFileOptionData(
                id = "choose_file",
                title = "Document",
                icon = Icons.Outlined.Description,
                iconBgLight = Color(0xFFEAF4E8),
                iconBgDark = Color(0xFF223522),
                iconTintLight = Color(0xFF335832),
                iconTintDark = WiseLime
            ),
            AttachFileOptionData(
                id = "photo_video",
                title = "Photo",
                icon = Icons.Outlined.Image,
                iconBgLight = Color(0xFFFAF0E6),
                iconBgDark = Color(0xFF332A1F),
                iconTintLight = Color(0xFF7A4E1B),
                iconTintDark = Color(0xFFF5C58A)
            ),
            AttachFileOptionData(
                id = "share_link",
                title = "Share link",
                icon = Icons.Outlined.Link,
                iconBgLight = Color(0xFFEBF5FB),
                iconBgDark = Color(0xFF1E2E3C),
                iconTintLight = Color(0xFF1D5276),
                iconTintDark = Color(0xFF82C8F7)
            ),
            AttachFileOptionData(
                id = "recent_files",
                title = "Recents",
                icon = Icons.Outlined.History,
                iconBgLight = Color(0xFFF4EBFB),
                iconBgDark = Color(0xFF2E2238),
                iconTintLight = Color(0xFF5B2C6F),
                iconTintDark = Color(0xFFD7A1F9)
            )
        )
    }

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
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 10.dp)
    ) {
        // Drag handle indicator bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
        )

        // Header Row: "Attach file" Title + Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attach file",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
                    .testTag("attach_file_close_btn")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Row of 4 Icon Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            options.forEach { option ->
                AttachFileIconButton(
                    option = option,
                    onClick = { onOptionSelect(option.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AttachFileIconButton(
    option: AttachFileOptionData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "attach_icon_scale"
    )

    val iconBg = if (colors.isDark) option.iconBgDark else option.iconBgLight
    val iconTint = if (colors.isDark) option.iconTintDark else option.iconTintLight

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("attach_file_card_${option.id}")
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title.replace("\n", " "),
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = option.title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            maxLines = 2
        )
    }
}

private fun extractFileInfo(
    context: Context,
    uri: Uri,
    defaultName: String = "Document.pdf",
    defaultExt: String = "PDF"
): Triple<String, String, String> {
    var name = defaultName
    var sizeStr = "1.2 MB"
    var ext = defaultExt

    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    val queriedName = cursor.getString(nameIndex)
                    if (!queriedName.isNullOrBlank()) {
                        name = queriedName
                    }
                }
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    val bytes = cursor.getLong(sizeIndex)
                    if (bytes > 0) {
                        sizeStr = formatBytes(bytes)
                    }
                }
            }
        }
    } catch (_: Exception) {}

    val dotIndex = name.lastIndexOf('.')
    if (dotIndex != -1 && dotIndex < name.length - 1) {
        ext = name.substring(dotIndex + 1).uppercase()
    }

    return Triple(name, sizeStr, ext)
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

@Composable
fun ShareLinkMorphSheet(
    clientName: String = "",
    onCancel: () -> Unit,
    onSendLink: (title: String, url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    var linkTitle by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }

    val inputBg = if (colors.isDark) Color(0xFF1E211C) else Color(0xFFF4F6F0)
    val inputBorder = if (colors.isDark) Color(0xFF2E322A) else Color(0xFFDFE3DA)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAmountY = 0f },
                    onDragEnd = {
                        if (dragAmountY > 30f) {
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
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Drag handle indicator
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
        )

        // Header Row: Title & Subtitle + Top right link badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(start = 2.dp)) {
                Text(
                    text = "Share link",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (clientName.isNotBlank()) "Share a link with $clientName" else "Share a link in chat",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (colors.isDark) WiseLime.copy(alpha = 0.35f) else Color(0xFFC7ECB0)
                ),
                modifier = Modifier.padding(end = 2.dp)
            ) {
                Text(
                    text = "Link",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (colors.isDark) WiseLime else DarkGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title Field
        Column {
            Text(
                text = "TITLE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colors.textSecondary.copy(alpha = 0.85f),
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = inputBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (linkTitle.isEmpty()) {
                        Text(
                            text = "e.g. Design Prototype or Brief",
                            fontSize = 13.sp,
                            color = colors.textSecondary.copy(alpha = 0.6f)
                        )
                    }
                    BasicTextField(
                        value = linkTitle,
                        onValueChange = { linkTitle = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                        modifier = Modifier.fillMaxWidth().testTag("link_title_input")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // URL Field
        Column {
            Text(
                text = "LINK URL *",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colors.textSecondary.copy(alpha = 0.85f),
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = inputBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (linkUrl.isEmpty()) {
                        Text(
                            text = "https://...",
                            fontSize = 13.sp,
                            color = colors.textSecondary.copy(alpha = 0.6f)
                        )
                    }
                    BasicTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (linkUrl.isNotBlank()) {
                                    onSendLink(linkTitle, linkUrl)
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                        modifier = Modifier.fillMaxWidth().testTag("link_url_input")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cancel button
            Surface(
                onClick = onCancel,
                shape = RoundedCornerShape(9999.dp),
                color = if (colors.isDark) Color(0xFF222620) else Color(0xFFE8EBE3),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (colors.isDark) Color(0xFF333830) else Color(0xFFDFE3DA)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("share_link_cancel_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Cancel",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            // Send Link button
            val isEnabled = linkUrl.isNotBlank()
            Surface(
                onClick = {
                    if (isEnabled) {
                        onSendLink(linkTitle, linkUrl)
                    }
                },
                shape = RoundedCornerShape(9999.dp),
                color = if (isEnabled) WiseLime else if (colors.isDark) Color(0xFF2C3227) else Color(0xFFDFE3D8),
                border = if (isEnabled) {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (colors.isDark) Color(0xFF8CD860) else Color(0xFF88D45A)
                    )
                } else null,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("share_link_send_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Send",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isEnabled) DarkGreen else colors.textSecondary.copy(alpha = 0.5f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = if (isEnabled) DarkGreen else colors.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

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

@Composable
fun NewMeetingMorphSheet(
    clientName: String = "Sarah Jenkins",
    onCancel: () -> Unit,
    onSchedule: (title: String, date: String, time: String, duration: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    var meetingTitle by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Select date") }
    var startTime by remember { mutableStateOf("10:00 AM") }
    var selectedDuration by remember { mutableStateOf("30 min") }
    var notes by remember { mutableStateOf("") }

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

        // Header: "New meeting", "With Sarah Jenkins", Mint squircle badge
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
                        onValueChange = { meetingTitle = it },
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
                onClick = {
                    selectedDate = if (selectedDate == "Select date") "Tomorrow, Oct 27" else if (selectedDate == "Tomorrow, Oct 27") "Thursday, Oct 29" else "Select date"
                },
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
                        text = selectedDate,
                        fontSize = 13.sp,
                        fontWeight = if (selectedDate == "Select date") FontWeight.Normal else FontWeight.Medium,
                        color = if (selectedDate == "Select date") colors.textPrimary else (if (colors.isDark) WiseLime else DarkGreen),
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
                    onClick = {
                        startTime = when (startTime) {
                            "10:00 AM" -> "11:30 AM"
                            "11:30 AM" -> "2:00 PM"
                            "2:00 PM" -> "3:30 PM"
                            else -> "10:00 AM"
                        }
                    },
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
                            text = startTime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Duration",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Surface(
                    onClick = {
                        selectedDuration = when (selectedDuration) {
                            "30 min" -> "45 min"
                            "45 min" -> "60 min"
                            "60 min" -> "15 min"
                            else -> "30 min"
                        }
                    },
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
                            text = selectedDuration,
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
                            text = "Anything Sarah should know?",
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

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Schedule meeting button (Dark/Black Pill)
        val submitInteractionSource = remember { MutableInteractionSource() }
        val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
        val submitScale by animateFloatAsState(
            targetValue = if (isSubmitPressed) 0.97f else 1.0f,
            animationSpec = spring(stiffness = 500f),
            label = "submit_btn_scale"
        )

        Surface(
            onClick = {
                onSchedule(
                    meetingTitle.ifBlank { "Project review" },
                    if (selectedDate == "Select date") "Tomorrow, 10:00 AM" else selectedDate,
                    startTime,
                    selectedDuration,
                    notes
                )
            },
            shape = RoundedCornerShape(9999.dp),
            color = if (colors.isDark) Color(0xFF262923) else Color(0xFF1B1E19),
            modifier = Modifier
                .fillMaxWidth()
                .scale(submitScale)
                .height(46.dp)
                .testTag("submit_schedule_meeting_btn")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Schedule meeting",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
    onClose: () -> Unit,
    onMeetingSelect: (title: String, date: String, time: String, duration: String, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }
    var selectedTab by remember { mutableStateOf("Upcoming") }

    val iconBg = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEAF4E8)
    val iconTint = if (colors.isDark) WiseLime else Color(0xFF335832)

    val upcomingMeetings = remember {
        listOf(
            ScheduledMeetingItemData(
                id = "sm1",
                month = "OCT",
                day = "28",
                title = "Project review",
                time = "10:00 AM",
                duration = "30 min",
                status = "Confirmed",
                isConfirmed = true
            ),
            ScheduledMeetingItemData(
                id = "sm2",
                month = "OCT",
                day = "30",
                title = "Feedback call",
                time = "2:30 PM",
                duration = "30 min",
                status = "Awaiting response",
                isConfirmed = false
            )
        )
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

        // Header: "Scheduled meetings", "With Sarah Jenkins", Mint badge
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

        // Meeting Cards List
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            upcomingMeetings.forEach { meeting ->
                ScheduledMeetingCard(
                    meeting = meeting,
                    onClick = {
                        onMeetingSelect(
                            meeting.title,
                            "${meeting.month} ${meeting.day}",
                            meeting.time,
                            meeting.duration,
                            meeting.status
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Footer Caption
        Text(
            text = "Example meetings for the prototype — not actual bookings.",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textSecondary.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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

@Composable
fun CreateInvoiceMorphSheet(
    clientName: String = "Sarah Jenkins",
    onClose: () -> Unit,
    onViewPastInvoices: () -> Unit,
    onCreateInvoice: (title: String, project: String, amount: String, dueDate: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    var invoiceTitle by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf("Select project (optional)") }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Choose due date") }
    var notes by remember { mutableStateOf("") }

    val iconBg = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEAF4E8)
    val iconTint = if (colors.isDark) WiseLime else Color(0xFF335832)

    val inputBg = if (colors.isDark) Color(0xFF1E211C) else Color(0xFFFFFFFF)
    val inputBorder = if (colors.isDark) Color(0xFF2E322A) else Color(0xFFE2E6DD)

    val sampleProjectsList = listOf("Select project (optional)", "Mobile Redesign", "Website Redesign", "Brand Film Edit")
    val sampleDueDates = listOf("Choose due date", "Oct 30, 2026", "Nov 15, 2026", "Dec 1, 2026")

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

        // Header Row: "Create invoice", "Invoice for Sarah Jenkins", Mint squircle badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Create invoice",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Invoice for $clientName",
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
                    .testTag("invoice_header_badge"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = "Receipt",
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrollable Form Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Invoice title
            Column {
                Text(
                    text = "Invoice title",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (invoiceTitle.isEmpty()) {
                            Text(
                                text = "e.g. Video editing — October",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textSecondary.copy(alpha = 0.65f)
                            )
                        }
                        BasicTextField(
                            value = invoiceTitle,
                            onValueChange = { invoiceTitle = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invoice_title_input")
                        )
                    }
                }
            }

            // 2. Project
            Column {
                Text(
                    text = "Project",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Surface(
                    onClick = {
                        val currIdx = sampleProjectsList.indexOf(selectedProject)
                        val nextIdx = if (currIdx == -1 || currIdx == sampleProjectsList.lastIndex) 1 else currIdx + 1
                        selectedProject = sampleProjectsList[nextIdx]
                    },
                    shape = RoundedCornerShape(9999.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("invoice_project_picker")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedProject,
                            fontSize = 13.5.sp,
                            fontWeight = if (selectedProject == "Select project (optional)") FontWeight.Normal else FontWeight.Medium,
                            color = if (selectedProject == "Select project (optional)") colors.textSecondary.copy(alpha = 0.65f) else colors.textPrimary
                        )
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Select project",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3. Amount
            Column {
                Text(
                    text = "Amount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (amount.isEmpty()) {
                            Text(
                                text = "0.00",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textSecondary.copy(alpha = 0.65f)
                            )
                        }
                        BasicTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invoice_amount_input")
                        )
                    }
                }
            }

            // 4. Due date
            Column {
                Text(
                    text = "Due date",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Surface(
                    onClick = {
                        val currIdx = sampleDueDates.indexOf(dueDate)
                        val nextIdx = if (currIdx == -1 || currIdx == sampleDueDates.lastIndex) 1 else currIdx + 1
                        dueDate = sampleDueDates[nextIdx]
                    },
                    shape = RoundedCornerShape(9999.dp),
                    color = inputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("invoice_due_date_picker")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Due date",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = dueDate,
                            fontSize = 13.5.sp,
                            fontWeight = if (dueDate == "Choose due date") FontWeight.Normal else FontWeight.Medium,
                            color = if (dueDate == "Choose due date") colors.textSecondary.copy(alpha = 0.65f) else colors.textPrimary,
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

            // 5. Notes (optional)
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
                        .height(72.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (notes.isEmpty()) {
                            Text(
                                text = "Add payment details or a short note",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textSecondary.copy(alpha = 0.65f)
                            )
                        }
                        BasicTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invoice_notes_input")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Create invoice button (Solid Dark Pill)
            val submitInteractionSource = remember { MutableInteractionSource() }
            val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
            val submitScale by animateFloatAsState(
                targetValue = if (isSubmitPressed) 0.97f else 1.0f,
                animationSpec = spring(stiffness = 500f),
                label = "submit_invoice_scale"
            )

            Surface(
                onClick = {
                    onCreateInvoice(
                        invoiceTitle.ifBlank { "Video editing — October" },
                        if (selectedProject == "Select project (optional)") "General Services" else selectedProject,
                        amount.ifBlank { "450.00" },
                        if (dueDate == "Choose due date") "Oct 30, 2026" else dueDate,
                        notes
                    )
                },
                shape = RoundedCornerShape(9999.dp),
                color = if (colors.isDark) Color(0xFF262923) else Color(0xFF1B1E19),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(submitScale)
                    .height(46.dp)
                    .testTag("submit_create_invoice_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Create invoice",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Past invoices button (Outlined Pill)
            val pastInteractionSource = remember { MutableInteractionSource() }
            val isPastPressed by pastInteractionSource.collectIsPressedAsState()
            val pastScale by animateFloatAsState(
                targetValue = if (isPastPressed) 0.97f else 1.0f,
                animationSpec = spring(stiffness = 500f),
                label = "past_invoices_scale"
            )

            Surface(
                onClick = onViewPastInvoices,
                shape = RoundedCornerShape(9999.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (colors.isDark) Color(0xFF2E322A) else Color(0xFFDFE3DA)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pastScale)
                    .height(46.dp)
                    .testTag("view_past_invoices_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Past invoices",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Past invoices",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

data class SamplePastInvoiceItem(
    val id: String,
    val title: String,
    val project: String,
    val amount: String,
    val dueDate: String,
    val notes: String,
    val isPaid: Boolean
)

@Composable
fun PastInvoicesMorphSheet(
    clientName: String = "Sarah Jenkins",
    onClose: () -> Unit,
    onInvoiceSelect: (SamplePastInvoiceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    val iconBg = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEAF4E8)
    val iconTint = if (colors.isDark) WiseLime else Color(0xFF335832)

    val samplePastInvoices = remember {
        listOf(
            SamplePastInvoiceItem(
                id = "inv_p1",
                title = "Video editing — September",
                project = "Brand Film Edit",
                amount = "450.00",
                dueDate = "Oct 15, 2026",
                notes = "Net 15 payment terms",
                isPaid = true
            ),
            SamplePastInvoiceItem(
                id = "inv_p2",
                title = "Mobile App Design Deposit",
                project = "Mobile Redesign",
                amount = "1,200.00",
                dueDate = "Nov 01, 2026",
                notes = "50% upfront project milestone",
                isPaid = false
            ),
            SamplePastInvoiceItem(
                id = "inv_p3",
                title = "Brand Identity Assets",
                project = "Brand Identity",
                amount = "800.00",
                dueDate = "Nov 10, 2026",
                notes = "Logo suite and typography kit",
                isPaid = false
            )
        )
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

        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Past invoices",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Invoices for $clientName",
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
                    .testTag("past_invoices_badge"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = "Past Invoices",
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Invoices list
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            samplePastInvoices.forEach { inv ->
                Surface(
                    onClick = { onInvoiceSelect(inv) },
                    shape = RoundedCornerShape(18.dp),
                    color = colors.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (colors.isDark) Color(0xFF2E322A) else Color(0xFFDFE3DA)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .testTag("past_invoice_item_${inv.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = inv.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$${inv.amount} · Due ${inv.dueDate}",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = if (inv.isPaid) {
                                if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)
                            } else {
                                if (colors.isDark) Color(0xFF2E2D23) else Color(0xFFFAF6E6)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (inv.isPaid) {
                                    if (colors.isDark) WiseLime.copy(alpha = 0.35f) else Color(0xFFC7ECB0)
                                } else {
                                    if (colors.isDark) Color(0xFF4A4428) else Color(0xFFEAE2C6)
                                }
                            )
                        ) {
                            Text(
                                text = if (inv.isPaid) "PAID" else "UNPAID",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (inv.isPaid) {
                                    if (colors.isDark) WiseLime else DarkGreen
                                } else {
                                    if (colors.isDark) Color(0xFF163300) else Color(0xFF8A6200)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "PEOPLE", "ACTION", "PROJECT", "FILE", "INVOICE"
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val payload: String
)

@Composable
fun SearchMorphSheet(
    onClose: () -> Unit,
    onResultSelect: (String) -> Unit,
    onCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    val allSearchItems = remember {
        listOf(
            // People
            SearchResultItem("s_sarah", "Sarah Jenkins", "Lead Product Designer · Acme Co. (London)", "PEOPLE", Icons.Default.Person, "open_chat:c1"),
            SearchResultItem("s_kenji", "Kenji Sato", "Senior Mobile Engineer · Tokyo", "PEOPLE", Icons.Default.Person, "open_chat:c2"),
            SearchResultItem("s_elena", "Elena Rostova", "VP of Product · Berlin Design Studio", "PEOPLE", Icons.Default.Person, "open_chat:c3"),
            SearchResultItem("s_david", "David Miller", "Design Partner · New York", "PEOPLE", Icons.Default.Person, "open_chat:c4"),

            // Actions
            SearchResultItem("s_act_inv", "Create Invoice", "Generate & send invoice with payment options", "ACTION", Icons.Outlined.ReceiptLong, "action:invoice"),
            SearchResultItem("s_act_sch", "Schedule Meeting", "Plan call with date, time, and timezone", "ACTION", Icons.Outlined.Schedule, "action:schedule"),
            SearchResultItem("s_act_proj", "Create Project", "New project workspace with tasks & budget", "ACTION", Icons.Outlined.WorkOutline, "action:create_project"),
            SearchResultItem("s_act_link", "Share Link", "Send prototype, Figma, or web URL in chat", "ACTION", Icons.Outlined.Link, "action:share_link"),
            SearchResultItem("s_act_att", "Attach Document", "Upload PDF, DOCX, ZIP, or photos from device", "ACTION", Icons.Outlined.AttachFile, "action:attach_file"),

            // Projects
            SearchResultItem("s_p1", "Mobile Redesign", "Acme Co. · Due May 30", "PROJECT", Icons.Outlined.WorkOutline, "project:p1"),
            SearchResultItem("s_p2", "Website Redesign", "Acme Co. · 65% complete", "PROJECT", Icons.Outlined.WorkOutline, "project:p2"),
            SearchResultItem("s_p3", "Brand Strategy", "Nova Studio · Planning", "PROJECT", Icons.Outlined.WorkOutline, "project:p3"),

            // Files
            SearchResultItem("s_f1", "Design_Final_v2.pdf", "PDF · 2.4 MB", "FILE", Icons.Outlined.Description, "file:fl1"),
            SearchResultItem("s_f2", "Project_Brief.docx", "DOCX · 1.2 MB", "FILE", Icons.Outlined.Description, "file:fl2"),
            SearchResultItem("s_f3", "Logo_Assets.zip", "ZIP · 8.7 MB", "FILE", Icons.Outlined.Description, "file:fl3"),

            // Invoices
            SearchResultItem("s_i1", "Milestone 3 — Wireframes & UI Kit", "Invoice INV-2024-003 · $1,850.00", "INVOICE", Icons.Outlined.ReceiptLong, "invoice:inv_mock_sarah"),
            SearchResultItem("s_i2", "Brand Identity & Guidelines", "Invoice INV-2024-001 · $2,400.00", "INVOICE", Icons.Outlined.ReceiptLong, "invoice:inv_past_1")
        )
    }

    val categoriesList = listOf("All", "People", "Actions", "Projects", "Files", "Invoices")

    val filteredResults = remember(searchQuery, selectedCategory) {
        allSearchItems.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "People" -> item.category == "PEOPLE"
                "Actions" -> item.category == "ACTION"
                "Projects" -> item.category == "PROJECT"
                "Files" -> item.category == "FILE"
                "Invoices" -> item.category == "INVOICE"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesQuery
        }.take(5)
    }

    LaunchedEffect(filteredResults.size) {
        onCountChanged(filteredResults.size)
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
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 10.dp)
    ) {
        // Drag handle bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
        )

        // Search Input Bar inside the Morphing Card
        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = if (colors.isDark) Color(0xFF222620) else Color(0xFFF1F3EE),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (colors.isDark) Color(0xFF333830) else Color(0xFFE2E6DD)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("morph_search_input_container")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = if (colors.isDark) WiseLime else NearBlack,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search people, actions, files...",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("morph_search_text_input")
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { searchQuery = "" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0xFF2C3227) else Color(0xFFE2E6DC))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categoriesList.forEach { cat ->
                val isSel = selectedCategory == cat
                Surface(
                    onClick = { selectedCategory = cat },
                    shape = RoundedCornerShape(9999.dp),
                    color = if (isSel) {
                        if (colors.isDark) WiseLime else DarkGreen
                    } else {
                        if (colors.isDark) Color(0xFF222620) else Color(0xFFEAF0E6)
                    },
                    modifier = Modifier.height(28.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) {
                                if (colors.isDark) DarkGreen else Color.White
                            } else colors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results List
        if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = colors.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No matching results found",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredResults.forEach { item ->
                    Surface(
                        onClick = { onResultSelect(item.payload) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (colors.isDark) Color(0xFF1E211C) else Color(0xFFF6F8F3),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (colors.isDark) Color(0xFF2D3228) else Color(0xFFE2E6DD)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("search_result_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (item.category) {
                                            "PEOPLE" -> if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)
                                            "ACTION" -> if (colors.isDark) Color(0xFF2A2D25) else Color(0xFFFAF0E6)
                                            "PROJECT" -> if (colors.isDark) Color(0xFF1E2F3C) else Color(0xFFE1F0FA)
                                            else -> if (colors.isDark) Color(0xFF252922) else Color(0xFFEFF3EA)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = when (item.category) {
                                        "PEOPLE" -> if (colors.isDark) WiseLime else DarkGreen
                                        "ACTION" -> if (colors.isDark) Color(0xFFF5C58A) else Color(0xFF7A4E1B)
                                        "PROJECT" -> if (colors.isDark) Color(0xFF73C2F5) else Color(0xFF0F5A8A)
                                        else -> colors.textPrimary
                                    },
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.subtitle,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = if (colors.isDark) Color(0xFF262A22) else Color(0xFFEAEDE6)
                            ) {
                                Text(
                                    text = item.category,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}










