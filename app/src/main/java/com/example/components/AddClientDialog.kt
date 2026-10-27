package com.example.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.WiseLime

@Composable
fun AddClientDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirmAdd: (username: String, onError: (String) -> Unit, onSuccess: (clientUid: String) -> Unit) -> Unit
) {
    if (!isOpen) return

    val colors = GlobalChatTheme.colors
    var usernameInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.cardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("add_client_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.cardBorder.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Add Client",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                    }

                    IconButton(
                        onClick = { if (!isLoading) onDismiss() },
                        enabled = !isLoading,
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

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Enter the username of the user you want to add to SyncLine.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username input field
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        usernameInput = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = { Text("e.g. sarah_j", color = colors.textSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PersonPin,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WiseLime,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.screenBackground,
                        unfocusedContainerColor = colors.screenBackground,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_client_username_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.5.sp,
                        color = Color(0xFFD03238),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WisePillButton(
                        text = "Cancel",
                        onClick = { if (!isLoading) onDismiss() },
                        isPrimary = false,
                        modifier = Modifier.weight(1f),
                        testTag = "add_client_cancel_btn"
                    )

                    WisePillButton(
                        text = if (isLoading) "Looking up..." else "Add Client",
                        onClick = {
                            if (!isLoading) {
                                val cleanUsername = usernameInput.trim().lowercase()
                                if (cleanUsername.isBlank()) {
                                    errorMessage = "Please enter a username."
                                } else if (cleanUsername.length < 3) {
                                    errorMessage = "Username must be at least 3 characters."
                                } else if (!cleanUsername.matches(Regex("^[a-zA-Z0-9_.]+$"))) {
                                    errorMessage = "Username can only contain letters, numbers, underscores, and dots."
                                } else {
                                    isLoading = true
                                    errorMessage = null
                                    onConfirmAdd(
                                        cleanUsername,
                                        { err ->
                                            isLoading = false
                                            errorMessage = err
                                        },
                                        { clientUid ->
                                            isLoading = false
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        },
                        isPrimary = true,
                        modifier = Modifier.weight(1f),
                        testTag = "add_client_submit_btn"
                    )
                }
            }
        }
    }
}
