package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WiseAvatar
import com.example.components.WiseFilterChip
import com.example.ui.theme.GlobalChatTheme
import com.example.viewmodel.GlobalChatViewModel

enum class SearchCategory { MESSAGES, CONTACTS, PROJECTS, FILES }

@Composable
fun SearchScreen(
    viewModel: GlobalChatViewModel,
    onBack: () -> Unit,
    onOpenResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var searchQuery by remember { mutableStateOf("design") }
    var selectedCategory by remember { mutableStateOf<SearchCategory?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Search Bar + Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...", color = colors.textSecondary) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.brandLime,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.cardBackground,
                        unfocusedContainerColor = colors.cardBackground,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input_field")
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Cancel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .testTag("search_cancel_btn")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WiseFilterChip(
                    label = "Messages",
                    isSelected = selectedCategory == SearchCategory.MESSAGES,
                    onClick = {
                        selectedCategory = if (selectedCategory == SearchCategory.MESSAGES) null else SearchCategory.MESSAGES
                    }
                )
                WiseFilterChip(
                    label = "Contacts",
                    isSelected = selectedCategory == SearchCategory.CONTACTS,
                    onClick = {
                        selectedCategory = if (selectedCategory == SearchCategory.CONTACTS) null else SearchCategory.CONTACTS
                    }
                )
                WiseFilterChip(
                    label = "Projects",
                    isSelected = selectedCategory == SearchCategory.PROJECTS,
                    onClick = {
                        selectedCategory = if (selectedCategory == SearchCategory.PROJECTS) null else SearchCategory.PROJECTS
                    }
                )
                WiseFilterChip(
                    label = "Files",
                    isSelected = selectedCategory == SearchCategory.FILES,
                    onClick = {
                        selectedCategory = if (selectedCategory == SearchCategory.FILES) null else SearchCategory.FILES
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Result 1: Message
                item {
                    SearchResultRow(
                        avatar = {
                            WiseAvatar(
                                name = "Sarah Johnson",
                                initial = "SJ",
                                backgroundColor = Color(0xFFFFD1B2),
                                size = 42.dp
                            )
                        },
                        title = "Sarah Johnson",
                        snippet = "Design revisions look great!",
                        time = "2:24 PM",
                        onClick = { onOpenResult("chat/c1") }
                    )
                }

                // Result 2: File
                item {
                    SearchResultRow(
                        avatar = {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        title = "Design_Final_v2.pdf",
                        snippet = "in Website Redesign",
                        time = "10:32 AM",
                        onClick = { onOpenResult("files") }
                    )
                }

                // Result 3: Project
                item {
                    SearchResultRow(
                        avatar = {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFF163300),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        title = "Website Redesign",
                        snippet = "Project · Acme Co.",
                        time = "Mon",
                        onClick = { onOpenResult("project_details/p2") }
                    )
                }

                // Result 4: Message from James
                item {
                    SearchResultRow(
                        avatar = {
                            WiseAvatar(
                                name = "James Miller",
                                initial = "JM",
                                backgroundColor = Color(0xFFC7E2FE),
                                size = 42.dp
                            )
                        },
                        title = "James Miller",
                        snippet = "Can you share the design files?",
                        time = "Sun",
                        onClick = { onOpenResult("chat/c2") }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    avatar: @Composable () -> Unit,
    title: String,
    snippet: String,
    time: String,
    onClick: () -> Unit
) {
    val colors = GlobalChatTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        avatar()

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = snippet,
                fontSize = 13.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = time,
            fontSize = 12.sp,
            color = colors.textSecondary
        )
    }
}
