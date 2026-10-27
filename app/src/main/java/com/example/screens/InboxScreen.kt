package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
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

    var debugTapCount by remember { mutableIntStateOf(0) }
    var selectedFilterTab by remember { mutableStateOf("All (12)") }

    val filteredList = when (selectedFilterTab) {
        "Unread (3)" -> inboxItems.filter { it.unreadCount > 0 }
        "Projects (5)" -> inboxItems.take(2)
        else -> inboxItems
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Rounded Container: Wise Lime rounded rectangle covering from top down through filter pills
            val topHeaderBg = WiseLime
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = topHeaderBg,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 20.dp)
                ) {
                    // Header Row: "Inbox." + Search Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Inbox.",
                            fontFamily = com.example.ui.theme.DisplayBlackFontFamily,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 38.sp,
                            letterSpacing = (-1.5).sp,
                            color = NearBlack,
                            modifier = Modifier.clickable {
                                debugTapCount++
                                if (debugTapCount >= 3) {
                                    debugTapCount = 0
                                    viewModel.openDebugDialog()
                                }
                            }
                        )

                        // Circular Search Button with translucent white circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f))
                                .clickable(onClick = onOpenSearch)
                                .testTag("inbox_search_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = NearBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Horizontal Quick Client Avatars Row (Sarah, Kenji, Elena, David)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        clients.take(4).forEach { client ->
                            QuickAvatarItem(
                                client = client,
                                onClick = { onOpenChat(client.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Filter Chips Row: All (12), Unread (3), Projects (5), Timezones
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPillChip(
                            label = "All (12)",
                            isSelected = selectedFilterTab == "All (12)",
                            onClick = { selectedFilterTab = "All (12)" }
                        )

                        FilterPillChip(
                            label = "Unread (3)",
                            isSelected = selectedFilterTab == "Unread (3)",
                            onClick = { selectedFilterTab = "Unread (3)" }
                        )

                        FilterPillChip(
                            label = "Projects (5)",
                            isSelected = selectedFilterTab == "Projects (5)",
                            onClick = { selectedFilterTab = "Projects (5)" }
                        )

                        FilterPillChip(
                            label = "Timezones",
                            isSelected = selectedFilterTab == "Timezones",
                            onClick = { selectedFilterTab = "Timezones" }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 4. Conversation Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 12.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.client.id }) { item ->
                    ExactConversationCard(
                        item = item,
                        onClick = { onOpenChat(item.client.id) }
                    )
                }
            }
        }
    }
}

/**
 * Top Avatar item matching the prototype:
 * Circular avatar (SJ in soft dark green tint, Others in clean White circles), followed by Name, followed by Local Time.
 */
@Composable
fun QuickAvatarItem(
    client: Client,
    onClick: () -> Unit
) {
    val isLimeAvatar = client.avatarInitial == "SJ"
    val avatarBg = if (isLimeAvatar) Color(0x28163300) else Color.White
    val avatarTextColor = if (isLimeAvatar) DarkGreen else NearBlack

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = client.avatarInitial,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = avatarTextColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = client.firstName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NearBlack
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = client.localTimeStr,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C3C24)
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
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
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
    onClick: () -> Unit
) {
    val colors = GlobalChatTheme.colors
    val isLimeAvatar = item.client.avatarInitial == "SJ"
    val avatarBg = if (isLimeAvatar) WiseLime else (if (colors.isDark) Color(0xFF222820) else Color(0xFFF0F3EC))
    val avatarTextColor = if (isLimeAvatar) DarkGreen else colors.textPrimary

    WiseCard(
        cornerRadius = 20.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(avatarBg)
                    .then(
                        if (!isLimeAvatar) Modifier.border(
                            1.dp,
                            if (colors.isDark) Color(0x33FFFFFF) else Color(0xFFDFE3DA),
                            CircleShape
                        ) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.client.avatarInitial,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = avatarTextColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle Column: Name, Status & Timezone, Last Message
            Column(modifier = Modifier.weight(1f)) {
                // Top row: Name & Time Ago
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.client.name,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )

                    Text(
                        text = item.timestamp,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Middle line: Flag + City · Local Time (Working)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.client.flag,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.client.city} · ${item.client.localTimeStr} ",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "(${item.client.statusStr})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.client.statusStr == "Working") {
                            if (colors.isDark) WiseLime else SentimentPositive
                        } else {
                            colors.textSecondary
                        }
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Bottom line: Message snippet
                Text(
                    text = item.lastMessage,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right unread badge if unread
            if (item.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(WiseLime),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${item.unreadCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                }
            }
        }
    }
}
