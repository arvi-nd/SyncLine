package com.example.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WisePillButton
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.LightMint
import com.example.ui.theme.NearBlack
import com.example.ui.theme.WiseLime

@Composable
fun EmptyStateScreen(
    onNewMessage: () -> Unit,
    onBack: (() -> Unit)? = null,
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("empty_state_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                }
            }

            // Center: Vector Illustration and Copy
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Vector speech bubbles illustration matching Screen 17
                EmptyChatIllustration(
                    modifier = Modifier.size(140.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "No messages yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Start a conversation with your\nclient to get things moving.",
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                WisePillButton(
                    text = "New message",
                    onClick = onNewMessage,
                    isPrimary = true,
                    modifier = Modifier.width(200.dp),
                    testTag = "empty_state_new_message_btn"
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun EmptyChatIllustration(modifier: Modifier = Modifier) {
    val colors = GlobalChatTheme.colors
    val bubbleColor = if (colors.isDark) Color(0xFF1B2E15) else LightMint
    val dotColor = if (colors.isDark) WiseLime else Color(0xFF163300)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Top-right small speech bubble
        drawRoundRect(
            color = bubbleColor,
            topLeft = Offset(w * 0.35f, h * 0.15f),
            size = Size(w * 0.55f, h * 0.4f),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        // Three dots in top bubble
        val dotRadius = 3.dp.toPx()
        drawCircle(color = dotColor, radius = dotRadius, center = Offset(w * 0.5f, h * 0.35f))
        drawCircle(color = dotColor, radius = dotRadius, center = Offset(w * 0.62f, h * 0.35f))
        drawCircle(color = dotColor, radius = dotRadius, center = Offset(w * 0.74f, h * 0.35f))

        // Bottom-left overlapping bubble
        drawRoundRect(
            color = bubbleColor.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.1f, h * 0.45f),
            size = Size(w * 0.55f, h * 0.4f),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        // Line accent
        drawLine(
            color = dotColor.copy(alpha = 0.6f),
            start = Offset(w * 0.22f, h * 0.65f),
            end = Offset(w * 0.5f, h * 0.65f),
            strokeWidth = 3.dp.toPx()
        )
    }
}
