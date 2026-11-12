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

data class ProjectItemData(
    val id: String = "",
    val title: String,
    val subtitle: String,
    val clientUid: String = "",
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
    projects: List<ProjectItemData> = emptyList(),
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }

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

        // Header Row: Title, Subtitle, Count, and Lime Circular Plus Button
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
                    text = "ONGOING PROJECTS · ${projects.size}",
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

        if (projects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkOutline,
                        contentDescription = null,
                        tint = if (colors.isDark) WiseLime else DarkGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "No Projects Yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Create your first project to organize tasks and deliverables for $clientName.",
                    fontSize = 12.5.sp,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    onClick = onAddProject,
                    shape = RoundedCornerShape(9999.dp),
                    color = WiseLime,
                    modifier = Modifier.testTag("no_projects_create_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Create Project",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                    }
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                projects.forEach { proj ->
                    ProjectCardItem(
                        proj = proj,
                        onClick = { onProjectSelect(if (proj.id.isNotBlank()) proj.id else proj.title) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun ProjectCardItem(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectMorphSheet(
    clientName: String,
    onCancel: () -> Unit,
    onCreateProject: (String, String, Long, Long, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var dragAmountY by remember { mutableFloatStateOf(0f) }
    var isCreating by remember { mutableStateOf(false) }

    var projectName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("Choose date") }
    var budget by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("In progress") }
    var selectedColorOption by remember { mutableStateOf(sampleProjectColorOptions.first()) }
    var initialTaskText by remember { mutableStateOf("") }
    var tasksList by remember { mutableStateOf(emptyList<String>()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val inputBg = if (colors.isDark) Color(0xFF1C1F1A) else Color(0xFFF4F6F1)
    val inputBorder = if (colors.isDark) Color(0xFF2D3228) else Color(0xFFE2E6DD)

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
                            deadline = formatted
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
                        if (projectName.isEmpty()) {
                            Text(
                                text = "Project name (e.g. Website Redesign)",
                                fontSize = 13.sp,
                                color = colors.textSecondary.copy(alpha = 0.5f)
                            )
                        }
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
                        if (description.isEmpty()) {
                            Text(
                                text = "What are you working on?",
                                fontSize = 12.sp,
                                color = colors.textSecondary.copy(alpha = 0.5f)
                            )
                        }
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

            // 3. DEADLINE & BUDGET
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deadline (clickable to pick date)
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
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        color = inputBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder),
                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("create_project_deadline_btn")
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
                                tint = if (deadline == "Choose date") colors.textSecondary.copy(alpha = 0.6f) else (if (colors.isDark) WiseLime else DarkGreen),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = deadline,
                                fontSize = 12.sp,
                                fontWeight = if (deadline == "Choose date") FontWeight.Normal else FontWeight.Bold,
                                color = if (deadline == "Choose date") colors.textSecondary.copy(alpha = 0.6f) else colors.textPrimary
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
                            if (budget.isEmpty()) {
                                Text(
                                    text = "e.g. ₹25,000",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary.copy(alpha = 0.5f)
                                )
                            }
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

            // 4. INITIAL STATUS
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

            // 5. TASKS
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TASKS",
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
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (initialTaskText.isEmpty()) {
                                Text(
                                    text = "Add a task (e.g. Review raw footage)",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary.copy(alpha = 0.5f)
                                )
                            }
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
                                modifier = Modifier.fillMaxWidth().testTag("create_project_task_input")
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (initialTaskText.isNotBlank()) Color(0xFFA8F96B) else (if (colors.isDark) Color(0xFF2E342A) else Color(0xFFE2E6DD)))
                                .clickable {
                                    if (initialTaskText.isNotBlank()) {
                                        tasksList = tasksList + initialTaskText.trim()
                                        initialTaskText = ""
                                    }
                                }
                                .testTag("create_project_add_task_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Task",
                                tint = if (initialTaskText.isNotBlank()) DarkGreen else colors.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Render added tasks with interactive rounded checkbox buttons
                if (tasksList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        tasksList.forEachIndexed { index, taskTitle ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = inputBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, inputBorder.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isChecked = remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isChecked.value) Color(0xFFA8F96B) else Color.Transparent)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isChecked.value) Color(0xFFA8F96B) else colors.textSecondary.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { isChecked.value = !isChecked.value },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked.value) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Checked",
                                                tint = DarkGreen,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = taskTitle,
                                        fontSize = 12.sp,
                                        fontWeight = if (isChecked.value) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isChecked.value) colors.textPrimary else colors.textSecondary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Task",
                                        tint = colors.textSecondary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                tasksList = tasksList.filterIndexed { i, _ -> i != index }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. CARD COLOR
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

        // 7. BOTTOM ACTION BUTTONS
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
                    if (!isCreating) {
                        isCreating = true
                        onCreateProject(
                            projectName.ifBlank { "New Project" },
                            if (deadline != "Choose date") deadline else "Nov 15",
                            selectedColorOption.hexLight,
                            selectedColorOption.hexDark,
                            tasksList
                        )
                    }
                },
                enabled = !isCreating,
                shape = RoundedCornerShape(9999.dp),
                color = if (isCreating) colors.textSecondary.copy(alpha = 0.3f) else WiseLime,
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
                        text = if (isCreating) "Creating..." else "Create",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCreating) colors.textSecondary else DarkGreen
                    )
                }
            }
        }
    }
}
