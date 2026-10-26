package com.example.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WiseCard
import com.example.components.WiseFilterChip
import com.example.model.AttachmentType
import com.example.model.FileAttachment
import com.example.ui.theme.GlobalChatTheme
import com.example.viewmodel.FilesFilter
import com.example.viewmodel.GlobalChatViewModel

@Composable
fun FilesScreen(
    viewModel: GlobalChatViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val files by viewModel.files.collectAsState()
    val filter by viewModel.filesFilter.collectAsState()

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("files_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Files",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WiseFilterChip(
                    label = "All",
                    isSelected = filter == FilesFilter.ALL,
                    onClick = { viewModel.setFilesFilter(FilesFilter.ALL) }
                )
                WiseFilterChip(
                    label = "Shared",
                    isSelected = filter == FilesFilter.SHARED,
                    onClick = { viewModel.setFilesFilter(FilesFilter.SHARED) }
                )
                WiseFilterChip(
                    label = "My Files",
                    isSelected = filter == FilesFilter.MY_FILES,
                    onClick = { viewModel.setFilesFilter(FilesFilter.MY_FILES) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Files List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(files, key = { it.id }) { file ->
                    FileRowItem(file = file)
                }
            }
        }
    }
}

@Composable
fun FileRowItem(file: FileAttachment) {
    val colors = GlobalChatTheme.colors

    val (badgeBg, badgeColor) = when (file.attachmentType) {
        AttachmentType.PDF -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        AttachmentType.DOCX -> Color(0xFFEFF6FF) to Color(0xFF2563EB)
        AttachmentType.ZIP -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        AttachmentType.TXT -> Color(0xFFF3E8FF) to Color(0xFF9333EA)
        AttachmentType.FIGMA -> Color(0xFFFEE2E2) to Color(0xFFEA580C)
        AttachmentType.FOLDER -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        AttachmentType.IMAGE -> Color(0xFFFAF0E6) to Color(0xFFD97706)
    }

    WiseCard(
        cornerRadius = 18.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("file_item_${file.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (file.attachmentType) {
                        AttachmentType.FOLDER -> Icons.Default.Folder
                        AttachmentType.IMAGE -> Icons.Outlined.Image
                        else -> Icons.Outlined.Description
                    },
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${file.size} · ${file.type}",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Download",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
