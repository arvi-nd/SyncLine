package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.composed
import androidx.compose.animation.with
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.components.WiseCard
import com.example.components.WisePillButton
import com.example.components.WiseStatusChip
import com.example.model.AttachmentType
import com.example.model.FileAttachment
import com.example.model.ProjectTask
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DisplayBlackFontFamily
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.NearBlack
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel

import com.example.components.DeleteProjectConfirmationDialog
import com.example.components.ProjectOptionsMorphMenu
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ProjectDetailsScreen(
    projectId: String,
    viewModel: GlobalChatViewModel,
    onBack: () -> Unit,
    onViewChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val projectState by viewModel.selectedProject.collectAsState()
    val isLoading by viewModel.isLoadingProject.collectAsState()
    val isDeleting by viewModel.isDeletingProject.collectAsState()
    val project = projectState ?: viewModel.getProject(projectId)
    val userProfile by viewModel.userProfile.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog && project != null) {
        DeleteProjectConfirmationDialog(
            projectTitle = project.title,
            isDeleting = isDeleting,
            errorMessage = deleteErrorMessage,
            onConfirmDelete = {
                deleteErrorMessage = null
                viewModel.deleteProject(
                    projectId = project.id,
                    onSuccess = {
                        showDeleteDialog = false
                        onBack()
                    },
                    onError = { err ->
                        deleteErrorMessage = err
                    }
                )
            },
            onDismiss = {
                if (!isDeleting) showDeleteDialog = false
            },
            confirmTag = "confirm_delete_project_btn",
            cancelTag = "cancel_delete_project_btn"
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        if (isLoading && project == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = WiseLime
                )
            }
            return@Surface
        }

        if (project == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Project unavailable",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This project could not be found or you do not have permission to view it.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                WisePillButton(
                    text = "Go back",
                    onClick = onBack,
                    isPrimary = true,
                    testTag = "project_unavailable_back_btn"
                )
            }
            return@Surface
        }
        val myUid = userProfile?.uid
        val chatPartner = remember(project, myUid) {
            (listOf(project.clientUid) + project.participantUids + project.ownerUid)
                .firstOrNull { it.isNotBlank() && it != myUid }
        }
        val reduce = rememberReduceMotion()
        val doneCount = project.tasks.count { it.isCompleted }
        val accent = if (colors.isDark) WiseLime else DarkGreen

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .zIndex(20f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("project_details_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }

                ProjectOptionsMorphMenu(
                    projectTitle = project.title,
                    onDeleteClick = { showDeleteDialog = true },
                    buttonTag = "project_details_menu_btn",
                    menuTag = "project_menu_delete_item"
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title block: what this is and who it is for
                Column(modifier = Modifier.enter(0, reduce).padding(start = 4.dp, bottom = 4.dp)) {
                    Text(
                        text = project.title,
                        fontFamily = DisplayBlackFontFamily,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        letterSpacing = (-1.2).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = listOf(project.clientName, project.status.label).filter { it.isNotBlank() }.joinToString(" · "),
                        fontSize = 16.sp,
                        color = colors.textSecondary
                    )
                    if (project.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = project.description,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // Dates as quiet pills
                Row(modifier = Modifier.enter(1, reduce), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(if (project.dueDate.isNotBlank()) "Due ${project.dueDate}" else "No due date")
                    if (project.startDate.isNotBlank()) InfoPill("Started ${project.startDate}")
                }

                // Three tiles: where it stands. Lime marks the progress tile only.
                Row(
                    modifier = Modifier.enter(2, reduce).fillMaxWidth().height(112.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(project.progress, "%", "Progress", Modifier.weight(1f), reduce, highlight = true)
                    StatTile(doneCount, "", "Tasks done", Modifier.weight(1f), reduce)
                    StatTile(project.tasks.size - doneCount, "", "Tasks left", Modifier.weight(1f), reduce, outlined = true)
                }

                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Tasks", modifier = Modifier.enter(3, reduce))
                if (project.tasks.isEmpty()) {
                    Text("No tasks yet.", fontSize = 14.sp, color = colors.textSecondary, modifier = Modifier.padding(start = 4.dp))
                } else {
                    val upNext = project.tasks.indexOfFirst { !it.isCompleted }
                    project.tasks.forEachIndexed { index, task ->
                        TaskRow(
                            task = task,
                            isUpNext = index == upNext,
                            onToggle = { viewModel.toggleTask(project.id, task.id) },
                            modifier = Modifier.enter(4 + index, reduce)
                        )
                    }
                }

                // Files: shown only when this project actually has some
                if (project.files.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(title = "Files", modifier = Modifier.enter(5 + project.tasks.size, reduce))
                    project.files.forEachIndexed { i, file -> FileRow(file = file, modifier = Modifier.enter(6 + project.tasks.size + i, reduce)) }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            if (chatPartner != null) {
                WisePillButton(
                    text = "View chat",
                    onClick = { onViewChat(chatPartner) },
                    isPrimary = false, // lime is reserved for the progress panel
                    modifier = Modifier
                        .enter(4, reduce)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    testTag = "project_view_chat_btn"
                )
            }
        }
    }
}

/** Honors the system "remove animations" setting: motion collapses to the final state. */
@Composable
private fun rememberReduceMotion(): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember {
        android.provider.Settings.Global.getFloat(
            context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        ) == 0f
    }
}

/** Rises 24dp and fades in, one beat after the previous item. Critically damped, no overshoot. */
private fun Modifier.enter(index: Int, reduce: Boolean): Modifier = composed {
    var shown by remember { mutableStateOf(reduce) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 45L)
        shown = true
    }
    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 1f, stiffness = 260f),
        label = "enter"
    )
    this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 24.dp.toPx()
    }
}

/**
 * Numeric text transition: each digit that changes slides up (or down when the value falls)
 * while the old one blurs out and the new one blurs in. The first value rolls up from zero.
 */
@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
private fun RollingNumber(value: Int, suffix: String, reduce: Boolean, color: Color) {
    // The number really counts: 0, 1, 2 ... up to the value, decelerating into it. Longer trips take longer.
    var target by remember { mutableStateOf(if (reduce) value else 0) }
    var lastTarget by remember { mutableStateOf(if (reduce) value else 0) }
    var duration by remember { mutableStateOf(900) }
    androidx.compose.runtime.LaunchedEffect(value) {
        duration = (kotlin.math.abs(value - lastTarget) * 20).coerceIn(500, 1400)
        lastTarget = value
        target = value
    }
    val count by androidx.compose.animation.core.animateIntAsState(
        targetValue = target,
        animationSpec = if (reduce) androidx.compose.animation.core.snap()
        else androidx.compose.animation.core.tween(duration, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "count"
    )
    val firstPass = remember { androidx.compose.runtime.mutableStateOf(true) }
    androidx.compose.runtime.SideEffect { firstPass.value = false }
    val digits = count.toString()
    Row(verticalAlignment = Alignment.Bottom) {
        digits.forEachIndexed { index, char ->
            // Slots are keyed from the right so the units digit keeps its identity when the length changes
            androidx.compose.runtime.key(digits.length - 1 - index) {
                val visible = remember { androidx.compose.animation.core.MutableTransitionState(firstPass.value).apply { targetState = true } }
                androidx.compose.animation.AnimatedVisibility(
                    visibleState = visible,
                    enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(260)) +
                        androidx.compose.animation.slideInVertically(
                            androidx.compose.animation.core.spring(dampingRatio = 1f, stiffness = 320f)
                        ) { h -> h / 2 },
                    exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120))
                ) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = char,
                        transitionSpec = {
                            val up = targetState.code >= initialState.code
                            val slide = androidx.compose.animation.core.spring<androidx.compose.ui.unit.IntOffset>(
                                dampingRatio = 1f, stiffness = 700f
                            )
                            (androidx.compose.animation.slideInVertically(slide) { h -> if (up) h / 2 else -h / 2 } +
                                androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(150))) with
                                (androidx.compose.animation.slideOutVertically(slide) { h -> if (up) -h / 2 else h / 2 } +
                                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120)))
                        },
                        label = "digit"
                    ) { digit ->
                        val blur by transition.animateFloat(
                            transitionSpec = { androidx.compose.animation.core.tween(150) },
                            label = "digit_blur"
                        ) { state -> if (state == androidx.compose.animation.EnterExitState.Visible) 0f else 6f }
                        Text(
                            text = digit.toString(),
                            fontFamily = DisplayBlackFontFamily,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = color,
                            letterSpacing = (-0.8).sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.graphicsLayer {
                                if (blur > 0.1f && android.os.Build.VERSION.SDK_INT >= 31) {
                                    val r = blur.dp.toPx()
                                    renderEffect = androidx.compose.ui.graphics.BlurEffect(r, r, androidx.compose.ui.graphics.TileMode.Decal)
                                }
                            }
                        )
                    }
                }
            }
        }
        if (suffix.isNotEmpty()) {
            Text(
                text = suffix,
                fontFamily = DisplayBlackFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color,
                modifier = Modifier.padding(start = 1.dp, bottom = 3.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = GlobalChatTheme.colors.textPrimary,
        modifier = modifier.padding(start = 4.dp)
    )
}

@Composable
private fun InfoPill(text: String) {
    val colors = GlobalChatTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(9999.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = text, fontSize = 14.sp, color = colors.textPrimary)
    }
}

@Composable
private fun StatTile(
    value: Int,
    suffix: String,
    label: String,
    modifier: Modifier,
    reduce: Boolean,
    highlight: Boolean = false,
    outlined: Boolean = false
) {
    val colors = GlobalChatTheme.colors
    val shape = RoundedCornerShape(24.dp)
    val bg = when {
        highlight -> WiseLime
        outlined -> Color.Transparent
        else -> colors.textPrimary.copy(alpha = 0.10f)
    }
    val fg = if (highlight) DarkGreen else colors.textPrimary
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(bg)
            .then(if (outlined) Modifier.border(1.dp, colors.cardBorder, shape) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        RollingNumber(value, suffix, reduce, fg)
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (highlight) DarkGreen.copy(alpha = 0.7f) else colors.textSecondary,
            maxLines = 1
        )
    }
}

/** One big row per task. The first open task is the lime one; tapping toggles it. */
@Composable
private fun TaskRow(task: ProjectTask, isUpNext: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = GlobalChatTheme.colors
    val shape = RoundedCornerShape(24.dp)
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 1f, stiffness = 900f),
        label = "task_press"
    )
    val highlight = isUpNext && !task.isCompleted
    // Colors glide when "up next" moves to the following task
    val colorSpec = androidx.compose.animation.core.tween<Color>(durationMillis = 320, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    val bg by androidx.compose.animation.animateColorAsState(if (highlight) WiseLime else colors.cardBackground, colorSpec, label = "row_bg")
    val border by androidx.compose.animation.animateColorAsState(if (highlight) WiseLime else colors.cardBorder, colorSpec, label = "row_border")
    val fg by androidx.compose.animation.animateColorAsState(
        if (task.isCompleted) colors.textSecondary else if (highlight) DarkGreen else colors.textPrimary, colorSpec, label = "row_fg"
    )
    val sub by androidx.compose.animation.animateColorAsState(
        if (highlight) DarkGreen.copy(alpha = 0.7f) else colors.textSecondary, colorSpec, label = "row_sub"
    )
    // The tick: a short spring with a hint of overshoot, the only bounce on the screen
    val tick by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (task.isCompleted) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 520f),
        label = "tick"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .scale(scale)
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onToggle)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = task.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        androidx.compose.animation.Crossfade(
            targetState = when {
                task.isCompleted -> "Done"
                highlight -> "Up next"
                else -> "To do"
            },
            animationSpec = androidx.compose.animation.core.tween(220),
            label = "row_state"
        ) { label ->
            Text(text = label, fontSize = 14.sp, color = sub)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(1.5.dp, if (task.isCompleted) WiseLime else if (highlight) DarkGreen.copy(alpha = 0.5f) else colors.cardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = tick
                        scaleY = tick
                        alpha = tick.coerceIn(0f, 1f)
                    }
                    .background(WiseLime, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = if (task.isCompleted) "Completed" else null,
                    tint = DarkGreen,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            val t = ((tick - 0.35f) / 0.65f).coerceIn(0f, 1f)
                            scaleX = t
                            scaleY = t
                            alpha = t
                        }
                )
            }
        }
    }
}

@Composable
private fun FileRow(file: FileAttachment, modifier: Modifier = Modifier) {
    val colors = GlobalChatTheme.colors
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.cardBackground)
            .border(1.dp, colors.cardBorder, shape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Description, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = listOf(file.size, file.type).filter { it.isNotBlank() }.joinToString(" · "),
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun TaskCheckItem(
    task: ProjectTask,
    onToggle: () -> Unit
) {
    val colors = GlobalChatTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val checkShape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(checkShape)
                .background(if (task.isCompleted) WiseLime else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (task.isCompleted) WiseLime else colors.cardBorder,
                    shape = checkShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = DarkGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = task.title,
            fontSize = 16.sp,
            fontWeight = if (task.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            color = if (task.isCompleted) colors.textPrimary else colors.textSecondary
        )
    }
}

@Composable
fun ProjectFileRow(file: FileAttachment) {
    val colors = GlobalChatTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.textPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = listOf(file.size, file.type).filter { it.isNotBlank() }.joinToString(" · "),
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        }
    }
}
