package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.WiseLime

data class ShowcaseScreenItem(
    val number: Int,
    val name: String,
    val route: String
)

val showcaseScreens = listOf(
    ShowcaseScreenItem(1, "Splash / Launch Screen", "splash"),
    ShowcaseScreenItem(2, "Login / Sign Up", "login"),
    ShowcaseScreenItem(3, "Home / Inbox", "inbox"),
    ShowcaseScreenItem(4, "Chat Screen", "chat/c1"),
    ShowcaseScreenItem(5, "Client Details / Timezone", "client_details/c1"),
    ShowcaseScreenItem(6, "Global Timezones", "timezones"),
    ShowcaseScreenItem(7, "Contacts", "contacts"),
    ShowcaseScreenItem(8, "Projects", "projects"),
    ShowcaseScreenItem(9, "Project Details", "project_details/p2"),
    ShowcaseScreenItem(10, "Files & Attachments", "files"),
    ShowcaseScreenItem(11, "Schedule Message", "schedule_message/c1"),
    ShowcaseScreenItem(12, "Calendar / Meetings", "calendar"),
    ShowcaseScreenItem(13, "Settings", "settings"),
    ShowcaseScreenItem(14, "Search", "search"),
    ShowcaseScreenItem(15, "Notifications", "notifications"),
    ShowcaseScreenItem(16, "Messages / AI Assistant (simulated)", "assistant"),
    ShowcaseScreenItem(17, "Empty States", "empty_state"),
    ShowcaseScreenItem(18, "Mobile Navigation / Bottom Bar", "inbox")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseDebugDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelectScreen: (String) -> Unit
) {
    if (!isOpen) return

    val colors = GlobalChatTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.cardBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("showcase_debug_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Showcase Inspection",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Jump to any of the 18 showcase screens",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.cardBorder.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(showcaseScreens) { item ->
                        Surface(
                            onClick = {
                                onSelectScreen(item.route)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (colors.isDark) Color(0xFF222620) else Color(0xFFF7F8F4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(WiseLime),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.number}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkGreen
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
