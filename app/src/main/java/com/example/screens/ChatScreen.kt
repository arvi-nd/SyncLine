package com.example.screens

import com.example.model.formatAmount
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale
import com.example.model.Invoice
import com.example.model.getDisplayTime
import java.util.Locale
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WiseAvatar
import com.example.components.WiseCard
import com.example.engine.TimezoneEngine
import com.example.model.AttachmentType
import com.example.model.Client
import com.example.model.FileAttachment
import com.example.model.Meeting
import com.example.model.Message
import com.example.model.Project
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.NearBlack
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel

@Composable
fun ChatScreen(
    clientId: String,
    viewModel: GlobalChatViewModel,
    onBack: () -> Unit,
    onOpenClientDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    showInternalComposer: Boolean = false,
    bottomContentPadding: Dp = 100.dp
) {
    val colors = GlobalChatTheme.colors
    val client = viewModel.getClient(clientId) ?: return
    val messages by viewModel.messages.collectAsState()
    val chatError by viewModel.chatErrorMessage.collectAsState()
    val isSending by viewModel.isSendingMessage.collectAsState()
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val imeBottomDp = with(density) { imeBottomPx.toDp() }
    val isKeyboardOpen = imeBottomPx > 0

    // Initialize listState already scrolled to the bottom (most recent messages) on frame 0
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (messages.size - 1).coerceAtLeast(0)
    )

    val prevMessageCount = remember(clientId) { mutableStateOf(messages.size) }

    var composerText by remember { mutableStateOf("") }

    val clientTime = TimezoneEngine.formatCurrentTime(client.timezoneId)

    val chatOpenAnim = remember(clientId) { Animatable(0f) }

    LaunchedEffect(clientId) {
        viewModel.setActiveChatClient(clientId)
        chatOpenAnim.snapTo(0f)
        chatOpenAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = 320f
            )
        )
    }

    // Scroll to new message smoothly so preceding bubbles glide up in complete sync with the new bubble
    LaunchedEffect(messages.size) {
        if (messages.size > prevMessageCount.value && messages.isNotEmpty()) {
            val lastIndex = (messages.size - 1).coerceAtLeast(0)
            listState.animateScrollToItem(lastIndex)
        }
        prevMessageCount.value = messages.size
    }

    // Keep chat list in instant 1:1 sync with keyboard motion without delay
    LaunchedEffect(isKeyboardOpen, imeBottomPx) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem((messages.size - 1).coerceAtLeast(0))
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .then(
                    if (showInternalComposer) {
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val isKeyboardOpen = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(density) > 0
                        if (!isKeyboardOpen) Modifier.navigationBarsPadding() else Modifier
                    } else {
                        Modifier
                    }
                )
        ) {
            // Chat Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenClientDetails(client.id) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WiseAvatar(
                        name = client.name,
                        initial = client.avatarInitial,
                        backgroundColor = client.avatarBg,
                        size = 40.dp,
                        isOnline = client.isOnline
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = client.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${client.country} · $clientTime",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.testTag("chat_phone_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Error Banner (surfaces send errors without interrupting layout)
            if (chatError != null) {
                Surface(
                    color = com.example.ui.theme.SentimentNegative.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.SentimentNegative.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { viewModel.clearChatError() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chatError ?: "Failed to send message",
                            fontSize = 12.sp,
                            color = com.example.ui.theme.SentimentNegative,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dismiss",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.SentimentNegative
                        )
                    }
                }
            }

            // Track animated message IDs and initial load status per client
            val animatedMessageIds = remember(clientId) { mutableStateListOf<String>() }
            val isInitialLoadDone = remember(clientId) { mutableStateOf(false) }

            // Mark all initial messages as already animated when opening chat so existing messages NEVER bounce and list opens at bottom
            LaunchedEffect(clientId, messages) {
                if (messages.isNotEmpty()) {
                    if (!isInitialLoadDone.value) {
                        messages.forEach { msg ->
                            if (!animatedMessageIds.contains(msg.id)) {
                                animatedMessageIds.add(msg.id)
                            }
                        }
                        listState.scrollToItem((messages.size - 1).coerceAtLeast(0))
                        isInitialLoadDone.value = true
                    }
                }
            }

            LaunchedEffect(clientId) {
                kotlinx.coroutines.delay(80)
                if (messages.isNotEmpty()) {
                    listState.scrollToItem((messages.size - 1).coerceAtLeast(0))
                }
                isInitialLoadDone.value = true
            }

            val openProgress = chatOpenAnim.value
            val listOffsetY = ((-28f) * (1f - openProgress)).dp
            val listAlpha = (0.2f + 0.8f * openProgress).coerceIn(0f, 1f)
            val dynamicBottomPadding = (bottomContentPadding + 4.dp).coerceAtLeast(8.dp)

            // Messages List - anchored above navigation bar (Alignment.Bottom) with slide-down entry
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        translationY = listOffsetY.toPx()
                        this.alpha = listAlpha
                    },
                contentPadding = PaddingValues(bottom = dynamicBottomPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
            ) {
                itemsIndexed(messages, key = { _, msg -> msg.id }) { index, msg ->
                    val isPrevSameSender = index > 0 && messages[index - 1].isOutgoing == msg.isOutgoing
                    val isNextSameSender = index < messages.size - 1 && messages[index + 1].isOutgoing == msg.isOutgoing
                    val isAlreadyAnimated = animatedMessageIds.contains(msg.id)

                    AnimatedMessageBubble(
                        msg = msg,
                        isPrevSameSender = isPrevSameSender,
                        isNextSameSender = isNextSameSender,
                        isAlreadyAnimated = isAlreadyAnimated,
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = spring(
                                dampingRatio = 0.68f,
                                stiffness = 240f
                            )
                        ),
                        onAnimationComplete = { id ->
                            if (!animatedMessageIds.contains(id)) {
                                animatedMessageIds.add(id)
                            }
                        },
                        onMarkInvoicePaid = { invoiceId -> viewModel.markInvoicePaid(invoiceId) },
                        onToggleTask = { projId, taskId -> viewModel.toggleTask(projId, taskId) }
                    )
                }
            }

            // Message Composer
            if (showInternalComposer) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = composerText,
                        onValueChange = { composerText = it },
                        placeholder = { Text("Type a message...", color = colors.textSecondary) },
                        singleLine = true,
                        leadingIcon = {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Attach file",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                val textToSend = composerText.trim()
                                if (textToSend.isNotBlank() && !isSending) {
                                    viewModel.sendMessage(client.id, textToSend) { success ->
                                        if (success) {
                                            composerText = ""
                                        }
                                    }
                                }
                            }
                        ),
                        shape = RoundedCornerShape(9999.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.cardBorder,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedContainerColor = colors.cardBackground,
                            unfocusedContainerColor = colors.cardBackground,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input_internal")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    val internalSendInteractionSource = remember { MutableInteractionSource() }
                    val isInternalPressed by internalSendInteractionSource.collectIsPressedAsState()

                    val internalBtnScale by animateFloatAsState(
                        targetValue = if (isInternalPressed) 0.88f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                        ),
                        label = "internal_send_btn_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .scale(internalBtnScale)
                            .clip(CircleShape)
                            .background(WiseLime)
                            .clickable(
                                interactionSource = internalSendInteractionSource,
                                indication = null
                            ) {
                                val textToSend = composerText.trim()
                                if (textToSend.isNotBlank() && !isSending) {
                                    viewModel.sendMessage(client.id, textToSend) { success ->
                                        if (success) {
                                            composerText = ""
                                        }
                                    }
                                }
                            }
                            .testTag("chat_send_button_internal"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Send",
                            tint = DarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun AnimatedMessageBubble(
    msg: Message,
    isPrevSameSender: Boolean = false,
    isNextSameSender: Boolean = false,
    isAlreadyAnimated: Boolean,
    onAnimationComplete: (String) -> Unit,
    onMarkInvoicePaid: (String) -> Unit,
    onToggleTask: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val animProgress = remember(msg.id) { Animatable(if (isAlreadyAnimated) 1f else 0f) }

    LaunchedEffect(msg.id, isAlreadyAnimated) {
        if (isAlreadyAnimated) {
            animProgress.snapTo(1f)
        } else {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = if (msg.isOutgoing) 0.62f else 0.75f,
                    stiffness = if (msg.isOutgoing) 240f else 260f
                )
            )
            onAnimationComplete(msg.id)
        }
    }

    val progress = animProgress.value

    // Smooth fluid scale and translation directly on GPU render layer (0% layout re-measurement jitter)
    val scaleX = if (msg.isOutgoing) {
        0.28f + 0.72f * progress
    } else {
        0.40f + 0.60f * progress
    }

    val scaleY = if (msg.isOutgoing) {
        if (progress < 0.55f) {
            0.22f + 1.15f * progress
        } else {
            1.15f - 0.15f * ((progress - 0.55f) / 0.45f)
        }
    } else {
        0.40f + 0.60f * progress
    }

    val alpha = (progress * 2.2f).coerceIn(0f, 1f)

    val offsetY = if (msg.isOutgoing) {
        (16f * (1f - progress)).dp
    } else {
        (12f * (1f - progress)).dp
    }

    val offsetX = if (msg.isOutgoing) {
        (8f * (1f - progress)).dp
    } else {
        (-8f * (1f - progress)).dp
    }

    val transformOrigin = if (msg.isOutgoing) {
        TransformOrigin(0.98f, 0.98f)
    } else {
        TransformOrigin(0.02f, 0.98f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
                translationX = offsetX.toPx()
                translationY = offsetY.toPx()
                this.alpha = alpha
                this.transformOrigin = transformOrigin
            }
    ) {
        MessageBubbleRow(
            msg = msg,
            isPrevSameSender = isPrevSameSender,
            isNextSameSender = isNextSameSender,
            animProgress = progress,
            onMarkInvoicePaid = onMarkInvoicePaid,
            onToggleTask = onToggleTask
        )
    }
}

@Composable
fun MessageBubbleRow(
    msg: Message,
    isPrevSameSender: Boolean = false,
    isNextSameSender: Boolean = false,
    animProgress: Float = 1f,
    onMarkInvoicePaid: (String) -> Unit = {},
    onToggleTask: (String, String) -> Unit = { _, _ -> }
) {
    val colors = GlobalChatTheme.colors

    val alignment = if (msg.isOutgoing) Alignment.End else Alignment.Start

    // Dynamic morphing corners when multiple messages from the same sender are stacked:
    // Top messages in group morph their tail corner into smooth 18.dp capsule so only bottom message has the tail
    val targetBottomStart = if (!msg.isOutgoing && isNextSameSender) 18.dp else if (!msg.isOutgoing) 4.dp else 18.dp
    val targetBottomEnd = if (msg.isOutgoing && isNextSameSender) 18.dp else if (msg.isOutgoing) 4.dp else 18.dp

    val animBottomStart by animateDpAsState(
        targetValue = targetBottomStart,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "bottomStartCorner"
    )

    val animBottomEnd by animateDpAsState(
        targetValue = targetBottomEnd,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "bottomEndCorner"
    )

    val isInvoiceMessage = msg.type == "invoice_share" || msg.invoiceCard != null || !msg.invoiceId.isNullOrBlank()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (isInvoiceMessage) {
            val displayInvoice = msg.invoiceCard ?: Invoice(
                id = msg.invoiceId ?: msg.id,
                title = msg.text.removePrefix("Invoice: ").substringBefore(" (").ifBlank { "Invoice" },
                amount = 0.0,
                status = "UNPAID",
                isPaid = false
            )
            LargeInvoiceCardBubble(
                invoice = displayInvoice,
                isOutgoing = msg.isOutgoing,
                onMarkPaid = { onMarkInvoicePaid(displayInvoice.id) }
            )
        } else if (msg.projectCard != null) {
            MinimalProjectCardBubble(
                project = msg.projectCard,
                isOutgoing = msg.isOutgoing,
                onToggleTask = onToggleTask
            )
        }

        if (msg.meetingCard != null) {
            MeetingInfoCardBubble(meeting = msg.meetingCard, isOutgoing = msg.isOutgoing)
        }

        if (msg.attachment != null) {
            AttachmentBubble(attachment = msg.attachment, isOutgoing = msg.isOutgoing)
        }

        if (msg.text.isNotEmpty()) {
            val bubbleColor = if (msg.isOutgoing) colors.brandLime else colors.cardBackground
            val textColor = if (msg.isOutgoing) DarkGreen else colors.textPrimary

            val topStartCorner = if (msg.isOutgoing) {
                (23f * (1f - animProgress) + 18f * animProgress).dp
            } else 18.dp

            val topEndCorner = if (msg.isOutgoing) {
                (23f * (1f - animProgress) + 18f * animProgress).dp
            } else 18.dp

            val bottomStartCorner = if (msg.isOutgoing) {
                (23f * (1f - animProgress) + 18f * animProgress).dp
            } else {
                (22f * (1f - animProgress) + animBottomStart.value * animProgress).dp
            }

            val bottomEndCorner = if (msg.isOutgoing) {
                (23f * (1f - animProgress) + animBottomEnd.value * animProgress).dp
            } else 18.dp

            val bubbleShape = RoundedCornerShape(
                topStart = topStartCorner,
                topEnd = topEndCorner,
                bottomStart = bottomStartCorner,
                bottomEnd = bottomEndCorner
            )

            val border = if (!msg.isOutgoing) {
                androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
            } else null

            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                border = border,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = msg.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalProjectCardBubble(
    project: Project,
    isOutgoing: Boolean,
    onToggleTask: (String, String) -> Unit = { _, _ -> }
) {
    val colors = GlobalChatTheme.colors
    val cardBg = if (colors.isDark) Color(project.cardColorHexDark) else Color(project.cardColorHexLight)
    val border = if (colors.isDark) {
        Color(project.cardColorHexDark).copy(alpha = 0.9f)
    } else {
        Color(project.cardColorHexLight).copy(alpha = 0.9f)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = Modifier
            .width(260.dp)
            .padding(bottom = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (colors.isDark) Color.Black.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkOutline,
                        contentDescription = "Project",
                        tint = if (colors.isDark) WiseLime else DarkGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Client · ${project.clientName}",
                        fontSize = 11.5.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tasks Checklist inside Chat Card
            if (project.tasks.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    project.tasks.forEach { task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleTask(project.id, task.id) }
                                .padding(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (task.isCompleted) WiseLime else Color.Transparent)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (task.isCompleted) WiseLime else colors.textSecondary.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(5.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (task.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = DarkGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = task.title,
                                fontSize = 12.sp,
                                fontWeight = if (task.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (task.isCompleted) colors.textPrimary else colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (colors.isDark) WiseLime.copy(alpha = 0.22f) else DarkGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = project.status.label,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) WiseLime else DarkGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "Due ${project.dueDate}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun AttachmentBubble(
    attachment: FileAttachment,
    isOutgoing: Boolean
) {
    val colors = GlobalChatTheme.colors

    WiseCard(
        cornerRadius = 16.dp,
        modifier = Modifier
            .width(240.dp)
            .padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isImg = attachment.attachmentType == AttachmentType.IMAGE
            val attBg = if (isImg) Color(0xFFFAF0E6) else Color(0xFFFEE2E2)
            val attTint = if (isImg) Color(0xFFD97706) else Color(0xFFDC2626)
            val attIcon = if (isImg) Icons.Outlined.Image else Icons.Outlined.Description

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(attBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = attIcon,
                    contentDescription = null,
                    tint = attTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${attachment.size} · ${attachment.type}",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Download",
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun MeetingInfoCardBubble(
    meeting: Meeting,
    isOutgoing: Boolean
) {
    val colors = GlobalChatTheme.colors

    val cardBg = if (colors.isDark) Color(0xFF1E211C) else Color(0xFFF9FAF7)
    val cardBorder = if (colors.isDark) Color(0xFF2E322A) else Color(0xFFE2E6DC)

    val dateText = meeting.dateString
    val monthStr = if (dateText.contains("Oct", ignoreCase = true)) "OCT"
                   else if (dateText.contains("Nov", ignoreCase = true)) "NOV"
                   else if (dateText.contains("Dec", ignoreCase = true)) "DEC"
                   else "OCT"
    val digits = dateText.filter { it.isDigit() }
    val dayDigits = if (digits.isNotEmpty()) digits else "28"

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .width(270.dp)
            .padding(bottom = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Date Squircle Box
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (colors.isDark) Color(0xFF252922) else Color(0xFFEFF3EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = monthStr,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (colors.isDark) WiseLime else Color(0xFF335832)
                        )
                        Text(
                            text = dayDigits,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Meeting details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meeting.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${meeting.getDisplayTime()} · ${meeting.duration}",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Confirmed or Status badge
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (colors.isDark) WiseLime.copy(alpha = 0.35f) else Color(0xFFC7ECB0)
                        )
                    ) {
                        Text(
                            text = meeting.status,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (colors.isDark) WiseLime else DarkGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (!meeting.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (colors.isDark) Color(0xFF242721) else Color(0xFFF2F4EE))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = meeting.notes,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LargeInvoiceCardBubble(
    invoice: Invoice,
    isOutgoing: Boolean,
    onMarkPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "invoice_paid_button_scale"
    )

    val cardBg = if (colors.isDark) Color(0xFF1E211C) else Color(0xFFFFFFFF)
    val cardBorder = if (invoice.isPaid) {
        if (colors.isDark) WiseLime.copy(alpha = 0.45f) else Color(0xFFC7ECB0)
    } else {
        if (colors.isDark) Color(0xFF2E322A) else Color(0xFFE2E6DC)
    }

    val formattedAmount = invoice.formatAmount()

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        shadowElevation = if (colors.isDark) 0.dp else 1.dp,
        modifier = modifier
            .widthIn(min = 280.dp, max = 320.dp)
            .padding(bottom = 6.dp)
            .testTag("invoice_card_${invoice.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Invoice Badge, Number, Date, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Receipt squircle icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0xFF243A1B) else Color(0xFFEBF7E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = "Invoice",
                        tint = if (colors.isDark) WiseLime else Color(0xFF1E4620),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.invoiceNumber,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Wise Invoice · ${invoice.createdAt}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (invoice.isPaid) {
                        if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)
                    } else {
                        if (colors.isDark) Color(0xFF382914) else Color(0xFFFFF3D6)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (invoice.isPaid) {
                            if (colors.isDark) WiseLime.copy(alpha = 0.4f) else Color(0xFFC7ECB0)
                        } else {
                            if (colors.isDark) Color(0xFF5E431B) else Color(0xFFFDE68A)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (invoice.isPaid) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colors.isDark) WiseLime else DarkGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "PAID",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (colors.isDark) WiseLime else DarkGreen
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD97706))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "UNPAID",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Invoice Title & Client
            Text(
                text = invoice.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Billed to: ${invoice.clientName}",
                    fontSize = 12.5.sp,
                    color = colors.textSecondary
                )
                if (!invoice.projectName.isNullOrBlank()) {
                    Text(
                        text = " · ${invoice.projectName}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (colors.isDark) WiseLime else DarkGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thin Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(if (colors.isDark) Color(0xFF2C3028) else Color(0xFFE8EBE4))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Big Amount Display
            Column {
                Text(
                    text = "TOTAL AMOUNT DUE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = colors.textSecondary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedAmount,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    lineHeight = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Due Date Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (colors.isDark) Color(0xFF252922) else Color(0xFFF4F6F1))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Due date: ${invoice.dueDate}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }

            // Optional Notes
            if (!invoice.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (colors.isDark) Color(0xFF222620) else Color(0xFFF7F8F5))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Note: ${invoice.notes}",
                        fontSize = 11.5.sp,
                        color = colors.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Receiver Action Section (Paid Button or Paid State with Morphing Animation)
            AnimatedContent(
                targetState = invoice.isPaid,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.88f, animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f)))
                        .togetherWith(fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.92f))
                },
                label = "invoice_paid_morph_transition"
            ) { isPaid ->
                if (!isPaid) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Receiver's Paid Button
                        Surface(
                            onClick = onMarkPaid,
                            interactionSource = interactionSource,
                            shape = RoundedCornerShape(9999.dp),
                            color = WiseLime,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .scale(buttonScale)
                                .testTag("pay_invoice_button_${invoice.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = DarkGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mark as Paid ($formattedAmount)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen
                                )
                            }
                        }
                    }
                } else {
                    // Morphed Payment Received Confirmation Card with popping check-mark animation
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (colors.isDark) Color(0xFF22381E) else Color(0xFFEBF7E5),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (colors.isDark) WiseLime.copy(alpha = 0.35f) else Color(0xFFC7ECB0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invoice_paid_banner_${invoice.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var iconVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                iconVisible = true
                            }
                            val checkScale by animateFloatAsState(
                                targetValue = if (iconVisible) 1.0f else 0.2f,
                                animationSpec = spring(
                                    dampingRatio = 0.5f,
                                    stiffness = 380f
                                ),
                                label = "check_mark_pop"
                            )

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .scale(checkScale)
                                    .clip(CircleShape)
                                    .background(if (colors.isDark) WiseLime else Color(0xFF163300)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Paid Checkmark",
                                    tint = if (colors.isDark) DarkGreen else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Payment Received",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (colors.isDark) WiseLime else DarkGreen
                                )

                                val displayPaidTime = remember(invoice.paidAt) {
                                    val raw = invoice.paidAt ?: "Oct 26, 2026 at 7:27 AM"
                                    val cleaned = raw.replace(" · Paid via Wise", "")
                                                     .replace(" · via Wise Transfer", "")
                                                     .replace("via Wise Direct Debit", "")
                                                     .replace("Transferred to Wise account", "")
                                                     .trim()
                                    if (cleaned.startsWith("Paid on")) cleaned else "Paid on $cleaned"
                                }

                                Text(
                                    text = displayPaidTime,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
