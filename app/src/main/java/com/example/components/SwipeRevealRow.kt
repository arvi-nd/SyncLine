package com.example.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** Strength of the small overshoot when a circle pops in (0 = none). */
private const val POP_BACK = 2.6f

data class SwipeAction(
    val icon: ImageVector,
    val label: String,
    val container: Color,
    val tint: Color,
    val tag: String,
    val onClick: () -> Unit
)

/**
 * Lets the content slide sideways to reveal round action buttons behind it.
 *
 * Content from the start side (incoming bubbles) is dragged right; content on the end side
 * (own bubbles, already at the right edge) is dragged left so it has room to move.
 * The circles pop in one after another as the content slides, and everything settles with a
 * tight spring so it feels snappy with almost no bounce.
 */
@Composable
fun SwipeRevealRow(
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    actions: List<SwipeAction>,
    fromStart: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable () -> Unit
) {
    if (!enabled || actions.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = contentAlignment) { content() }
        return
    }

    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val circleSize = 32.dp
    val gap = 8.dp
    val edgePad = 8.dp
    val count = actions.size
    val revealPx = with(density) { (edgePad + (circleSize + gap) * count).toPx() }
    val sign = if (fromStart) 1f else -1f
    val offset = remember { Animatable(0f) }
    val bounce = remember { spring<Float>(dampingRatio = 0.86f, stiffness = 1100f) }

    // Closing or opening from outside (another row opened, tapped elsewhere, scrolled).
    LaunchedEffect(isOpen, revealPx) {
        val target = if (isOpen) revealPx else 0f
        if (offset.value != target) offset.animateTo(target, bounce)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Action circles sit behind the content and pop in as it slides away.
        Row(
            modifier = Modifier
                .align(if (fromStart) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = edgePad),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val ordered = if (fromStart) actions else actions.reversed()
            ordered.forEachIndexed { visualIndex, action ->
                val fromEdge = if (fromStart) visualIndex else count - 1 - visualIndex
                val progress = if (revealPx == 0f) 0f else offset.value / revealPx
                val start = fromEdge * 0.3f / count
                val pop = ((progress - start) / 0.45f).coerceIn(0f, 1f)
                // Ease-out-back on the slide progress: each circle overshoots a little as it pops in.
                val t = pop - 1f
                val popScale = if (pop <= 0f) 0f else 1f + (POP_BACK + 1f) * t * t * t + POP_BACK * t * t
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .graphicsLayer {
                            scaleX = popScale
                            scaleY = popScale
                            alpha = pop
                        }
                        .clip(CircleShape)
                        .background(action.container)
                        .border(1.dp, action.tint.copy(alpha = 0.22f), CircleShape)
                        .clickable(enabled = isOpen && pop > 0.7f) {
                            action.onClick()
                            onOpenChange(false)
                        }
                        .testTag(action.tag),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = action.tint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // The message itself.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset((offset.value * sign).roundToInt(), 0) }
                .pointerInput(revealPx, fromStart) {
                    var lastDx = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { lastDx = 0f },
                        onDragEnd = {
                            val towardsOpen = lastDx * sign
                            val open = when {
                                towardsOpen > 2f -> offset.value > revealPx * 0.25f
                                towardsOpen < -2f -> false
                                else -> offset.value > revealPx * 0.5f
                            }
                            if (open && !isOpen) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenChange(open)
                            scope.launch { offset.animateTo(if (open) revealPx else 0f, bounce) }
                        },
                        onDragCancel = {
                            onOpenChange(false)
                            scope.launch { offset.animateTo(0f, bounce) }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            lastDx = dragAmount
                            val current = offset.value
                            val resistance = if (current > revealPx) 0.2f else 1f
                            val next = (current + dragAmount * sign * resistance).coerceIn(0f, revealPx * 1.1f)
                            scope.launch { offset.snapTo(next) }
                        }
                    )
                },
            contentAlignment = contentAlignment
        ) {
            content()
            if (isOpen) {
                // While open, a tap anywhere on the message closes it instead of triggering the bubble.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onOpenChange(false) }
                )
            }
        }
    }
}
