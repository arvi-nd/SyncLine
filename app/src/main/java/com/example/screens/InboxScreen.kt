package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WiseCard
import com.example.data.MockDataProvider
import com.example.model.Client
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.NearBlack
import com.example.ui.theme.SentimentPositive
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel
import com.example.viewmodel.InboxFilter

@Composable
fun InboxScreen(
    viewModel: GlobalChatViewModel,
    onOpenChat: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val inboxItems by viewModel.inboxItems.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val activeFilter by viewModel.inboxFilter.collectAsState()
    val projects by viewModel.projects.collectAsState()

    var debugTapCount by remember { mutableIntStateOf(0) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val unreadCount = inboxItems.count { it.unreadCount > 0 }
    val projectChats = inboxItems.filter { item ->
        projects.any { p -> p.clientUid == item.client.id || p.ownerUid == item.client.id || item.client.id in p.participantUids }
    }
    val filteredList = when (selectedFilterIndex) {
        1 -> inboxItems.filter { it.unreadCount > 0 }
        2 -> projectChats
        else -> inboxItems
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Header panel: search pill, large title, contacts with an Add circle, filters
            val headerShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(headerShape)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFB4F08C), WiseLime, Color(0xFF8EDB63))),
                        headerShape
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 20.dp)
                ) {
                    // Search pill: opens the full search screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.White.copy(alpha = 0.5f))
                            .clickable(onClick = onOpenSearch)
                            .testTag("inbox_search_button"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = DarkGreen.copy(alpha = 0.75f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search",
                                fontSize = 15.sp,
                                color = DarkGreen.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Inbox",
                        fontFamily = com.example.ui.theme.DisplayBlackFontFamily,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 44.sp,
                        letterSpacing = (-1.5).sp,
                        color = NearBlack,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clickable {
                                debugTapCount++
                                if (debugTapCount >= 3) {
                                    debugTapCount = 0
                                    viewModel.openDebugDialog()
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Contacts: an Add circle first (opens the Create sheet), then everyone you chat with
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        AddContactItem(onClick = onOpenCreate)
                        clients.forEach { client ->
                            QuickAvatarItem(
                                client = client,
                                onClick = { onOpenChat(client.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPillChip(
                            label = "All (${inboxItems.size})",
                            isSelected = selectedFilterIndex == 0,
                            onClick = { selectedFilterIndex = 0 }
                        )
                        FilterPillChip(
                            label = "Unread ($unreadCount)",
                            isSelected = selectedFilterIndex == 1,
                            onClick = { selectedFilterIndex = 1 }
                        )
                        FilterPillChip(
                            label = "Projects (${projectChats.size})",
                            isSelected = selectedFilterIndex == 2,
                            onClick = { selectedFilterIndex = 2 }
                        )
                    }
                }
            }

            // 4. Conversation Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 8.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        WiseCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No conversations yet",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add a client by username to start messaging",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.client.id }) { item ->
                        ExactConversationCard(
                            item = item,
                            onClick = { onOpenChat(item.client.id) },
                            onToggleStar = { viewModel.toggleStar(item.client.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddContactItem(onClick: () -> Unit) {
    val ring = DarkGreen.copy(alpha = 0.55f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).testTag("inbox_add_contact")
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .drawBehind {
                    drawCircle(
                        color = ring,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(9f, 9f))
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add contact",
                tint = DarkGreen,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NearBlack)
    }
}

/** A contact in the header strip: round initial, first name, nothing else. */
@Composable
fun QuickAvatarItem(
    client: Client,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = client.avatarInitial,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = client.firstName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NearBlack,
            maxLines = 1
        )
    }
}

/**
 * Filter Pill matching prototype:
 * Selected has black background `#0E0F0C` with white text.
 * Unselected has clean white background with dark near-black text.
 */
@Composable
fun FilterPillChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) NearBlack else Color.White
    val textCol = if (isSelected) Color.White else NearBlack

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textCol
        )
    }
}

/**
 * Individual Conversation Card matching the prototype PDF:
 * Card container with white background, rounded corners (18dp), subtle 1dp border,
 * Avatar (SJ in lime), Name + Timestamp, Status line with flag + City + Time + (Working)/(After hours),
 * Message snippet, and optional unread badge pill.
 */
@Composable
fun ExactConversationCard(
    item: MockDataProvider.InboxItem,
    onClick: () -> Unit,
    onToggleStar: () -> Unit = {}
) {
    val colors = GlobalChatTheme.colors
    val avatarBg = if (colors.isDark) Color(0xFF222620) else Color.White
    val unread = item.unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar; the lime dot is a real state: the contact is inside their working hours now
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(avatarBg)
                    .border(0.5.dp, colors.cardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.client.avatarInitial,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (colors.isDark) WiseLime else DarkGreen
                )
            }
            if (item.client.statusStr == "Working") {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .background(colors.screenBackground, CircleShape)
                        .padding(2.dp)
                        .background(WiseLime, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.client.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.timestamp,
                    fontSize = 12.sp,
                    fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (unread) (if (colors.isDark) WiseLime else DarkGreen) else colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.lastMessage,
                    fontSize = 14.sp,
                    fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (unread) colors.textPrimary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (unread) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                            .clip(CircleShape)
                            .background(WiseLime)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${item.unreadCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                    }
                }
            }
        }
    }
}
