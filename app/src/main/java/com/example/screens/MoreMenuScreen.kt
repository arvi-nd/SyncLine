package com.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WiseCard
import com.example.ui.theme.GlobalChatTheme
import com.example.viewmodel.GlobalChatViewModel

@Composable
fun MoreMenuScreen(
    viewModel: GlobalChatViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "More",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            WiseCard(
                cornerRadius = 22.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MoreMenuItemRow(
                        icon = Icons.Outlined.Public,
                        title = "Global Timezones",
                        subtitle = "World clocks & overlap calculation",
                        onClick = { onNavigate("timezones") },
                        testTag = "more_item_timezones"
                    )

                    MoreMenuItemRow(
                        icon = Icons.Outlined.CalendarToday,
                        title = "Calendar & Meetings",
                        subtitle = "Schedule & client meetings",
                        onClick = { onNavigate("calendar") },
                        testTag = "more_item_calendar"
                    )

                    MoreMenuItemRow(
                        icon = Icons.Outlined.Description,
                        title = "Files & Attachments",
                        subtitle = "Project briefs & design deliverables",
                        onClick = { onNavigate("files") },
                        testTag = "more_item_files"
                    )

                    MoreMenuItemRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        subtitle = "Activity feed & client alerts",
                        onClick = { onNavigate("notifications") },
                        testTag = "more_item_notifications"
                    )

                    MoreMenuItemRow(
                        icon = Icons.Outlined.SmartToy,
                        title = "AI Assistant",
                        subtitle = "Smart summary & quick actions",
                        onClick = { onNavigate("assistant") },
                        testTag = "more_item_assistant"
                    )

                    MoreMenuItemRow(
                        icon = Icons.Outlined.HourglassEmpty,
                        title = "Empty States Preview",
                        subtitle = "Zero-state UI concepts",
                        onClick = { onNavigate("empty_state") },
                        testTag = "more_item_empty_state"
                    )

                    MoreMenuItemRow(
                        icon = Icons.Outlined.Settings,
                        title = "Settings",
                        subtitle = "Profile, themes & preferences",
                        onClick = { onNavigate("settings") },
                        testTag = "more_item_settings"
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MoreMenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String = "more_menu_row"
) {
    val colors = GlobalChatTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
