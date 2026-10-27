package com.example.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlobalChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBottomSheet(
    onDismiss: () -> Unit,
    onNewChat: () -> Unit,
    onAddClient: () -> Unit = {},
    onNewProject: () -> Unit,
    onScheduleMessage: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = GlobalChatTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.cardBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.cardBorder.copy(alpha = 0.2f))
                        .testTag("create_sheet_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Items
            CreateActionItem(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "New chat",
                onClick = {
                    onDismiss()
                    onNewChat()
                },
                testTag = "create_action_new_chat"
            )

            Spacer(modifier = Modifier.height(10.dp))

            CreateActionItem(
                icon = Icons.Outlined.PersonAdd,
                title = "Add client",
                onClick = {
                    onDismiss()
                    onAddClient()
                },
                testTag = "create_action_add_client"
            )

            Spacer(modifier = Modifier.height(10.dp))

            CreateActionItem(
                icon = Icons.Outlined.Folder,
                title = "New project",
                onClick = {
                    onDismiss()
                    onNewProject()
                },
                testTag = "create_action_new_project"
            )

            Spacer(modifier = Modifier.height(10.dp))

            CreateActionItem(
                icon = Icons.Outlined.Schedule,
                title = "Schedule message",
                onClick = {
                    onDismiss()
                    onScheduleMessage()
                },
                testTag = "create_action_schedule_message"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Cancel Button
            WisePillButton(
                text = "Cancel",
                onClick = onDismiss,
                isPrimary = false,
                modifier = Modifier.fillMaxWidth(),
                testTag = "create_action_cancel"
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CreateActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = GlobalChatTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "create_action_press_scale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (colors.isDark) androidx.compose.ui.graphics.Color(0xFF222620) else androidx.compose.ui.graphics.Color(0xFFF7F8F4),
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.cardBorder.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }
    }
}
