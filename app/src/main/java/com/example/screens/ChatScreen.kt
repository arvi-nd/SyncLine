package com.example.screens

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
    val listState = rememberLazyListState()

    var composerText by remember { mutableStateOf("") }

    val clientTime = TimezoneEngine.formatCurrentTime(client.timezoneId)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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
                        Modifier
                            .navigationBarsPadding()
                            .imePadding()
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

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = bottomContentPadding + 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubbleRow(
                        msg = msg,
                        onMarkInvoicePaid = { invoiceId -> viewModel.markInvoicePaid(invoiceId) }
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
                                viewModel.sendMessage(client.id, composerText)
                                composerText = ""
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

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WiseLime)
                            .clickable {
                                viewModel.sendMessage(client.id, composerText)
                                composerText = ""
                            }
                            .testTag("chat_send_button_internal"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
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
fun MessageBubbleRow(
    msg: Message,
    onMarkInvoicePaid: (String) -> Unit = {}
) {
    val colors = GlobalChatTheme.colors

    val alignment = if (msg.isOutgoing) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (msg.invoiceCard != null) {
            LargeInvoiceCardBubble(
                invoice = msg.invoiceCard,
                isOutgoing = msg.isOutgoing,
                onMarkPaid = { onMarkInvoicePaid(msg.invoiceCard.id) }
            )
        }

        if (msg.projectCard != null) {
            MinimalProjectCardBubble(project = msg.projectCard, isOutgoing = msg.isOutgoing)
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
            val borderModifier = if (!msg.isOutgoing) {
                Modifier.border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp))
            } else Modifier

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (msg.isOutgoing) 18.dp else 4.dp,
                    bottomEnd = if (msg.isOutgoing) 4.dp else 18.dp
                ),
                color = bubbleColor,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .then(borderModifier)
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

        // Timestamp
        Row(
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = msg.timestamp,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            if (msg.isOutgoing) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Read",
                    tint = WiseLime,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun MinimalProjectCardBubble(
    project: Project,
    isOutgoing: Boolean
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
                        text = "${meeting.time} · ${meeting.duration}",
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

    val formattedAmount = String.format(Locale.US, "$%,.2f", invoice.amount)

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
