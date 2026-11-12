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
