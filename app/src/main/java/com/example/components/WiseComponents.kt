package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectStatus
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.LightMint
import com.example.ui.theme.NearBlack
import com.example.ui.theme.SentimentPositive
import com.example.ui.theme.WiseLime

@Composable
fun WisePillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "pill_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "btn_scale"
    )

    val colors = GlobalChatTheme.colors
    val backgroundColor = when {
        !enabled -> colors.textSecondary.copy(alpha = 0.2f)
        isPrimary -> colors.brandLime
        else -> if (colors.isDark) Color(0xFF222820) else Color(0x14163300)
    }

    val contentColor = when {
        !enabled -> colors.textSecondary.copy(alpha = 0.5f)
        isPrimary -> colors.darkGreen
        else -> colors.textPrimary
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(9999.dp),
        color = backgroundColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WiseCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = GlobalChatTheme.colors.cardBackground,
    borderColor: Color = GlobalChatTheme.colors.cardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
    } else {
        modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    }

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun WiseAvatar(
    name: String,
    initial: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    countryFlag: String? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.dp, Color(0x1F0E0F0C), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp,
                color = NearBlack
            )
        }

        // Online indicator dot
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(WiseLime)
                    .border(2.dp, GlobalChatTheme.colors.cardBackground, CircleShape)
            )
        }

        // Optional flag pip
        countryFlag?.let { flag ->
            if (!isOnline) {
                Text(
                    text = flag,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(GlobalChatTheme.colors.cardBackground, CircleShape)
                        .padding(1.dp)
                )
            }
        }
    }
}

@Composable
fun WiseStatusChip(
    status: ProjectStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        ProjectStatus.IN_PROGRESS -> LightMint to DarkGreen
        ProjectStatus.PLANNING -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        ProjectStatus.COMPLETED -> Color(0xFFDCFCE7) to SentimentPositive
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun WiseFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    testTag: String = "filter_chip"
) {
    val colors = GlobalChatTheme.colors
    val backgroundColor = if (isSelected) {
        if (colors.isDark) WiseLime else NearBlack
    } else {
        if (colors.isDark) Color(0xFF1E221C) else Color(0xFFEDF0E8)
    }

    val textColor = if (isSelected) {
        if (colors.isDark) DarkGreen else Color.White
    } else {
        colors.textSecondary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            count?.let {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$it",
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WiseTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (colors.isDark) Color(0xFF1E221C) else Color(0x0F0E0F0C))
                    .testTag("top_bar_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        actions?.invoke()
    }
}

/**
 * Minimalist wireframe globe vector drawing matching Screen 1 (Splash/Launch).
 */
@Composable
fun GlobeWireframe(
    modifier: Modifier = Modifier,
    lineColor: Color = NearBlack
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val stroke = Stroke(width = 2.dp.toPx())

        // Outer circle
        drawCircle(
            color = lineColor,
            radius = radius - stroke.width,
            center = center,
            style = stroke
        )

        // Equator
        drawLine(
            color = lineColor,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = stroke.width
        )

        // Latitude circles (ellipses)
        val latR1 = radius * 0.5f
        drawOval(
            color = lineColor,
            topLeft = Offset(center.x - radius * 0.866f, center.y - latR1),
            size = androidx.compose.ui.geometry.Size(radius * 1.732f, latR1 * 2),
            style = stroke
        )

        // Prime meridian and longitude ellipses
        drawLine(
            color = lineColor,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = stroke.width
        )

        drawOval(
            color = lineColor,
            topLeft = Offset(center.x - radius * 0.55f, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 1.1f, radius * 2),
            style = stroke
        )
    }
}
