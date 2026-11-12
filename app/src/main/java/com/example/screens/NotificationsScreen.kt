package com.example.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WisePillButton
import com.example.model.ChatRequest
import com.example.model.NotificationItem
import com.example.model.NotificationType
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DisplayBlackFontFamily
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel

/**
 * Activity, in the same language as the Overview: a lime header with one big figure, the single thing
 * that needs an answer set apart, updates hung on a spine, and everyone else as faces.
 */
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

    fun incoming(r: ChatRequest) =
        r.targetUid.equals(currentUid, ignoreCase = true) || (currentUid.isNotBlank() && r.senderUid != currentUid)
    fun pending(r: ChatRequest) = r.status.equals("PENDING", ignoreCase = true)

    val needsAnswer = chatRequests.filter { incoming(it) && pending(it) }
    val waiting = chatRequests.filter { !incoming(it) && pending(it) || r2declined(it) }
    val connected = chatRequests.filter { it.status.equals("ACCEPTED", ignoreCase = true) }
    val unread = notifications.filter { !it.isRead }
    val earlier = notifications.filter { it.isRead }

    Surface(modifier = modifier.fillMaxSize(), color = colors.screenBackground) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 188.dp),
            verticalArrangement = Arrangement.spacedBy(36.dp)
        ) {
            // Lime header with the one figure that matters
            item {
                val headerShape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                val big: String
                val unit: String
                val sub: String
                when {
                    needsAnswer.isNotEmpty() -> {
                        big = "${needsAnswer.size}"
                        unit = if (needsAnswer.size == 1) "request needs your answer" else "requests need your answer"
                        sub = needsAnswer.joinToString(", ") { it.senderDisplayName.ifBlank { it.senderUsername } }
                    }
                    unread.isNotEmpty() -> {
                        big = "${unread.size}"
                        unit = if (unread.size == 1) "new update" else "new updates"
                        sub = unread.take(3).joinToString(", ") { it.sender }
                    }
                    else -> {
                        big = ""
                        unit = "You're all caught up"
                        sub = ""
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (colors.isDark) 0.dp else 20.dp,
                            shape = headerShape,
                            ambientColor = Color(0x669FE870),
                            spotColor = Color(0x669FE870)
                        )
                        .clip(headerShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFFB4F08C), WiseLime, Color(0xFF8EDB63))))
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activity",
                            fontFamily = DisplayBlackFontFamily,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkGreen,
                            letterSpacing = (-1.2).sp
                        )
                        if (unread.isNotEmpty()) {
                            Text(
                                text = "Mark all read",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WiseLime,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(DarkGreen)
                                    .clickable { viewModel.markAllNotificationsRead() }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                    .testTag("mark_all_read_btn")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    if (big.isNotEmpty()) {
                        Text(
                            big,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            letterSpacing = (-1.5).sp
                        )
                    }
                    Text(unit, fontSize = if (big.isEmpty()) 26.sp else 20.sp, fontWeight = if (big.isEmpty()) FontWeight.Bold else FontWeight.SemiBold, color = DarkGreen)
                    if (big.isEmpty() && (connected.isNotEmpty() || waiting.isNotEmpty())) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            if (connected.isNotEmpty()) Tally(connected.size, "connected")
                            if (waiting.isNotEmpty()) Tally(waiting.size, "waiting on")
                        }
                    }
                    if (sub.isNotBlank()) {
                        Text(sub, fontSize = 15.sp, color = DarkGreen.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // The one actionable object: an incoming request, with the answer under your thumb
            if (needsAnswer.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Needs your answer", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, letterSpacing = (-0.4).sp)
                        needsAnswer.forEach { req ->
                            val name = req.senderDisplayName.ifBlank { req.senderUsername }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(WiseLime.copy(alpha = if (colors.isDark) 0.14f else 0.32f))
                                    .padding(20.dp)
                                    .testTag("chat_request_card_${req.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Face(initial = initialOf(name, req.senderUsername), size = 64)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("@${req.senderUsername} wants to connect", fontSize = 14.sp, color = colors.textSecondary, maxLines = 2)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    WisePillButton(
                                        text = "Decline",
                                        onClick = { viewModel.declineChatRequest(req.id) },
                                        isPrimary = false,
                                        modifier = Modifier.weight(1f),
                                        testTag = "decline_request_${req.id}"
                                    )
                                    WisePillButton(
                                        text = "Accept",
                                        onClick = { viewModel.acceptChatRequest(req) },
                                        isPrimary = true,
                                        modifier = Modifier.weight(1f),
                                        testTag = "accept_request_${req.id}"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Updates on a spine: new ones filled in lime, earlier ones outlined
            if (notifications.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().testTag("activity_updates")) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Updates", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, letterSpacing = (-0.4).sp)
                            if (unread.isNotEmpty()) {
                                Text("${unread.size} new", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val all = unread + earlier
                        all.forEachIndexed { index, item ->
                            UpdateRow(item = item, first = index == 0, last = index == all.lastIndex)
                        }
                    }
                }
            }

            // Pending and declined, plainly
            if (waiting.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                        Text("Waiting on", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, letterSpacing = (-0.4).sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        waiting.forEachIndexed { index, req ->
                            val name = req.targetDisplayName.ifBlank { req.targetUsername }
                            if (index > 0) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.cardBorder))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("chat_request_card_${req.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Face(initial = initialOf(name, req.targetUsername), size = 44, tint = false)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("@${req.targetUsername}", fontSize = 14.sp, color = colors.textSecondary)
                                }
                                Text(
                                    if (req.status.equals("DECLINED", true)) "Declined" else "Request sent",
                                    fontSize = 14.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Connected: everyone who said yes, as faces
            if (connected.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Connected",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            letterSpacing = (-0.4).sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            connected.forEach { req ->
                                val other = if (incoming(req)) {
                                    req.senderDisplayName.ifBlank { req.senderUsername } to req.senderUsername
                                } else {
                                    req.targetDisplayName.ifBlank { req.targetUsername } to req.targetUsername
                                }
                                Column(
                                    modifier = Modifier.width(72.dp).testTag("chat_request_card_${req.id}"),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Face(initial = initialOf(other.first, other.second), size = 68)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        other.first,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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

private fun r2declined(r: ChatRequest) = r.status.equals("DECLINED", ignoreCase = true)

private fun initialOf(name: String, username: String): String =
    name.firstOrNull()?.uppercase() ?: username.take(1).uppercase().ifBlank { "?" }

@Composable
private fun Face(initial: String, size: Int, tint: Boolean = true) {
    val colors = GlobalChatTheme.colors
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                if (tint) Brush.verticalGradient(
                    listOf(
                        WiseLime.copy(alpha = if (colors.isDark) 0.30f else 0.60f),
                        WiseLime.copy(alpha = if (colors.isDark) 0.14f else 0.30f)
                    )
                ) else Brush.verticalGradient(listOf(colors.textPrimary.copy(alpha = 0.08f), colors.textPrimary.copy(alpha = 0.08f)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initial,
            fontFamily = DisplayBlackFontFamily,
            fontSize = (size * 0.42f).sp,
            fontWeight = FontWeight.Black,
            color = if (tint) (if (colors.isDark) WiseLime else DarkGreen) else colors.textPrimary
        )
    }
}

/** Eases down on touch-down, springs back on release; never consumes the touch. */
private fun Modifier.press(pressedScale: Float = 0.98f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 900f),
        label = "press"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                pressed = true
                waitForUpOrCancellation(pass = PointerEventPass.Initial)
                pressed = false
            }
        }
}

/** One update on the spine: a node carrying its icon, the line running through, the time on the right. */
@Composable
private fun UpdateRow(item: NotificationItem, first: Boolean, last: Boolean) {
    val colors = GlobalChatTheme.colors
    val lineColor = colors.textPrimary.copy(alpha = 0.14f)
    val fresh = !item.isRead
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .press()
            .clickable { }
            .padding(horizontal = 24.dp)
            .testTag("activity_update_${item.id}")
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight()
                .drawBehind {
                    val cx = size.width / 2
                    val cy = 22.dp.toPx()
                    if (!last) drawLine(lineColor, Offset(cx, cy), Offset(cx, size.height), strokeWidth = 2.dp.toPx())
                    if (!first) drawLine(lineColor, Offset(cx, 0f), Offset(cx, cy), strokeWidth = 2.dp.toPx())
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (fresh) WiseLime else colors.screenBackground)
                    .then(
                        if (fresh) Modifier else Modifier.background(colors.textPrimary.copy(alpha = 0.08f))
                    ),
                contentAlignment = Alignment.Center
            ) {
                val tint = if (fresh) DarkGreen else colors.textSecondary
                when (item.type) {
                    NotificationType.PROJECT_UPDATE ->
                        Icon(Icons.Outlined.Folder, null, tint = tint, modifier = Modifier.size(22.dp))
                    NotificationType.SYSTEM ->
                        Icon(Icons.Default.Notifications, null, tint = tint, modifier = Modifier.size(22.dp))
                    else ->
                        Text(
                            item.avatarInitial.ifBlank { initialOf(item.sender, "") }.take(2),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = tint
                        )
                }
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).padding(bottom = 22.dp, top = 2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    fontSize = 17.sp,
                    fontWeight = if (fresh) FontWeight.Bold else FontWeight.Medium,
                    color = if (fresh) colors.textPrimary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(item.timestamp, fontSize = 13.sp, color = colors.textSecondary)
            }
            Text(item.description, fontSize = 14.sp, color = colors.textSecondary)
        }
    }
}

/** A small count with its label, for the quiet header. */
@Composable
private fun Tally(count: Int, label: String) {
    Column {
        Text("$count", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkGreen, letterSpacing = (-1).sp)
        Text(label, fontSize = 14.sp, color = DarkGreen.copy(alpha = 0.75f))
    }
}
