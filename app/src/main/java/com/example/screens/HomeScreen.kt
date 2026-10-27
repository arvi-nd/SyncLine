package com.example.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WiseCard
import com.example.model.Client
import com.example.model.Meeting
import com.example.model.Project
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DisplayBlackFontFamily
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.NearBlack
import com.example.ui.theme.PastelGreen
import com.example.ui.theme.SentimentPositive
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel

@Composable
fun HomeScreen(
    viewModel: GlobalChatViewModel,
    onOpenChat: (String) -> Unit,
    onViewAllChats: () -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val inboxItems by viewModel.inboxItems.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val meetings by viewModel.meetings.collectAsState()
    val cities by viewModel.cities.collectAsState()

    val unreadList = inboxItems.filter { it.unreadCount > 0 }
    val displayChats = if (unreadList.isNotEmpty()) unreadList else inboxItems.take(3)
    val totalUnreadCount = inboxItems.sumOf { it.unreadCount }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 1. Top Header: Date, Big "Overview.", Search & Notification icons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY · WED, MAY 28",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = colors.textSecondary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenSearch,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("home_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("home_notifications_button")
                            ) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = "Notifications",
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    // Notification dot
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .align(Alignment.TopEnd)
                                            .background(WiseLime, CircleShape)
                                            .border(1.dp, colors.screenBackground, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Overview.",
                        fontFamily = DisplayBlackFontFamily,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        letterSpacing = (-1.2).sp
                    )
                }
            }

            // 2. Metric Highlight Pills Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        text = if (totalUnreadCount > 0) "$totalUnreadCount unread" else "All caught up",
                        isHighlight = totalUnreadCount > 0,
                        onClick = onViewAllChats
                    )
                    MetricChip(
                        text = "${projects.count { it.status != com.example.model.ProjectStatus.COMPLETED }} active projects",
                        isHighlight = false,
                        onClick = {
                            if (projects.isNotEmpty()) onOpenProject(projects.first().id)
                        }
                    )
                    MetricChip(
                        text = "${meetings.size} meetings today",
                        isHighlight = false,
                        onClick = onOpenCalendar
                    )
                }
            }

            // 3. Section: Unread Chats Overview
            item {
                Spacer(modifier = Modifier.height(20.dp))
                HomeSectionHeader(
                    title = "Unread chats",
                    count = totalUnreadCount,
                    actionLabel = "See all",
                    onActionClick = onViewAllChats,
                    testTag = "home_see_all_chats"
                )
            }

            if (displayChats.isEmpty()) {
                item {
                    WiseCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SentimentPositive,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "You have caught up with all conversations!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            } else {
                items(displayChats) { item ->
                    HomeChatCard(
                        client = item.client,
                        lastMessage = item.lastMessage,
                        timestamp = item.timestamp,
                        statusText = item.statusText,
                        unreadCount = item.unreadCount,
                        onClick = { onOpenChat(item.client.id) },
                        testTag = "home_chat_item_${item.client.id}"
                    )
                }
            }

            // 4. Section: Projects Progress Overview
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HomeSectionHeader(
                    title = "Projects progress",
                    count = projects.size,
                    actionLabel = "View all",
                    onActionClick = {
                        if (projects.isNotEmpty()) onOpenProject(projects.first().id)
                    },
                    testTag = "home_see_all_projects"
                )
            }

            items(projects.take(3)) { project ->
                HomeProjectCard(
                    project = project,
                    onClick = { onOpenProject(project.id) },
                    testTag = "home_project_card_${project.id}"
                )
            }

            // 5. Section: Upcoming Meetings
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HomeSectionHeader(
                    title = "Upcoming meetings",
                    count = meetings.size,
                    actionLabel = "Calendar",
                    onActionClick = onOpenCalendar,
                    testTag = "home_see_all_calendar"
                )
            }

            items(meetings) { meeting ->
                HomeMeetingCard(
                    meeting = meeting,
                    onClick = onOpenCalendar,
                    testTag = "home_meeting_card_${meeting.id}"
                )
            }

            // 6. Section: Global Timezones Glance
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Global timezones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time client availability worldwide",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    cities.forEach { city ->
                        TimezonePill(city = city)
                    }
                }
            }

            // Bottom space for floating bottom navigation bar
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}

@Composable
private fun MetricChip(
    text: String,
    isHighlight: Boolean,
    onClick: () -> Unit
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "metric_scale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9999.dp),
        color = if (isHighlight) WiseLime else (if (colors.isDark) Color(0xFF242721) else Color(0xFFE8EBE3)),
        border = if (isHighlight) null else androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isHighlight) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(DarkGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isHighlight) DarkGreen else colors.textPrimary
            )
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    count: Int? = null,
    actionLabel: String,
    onActionClick: () -> Unit,
    testTag: String
) {
    val colors = GlobalChatTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            if (count != null && count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (colors.isDark) Color(0xFF2A2E26) else Color(0xFFE2E6DD),
                            RoundedCornerShape(9999.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(9999.dp))
                .clickable { onActionClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (colors.isDark) WiseLime else DarkGreen
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (colors.isDark) WiseLime else DarkGreen,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun HomeChatCard(
    client: Client,
    lastMessage: String,
    timestamp: String,
    statusText: String,
    unreadCount: Int,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "chat_card_scale"
    )

    WiseCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .scale(scale)
            .testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(client.avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = client.avatarInitial,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NearBlack
                )

                if (client.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFF054D28), CircleShape)
                            .border(2.dp, colors.cardBackground, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = client.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Text(
                        text = timestamp,
                        fontSize = 12.sp,
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (unreadCount > 0) (if (colors.isDark) WiseLime else DarkGreen) else colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = lastMessage,
                    fontSize = 13.5.sp,
                    fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (unreadCount > 0) colors.textPrimary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusText,
                        fontSize = 11.5.sp,
                        color = colors.textSecondary
                    )

                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .background(WiseLime, RoundedCornerShape(9999.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$unreadCount new",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeProjectCard(
    project: Project,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "project_card_scale"
    )

    WiseCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .scale(scale)
            .testTag(testTag),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${project.clientName} · ${project.dueDate}",
                        fontSize = 12.5.sp,
                        color = colors.textSecondary
                    )
                }

                // Status Tag
                val statusBg = when (project.status) {
                    com.example.model.ProjectStatus.COMPLETED -> Color(0xFFE2F6D5)
                    com.example.model.ProjectStatus.IN_PROGRESS -> WiseLime.copy(alpha = 0.35f)
                    com.example.model.ProjectStatus.PLANNING -> Color(0xFFFFE0B2)
                }
                val statusTextCol = when (project.status) {
                    com.example.model.ProjectStatus.COMPLETED -> DarkGreen
                    com.example.model.ProjectStatus.IN_PROGRESS -> DarkGreen
                    com.example.model.ProjectStatus.PLANNING -> Color(0xFF8A4A00)
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(9999.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = project.status.label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextCol
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar & %
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (colors.isDark) Color(0xFF282C24) else Color(0xFFE5E9DF))
                ) {
                    val progressFraction = (project.progress / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(8.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(WiseLime)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "${project.progress}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            if (project.tasks.isNotEmpty()) {
                val completed = project.tasks.count { it.isCompleted }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$completed of ${project.tasks.size} tasks finished",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun HomeMeetingCard(
    meeting: Meeting,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "meeting_card_scale"
    )

    WiseCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .scale(scale)
            .testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (colors.isDark) Color(0xFF242721) else Color(0xFFF0F4EC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (colors.isDark) WiseLime else DarkGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meeting.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${meeting.time} · ${meeting.duration} · Google Meet",
                    fontSize = 12.5.sp,
                    color = colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .background(WiseLime, RoundedCornerShape(9999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = "Join",
                        tint = DarkGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Join",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun TimezonePill(
    city: com.example.model.TimezoneCity
) {
    val colors = GlobalChatTheme.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = city.flag, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = city.cityName,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val time = when (city.cityName) {
                "London" -> "12:45 PM"
                "Berlin" -> "1:45 PM"
                "Tokyo" -> "8:45 PM"
                "New York" -> "4:45 AM"
                "New Delhi" -> "5:15 PM"
                else -> "12:00 PM"
            }
            Text(
                text = time,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (city.cityName in listOf("London", "Berlin", "New Delhi")) SentimentPositive else colors.textSecondary
            )
        }
    }
}
