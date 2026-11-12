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

internal fun extractFileInfo(
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

internal fun formatBytes(bytes: Long): String {
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
