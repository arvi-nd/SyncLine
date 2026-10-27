package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.components.WiseAvatar
import com.example.components.WisePillButton
import com.example.model.ChatRequest
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel

@Composable
fun NotificationsScreen(
    viewModel: GlobalChatViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val notifications by viewModel.notifications.collectAsState()
    val chatRequests by viewModel.chatRequests.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentUid = currentUser?.uid ?: ""

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("notifications_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "Activity",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Text(
                    text = "Mark all read",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.brandLime,
                    modifier = Modifier
                        .clickable { viewModel.markAllNotificationsRead() }
                        .testTag("mark_all_read_btn")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications & Requests List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (chatRequests.isNotEmpty()) {
                    item {
                        Text(
                            text = "CHAT REQUESTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = colors.textSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                    }

                    items(chatRequests, key = { "req_${it.id}" }) { req ->
                        ChatRequestCard(
                            request = req,
                            currentUid = currentUid,
                            onAccept = { viewModel.acceptChatRequest(req) },
                            onDecline = { viewModel.declineChatRequest(req.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "SYSTEM & UPDATES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = colors.textSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                    }
                }

                items(notifications, key = { it.id }) { item ->
                    NotificationRow(item = item)
                }
            }
        }
    }
}

@Composable
fun ChatRequestCard(
    request: ChatRequest,
    currentUid: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val colors = GlobalChatTheme.colors
    val isIncoming = request.targetUid == currentUid

    val titleText = if (isIncoming) {
        "Chat request from ${request.senderDisplayName.ifBlank { request.senderUsername }}"
    } else {
        "Chat request to ${request.targetDisplayName.ifBlank { request.targetUsername }}"
    }

    val subtitleText = if (isIncoming) {
        "@${request.senderUsername} wants to connect with you"
    } else {
        "Request sent to @${request.targetUsername}"
    }

    val avatarInitial = if (isIncoming) {
        request.senderDisplayName.firstOrNull()?.toString()?.uppercase()
            ?: request.senderUsername.take(2).uppercase().ifBlank { "CR" }
    } else {
        request.targetDisplayName.firstOrNull()?.toString()?.uppercase()
            ?: request.targetUsername.take(2).uppercase().ifBlank { "CR" }
    }

    val avatarName = if (isIncoming) request.senderDisplayName else request.targetDisplayName

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_request_card_${request.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WiseAvatar(
                    name = avatarName,
                    initial = avatarInitial,
                    backgroundColor = if (isIncoming) WiseLime else Color(0xFFC7E2FE),
                    size = 44.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        fontSize = 12.5.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Chip
                val chipText = when (request.status) {
                    "PENDING" -> if (isIncoming) "Pending" else "Sent"
                    "ACCEPTED" -> "Accepted"
                    "DECLINED" -> "Declined"
                    else -> request.status
                }
                val chipBg = when (request.status) {
                    "PENDING" -> if (isIncoming) WiseLime.copy(alpha = 0.25f) else Color(0xFFFFF3C4)
                    "ACCEPTED" -> Color(0xFFE2F6D5)
                    "DECLINED" -> Color(0xFFFDE8E8)
                    else -> colors.cardBorder.copy(alpha = 0.3f)
                }
                val chipTextCol = when (request.status) {
                    "PENDING" -> if (isIncoming) DarkGreen else Color(0xFF8C5800)
                    "ACCEPTED" -> DarkGreen
                    "DECLINED" -> Color(0xFFD03238)
                    else -> colors.textSecondary
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(chipBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chipText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = chipTextCol
                    )
                }
            }

            // Accept & Decline buttons for incoming pending requests
            if (isIncoming && request.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WisePillButton(
                        text = "Decline",
                        onClick = onDecline,
                        isPrimary = false,
                        modifier = Modifier.weight(1f),
                        testTag = "decline_request_${request.id}"
                    )

                    WisePillButton(
                        text = "Accept",
                        onClick = onAccept,
                        isPrimary = true,
                        modifier = Modifier.weight(1f),
                        testTag = "accept_request_${request.id}"
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationRow(item: NotificationItem) {
    val colors = GlobalChatTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (item.type) {
            NotificationType.PROJECT_UPDATE -> {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(WiseLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            NotificationType.SYSTEM -> {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            else -> {
                WiseAvatar(
                    name = item.sender,
                    initial = item.avatarInitial,
                    backgroundColor = item.avatarBg ?: Color(0xFFC7E2FE),
                    size = 46.dp
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${item.description} · ${item.timestamp}",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }

        if (!item.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(WiseLime)
            )
        }
    }
}
