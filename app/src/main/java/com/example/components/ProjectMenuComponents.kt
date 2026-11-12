package com.example.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.WiseLime
import kotlinx.coroutines.launch

/**
 * Apple-style liquid morphing 3-dots menu.
 * Morphs the 3-dots circle in place into a small menu with a lively spring bounce effect,
 * adhering to the Wise design system (rounded corners, dark green / lime / danger accents, and bold Inter typography).
 *
 * Uses a single continuous Surface to eliminate any separate window sliding or duplicate buttons.
 */
@Composable
fun ProjectOptionsMorphMenu(
    projectTitle: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonTag: String = "project_details_menu_btn",
    menuTag: String = "project_menu_delete_item"
) {
    val colors = GlobalChatTheme.colors
    val coroutineScope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(false) }
    // 0f = 38dp circle, 1f = expanded menu (with spring bounce overshoot)
    val morphProgress = remember { Animatable(0f) }

    val closeMenu: () -> Unit = {
        isExpanded = false
        coroutineScope.launch {
            morphProgress.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = 420f
                )
            )
        }
    }

    val openMenu: () -> Unit = {
        isExpanded = true
        coroutineScope.launch {
            morphProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.58f, // Apple bouncy liquid damping
                    stiffness = 360f      // Responsive spring physics
                )
            )
        }
    }

    BackHandler(enabled = isExpanded) {
        closeMenu()
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (isPressed && !isExpanded) 0.88f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "dots_btn_scale"
    )

    val progress = morphProgress.value
    val cardWidth = lerp(start = 38.dp, stop = 212.dp, fraction = progress)
    val cardHeight = lerp(start = 38.dp, stop = 124.dp, fraction = progress)
    val cornerRadius = lerp(start = 19.dp, stop = 22.dp, fraction = progress.coerceIn(0f, 1f))
    val contentAlpha = ((progress - 0.35f) / 0.65f).coerceIn(0f, 1f)
    val dotsAlpha = (1f - (progress / 0.35f)).coerceIn(0f, 1f)
    val shadowElevation = (progress.coerceIn(0f, 1f) * 8f).dp
    val isMenuInteractive = progress > 0.5f

    Box(
        modifier = modifier.size(38.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        // Transparent fullscreen scrim behind the morphing menu to dismiss on outside click
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .requiredSize(3000.dp)
                    .offset(x = 1500.dp - 38.dp, y = -1000.dp)
                    .zIndex(10f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            closeMenu()
                        }
                    }
            )
        }

        // Single continuous Surface that morphs in-place from circle to menu
        Surface(
            onClick = {
                if (!isExpanded) {
                    openMenu()
                }
            },
            enabled = !isMenuInteractive,
            shape = RoundedCornerShape(cornerRadius),
            color = if (progress < 0.15f) {
                if (colors.isDark) Color(0xFF222820) else Color(0xFFEFF3EA)
            } else {
                colors.cardBackground
            },
            border = BorderStroke(
                1.dp,
                if (progress > 0.2f) {
                    if (colors.isDark) Color(0xFF333B2E) else colors.cardBorder
                } else {
                    colors.cardBorder.copy(alpha = 0.7f)
                }
            ),
            shadowElevation = shadowElevation,
            interactionSource = if (!isMenuInteractive) interactionSource else remember { MutableInteractionSource() },
            modifier = Modifier
                .scale(if (!isExpanded) btnScale else 1f)
                .requiredSize(width = cardWidth, height = cardHeight)
                .zIndex(20f)
                .testTag(buttonTag)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                // Morphing 3 dots icon during initial expand / collapse
                if (dotsAlpha > 0.02f) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .alpha(dotsAlpha),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Project Options",
                            tint = colors.textPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer(rotationZ = progress * 90f)
                        )
                    }
                }

                // Expanded liquid menu content
                if (contentAlpha > 0.02f) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(contentAlpha)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header: Label & Close icon button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(WiseLime)
                                )
                                Text(
                                    text = "OPTIONS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    color = colors.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (colors.isDark) Color(0xFF262C22) else Color(0xFFEFF2EB))
                                    .clickable(onClick = closeMenu),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Delete Project Action Tile with spring scale press
                        val itemInteraction = remember { MutableInteractionSource() }
                        val itemPressed by itemInteraction.collectIsPressedAsState()
                        val itemScale by animateFloatAsState(
                            targetValue = if (itemPressed) 0.94f else 1f,
                            animationSpec = spring(stiffness = 500f),
                            label = "delete_btn_scale"
                        )

                        Surface(
                            onClick = {
                                closeMenu()
                                onDeleteClick()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (colors.isDark) Color(0xFF2C1618) else Color(0xFFFFF0F2),
                            border = BorderStroke(
                                1.dp,
                                if (colors.isDark) Color(0xFF5A2427) else Color(0xFFF9C8CB)
                            ),
                            interactionSource = itemInteraction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(itemScale)
                                .testTag(menuTag)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (colors.isDark) Color(0xFF421C1F) else Color(0xFFFCD3D6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFD03238),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Delete Project",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD03238)
                                    )
                                    Text(
                                        text = "Remove permanently",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFD03238).copy(alpha = 0.75f)
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

/**
 * Beautiful Wise-inspired Morphing Confirmation Dialog for deleting a project.
 */
@Composable
fun DeleteProjectConfirmationDialog(
    projectTitle: String,
    isDeleting: Boolean,
    errorMessage: String? = null,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
    confirmTag: String = "confirm_delete_project_btn",
    cancelTag: String = "cancel_delete_project_btn"
) {
    val colors = GlobalChatTheme.colors

    Dialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = colors.cardBackground,
                border = BorderStroke(1.dp, colors.cardBorder),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_project_confirmation_modal")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag pill indicator
                    Box(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
                    )

                    // Soft red squircle danger icon container
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (colors.isDark) Color(0xFF35191C) else Color(0xFFFDE8E9))
                            .border(
                                width = 1.dp,
                                color = if (colors.isDark) Color(0xFF5A2427) else Color(0xFFF7C3C5),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFD03238),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Delete project?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Are you sure you want to delete \"$projectTitle\"? This will permanently delete this project and all its tasks from Firestore. Other projects will not be affected.",
                        fontSize = 13.5.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD03238),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pill action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel pill button
                        val cancelInteraction = remember { MutableInteractionSource() }
                        val cancelPressed by cancelInteraction.collectIsPressedAsState()
                        val cancelScale by animateFloatAsState(
                            targetValue = if (cancelPressed) 0.95f else 1f,
                            animationSpec = spring(stiffness = 500f),
                            label = "cancel_scale"
                        )

                        Surface(
                            onClick = onDismiss,
                            enabled = !isDeleting,
                            shape = RoundedCornerShape(9999.dp),
                            color = if (colors.isDark) Color(0xFF222620) else Color(0xFFE8EBE3),
                            border = BorderStroke(
                                1.dp,
                                if (colors.isDark) Color(0xFF333830) else Color(0xFFDFE3DA)
                            ),
                            interactionSource = cancelInteraction,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .scale(cancelScale)
                                .testTag(cancelTag)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        // Delete pill button
                        val deleteInteraction = remember { MutableInteractionSource() }
                        val deletePressed by deleteInteraction.collectIsPressedAsState()
                        val deleteScale by animateFloatAsState(
                            targetValue = if (deletePressed) 0.95f else 1f,
                            animationSpec = spring(stiffness = 500f),
                            label = "delete_scale"
                        )

                        Surface(
                            onClick = onConfirmDelete,
                            enabled = !isDeleting,
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFD03238),
                            interactionSource = deleteInteraction,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp)
                                .scale(deleteScale)
                                .testTag(confirmTag)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isDeleting) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Deleting...",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Delete",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
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
