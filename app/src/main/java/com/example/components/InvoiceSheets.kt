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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceMorphSheet(
    clientName: String = "Sarah Jenkins",
    clientUid: String = "",
    projectsList: List<ProjectItemData> = emptyList(),
    onClose: () -> Unit,
    onViewPastInvoices: () -> Unit,
    onCreateInvoiceDirect: (
        title: String,
        clientUid: String,
        clientName: String,
        projectId: String?,
        projectName: String?,
        amount: Double,
        currencyCode: String,
        dueDate: String,
        notes: String?,
        attemptId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    var invoiceTitle by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf("Select project (optional)") }
    var amount by remember { mutableStateOf("") }
    var currencyCode by remember { mutableStateOf("INR") }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var projectDropdownExpanded by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf("Choose due date") }
    var showDatePicker by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val attemptInvoiceId = remember { "inv_${java.util.UUID.randomUUID()}" }

    val iconBg = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEAF4E8)
    val iconTint = if (colors.isDark) WiseLime else Color(0xFF335832)

    val inputBg = if (colors.isDark) Color(0xFF1E211C) else Color(0xFFFFFFFF)
    val inputBorder = if (colors.isDark) Color(0xFF2E322A) else Color(0xFFE2E6DD)

    // Filter projects associated with the active client
    val availableProjects = remember(projectsList, clientUid) {
        projectsList.filter { it.clientUid.isBlank() || it.clientUid == clientUid }
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
                            dueDate = formatted
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
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEE2E2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFF991B1B),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

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
                            onValueChange = { if (it.length <= 80) invoiceTitle = it },
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

            // 2. Project Selector
            Column {
                Text(
                    text = "Project",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { projectDropdownExpanded = true },
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

                    // Custom Rounded Dropdown Surface
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = inputBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                        shadowElevation = 6.dp
                    ) {
                        DropdownMenu(
                            expanded = projectDropdownExpanded,
                            onDismissRequest = { projectDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .background(inputBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Select project (optional)", fontSize = 13.sp, color = colors.textSecondary) },
                                onClick = {
                                    selectedProject = "Select project (optional)"
                                    projectDropdownExpanded = false
                                }
                            )
                            if (availableProjects.isEmpty()) {
                                DropdownMenuItem(
                                    enabled = false,
                                    text = {
                                        Text(
                                            text = "No projects available",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = colors.textSecondary.copy(alpha = 0.5f)
                                        )
                                    },
                                    onClick = {}
                                )
                            } else {
                                availableProjects.forEach { proj ->
                                    val isSelected = (proj.title == selectedProject)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = proj.title,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) (if (colors.isDark) WiseLime else DarkGreen) else colors.textPrimary
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = if (colors.isDark) WiseLime else DarkGreen,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedProject = proj.title
                                            projectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Currency & Amount Row
            Column {
                Text(
                    text = "Amount & Currency",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Currency Dropdown Pill
                    Box {
                        Surface(
                            onClick = { currencyDropdownExpanded = true },
                            shape = RoundedCornerShape(9999.dp),
                            color = inputBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (currencyCode) {
                                        "INR" -> "₹ INR"
                                        "GBP" -> "£ GBP"
                                        else -> "$ USD"
                                    },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Select currency",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = inputBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                            shadowElevation = 6.dp
                        ) {
                            DropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false },
                                modifier = Modifier.background(inputBg)
                            ) {
                                listOf(
                                    "INR" to "₹ INR (Indian Rupee)",
                                    "USD" to "$ USD (US Dollar)",
                                    "GBP" to "£ GBP (British Pound)"
                                ).forEach { (code, label) ->
                                    val isSelected = (currencyCode == code)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = label,
                                                fontSize = 13.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected) (if (colors.isDark) WiseLime else DarkGreen) else colors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            currencyCode = code
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Numeric Amount Field
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = inputBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                        modifier = Modifier
                            .weight(1f)
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
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() || it == '.' }) {
                                        amount = input
                                    }
                                },
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
            }

            // 4. Due Date
            Column {
                Text(
                    text = "Due date",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Surface(
                    onClick = { showDatePicker = true },
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
                            onValueChange = { if (it.length <= 300) notes = it },
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
            val submitInteractionSource = remember { MutableInteractionSource() }
            val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
            val submitScale by animateFloatAsState(
                targetValue = if (isSubmitPressed) 0.97f else 1.0f,
                animationSpec = spring(stiffness = 500f),
                label = "submit_invoice_scale"
            )

            Surface(
                onClick = {
                    if (isSubmitting) return@Surface
                    val cleanTitle = invoiceTitle.trim()
                    if (cleanTitle.isBlank()) {
                        errorMessage = "Please enter an invoice title"
                        return@Surface
                    }
                    val parsedAmt = amount.toDoubleOrNull()
                    if (parsedAmt == null || parsedAmt <= 0) {
                        errorMessage = "Please enter a valid amount"
                        return@Surface
                    }
                    if (dueDate == "Choose due date" || dueDate.isBlank()) {
                        errorMessage = "Please select a due date"
                        return@Surface
                    }

                    errorMessage = null
                    isSubmitting = true

                    val selectedProjObj = availableProjects.find { it.title == selectedProject }
                    val projId = selectedProjObj?.id
                    val projName = selectedProjObj?.title

                    onCreateInvoiceDirect(
                        cleanTitle,
                        clientUid,
                        clientName,
                        projId,
                        projName,
                        parsedAmt,
                        currencyCode,
                        dueDate,
                        notes.trim(),
                        attemptInvoiceId,
                        {
                            isSubmitting = false
                        },
                        { err ->
                            isSubmitting = false
                            errorMessage = err
                        }
                    )
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(9999.dp),
                color = if (colors.isDark) Color(0xFF262923) else Color(0xFF1B1E19),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(submitScale)
                    .height(46.dp)
                    .testTag("submit_create_invoice_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Create invoice",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
                    if (colors.isDark) Color(0xFF32362D) else Color(0xFFD2D6CC)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pastScale)
                    .height(44.dp)
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Past invoices",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun PastInvoicesMorphSheet(
    clientName: String = "Sarah Jenkins",
    invoices: List<com.example.model.Invoice> = emptyList(),
    onClose: () -> Unit,
    onInvoiceSelect: (com.example.model.Invoice) -> Unit,
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

        if (invoices.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No invoices found",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Invoices created for $clientName will appear here",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                invoices.forEach { inv ->
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
                            .testTag("past_invoice_item_${inv.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                    text = "${inv.invoiceNumber} · Due ${inv.dueDate}",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = inv.formatAmount(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = if (inv.isPaid) {
                                        if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)
                                    } else {
                                        if (colors.isDark) Color(0xFF382914) else Color(0xFFFFF3D6)
                                    }
                                ) {
                                    Text(
                                        text = if (inv.isPaid) "PAID" else if (inv.status.equals("CANCELLED", ignoreCase = true)) "CANCELLED" else "UNPAID",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (inv.isPaid) (if (colors.isDark) WiseLime else DarkGreen) else Color(0xFFB45309),
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
}
