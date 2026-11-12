package com.example.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.example.model.formatAmount
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.NearBlack
import com.example.ui.theme.WiseLime
import com.example.model.getDisplayMonth
import com.example.model.getDisplayDay
import com.example.model.getDisplayTime

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "PEOPLE", "ACTION", "PROJECT", "FILE", "INVOICE"
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val payload: String
)

@Composable
fun SearchMorphSheet(
    onClose: () -> Unit,
    onResultSelect: (String) -> Unit,
    onCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    val allSearchItems = remember {
        listOf(
            // People
            SearchResultItem("s_sarah", "Sarah Jenkins", "Lead Product Designer · Acme Co. (London)", "PEOPLE", Icons.Default.Person, "open_chat:c1"),
            SearchResultItem("s_kenji", "Kenji Sato", "Senior Mobile Engineer · Tokyo", "PEOPLE", Icons.Default.Person, "open_chat:c2"),
            SearchResultItem("s_elena", "Elena Rostova", "VP of Product · Berlin Design Studio", "PEOPLE", Icons.Default.Person, "open_chat:c3"),
            SearchResultItem("s_david", "David Miller", "Design Partner · New York", "PEOPLE", Icons.Default.Person, "open_chat:c4"),

            // Actions
            SearchResultItem("s_act_inv", "Create Invoice", "Generate & send invoice with payment options", "ACTION", Icons.Outlined.ReceiptLong, "action:invoice"),
            SearchResultItem("s_act_sch", "Schedule Meeting", "Plan call with date, time, and timezone", "ACTION", Icons.Outlined.Schedule, "action:schedule"),
            SearchResultItem("s_act_proj", "Create Project", "New project workspace with tasks & budget", "ACTION", Icons.Outlined.WorkOutline, "action:create_project"),
            SearchResultItem("s_act_link", "Share Link", "Send prototype, Figma, or web URL in chat", "ACTION", Icons.Outlined.Link, "action:share_link"),
            SearchResultItem("s_act_att", "Attach Document", "Upload PDF, DOCX, ZIP, or photos from device", "ACTION", Icons.Outlined.AttachFile, "action:attach_file"),

            // Projects
            SearchResultItem("s_p1", "Mobile Redesign", "Acme Co. · Due May 30", "PROJECT", Icons.Outlined.WorkOutline, "project:p1"),
            SearchResultItem("s_p2", "Website Redesign", "Acme Co. · 65% complete", "PROJECT", Icons.Outlined.WorkOutline, "project:p2"),
            SearchResultItem("s_p3", "Brand Strategy", "Nova Studio · Planning", "PROJECT", Icons.Outlined.WorkOutline, "project:p3"),

            // Files
            SearchResultItem("s_f1", "Design_Final_v2.pdf", "PDF · 2.4 MB", "FILE", Icons.Outlined.Description, "file:fl1"),
            SearchResultItem("s_f2", "Project_Brief.docx", "DOCX · 1.2 MB", "FILE", Icons.Outlined.Description, "file:fl2"),
            SearchResultItem("s_f3", "Logo_Assets.zip", "ZIP · 8.7 MB", "FILE", Icons.Outlined.Description, "file:fl3"),

            // Invoices
            SearchResultItem("s_i1", "Milestone 3 — Wireframes & UI Kit", "Invoice INV-2024-003 · $1,850.00", "INVOICE", Icons.Outlined.ReceiptLong, "invoice:inv_mock_sarah"),
            SearchResultItem("s_i2", "Brand Identity & Guidelines", "Invoice INV-2024-001 · $2,400.00", "INVOICE", Icons.Outlined.ReceiptLong, "invoice:inv_past_1")
        )
    }

    val categoriesList = listOf("All", "People", "Actions", "Projects", "Files", "Invoices")

    val filteredResults = remember(searchQuery, selectedCategory) {
        allSearchItems.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "People" -> item.category == "PEOPLE"
                "Actions" -> item.category == "ACTION"
                "Projects" -> item.category == "PROJECT"
                "Files" -> item.category == "FILE"
                "Invoices" -> item.category == "INVOICE"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesQuery
        }.take(5)
    }

    LaunchedEffect(filteredResults.size) {
        onCountChanged(filteredResults.size)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAmountY = 0f },
                    onDragEnd = {
                        if (dragAmountY > 35f) {
                            onClose()
                        }
                        dragAmountY = 0f
                    },
                    onDragCancel = { dragAmountY = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f) {
                            dragAmountY += dragAmount
                        }
                    }
                )
            }
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 10.dp)
    ) {
        // Drag handle bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (colors.isDark) Color(0xFF3E443A) else Color(0xFFD2D6CC))
        )

        // Search Input Bar inside the Morphing Card
        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = if (colors.isDark) Color(0xFF222620) else Color(0xFFF1F3EE),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (colors.isDark) Color(0xFF333830) else Color(0xFFE2E6DD)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("morph_search_input_container")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = if (colors.isDark) WiseLime else NearBlack,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search people, actions, files...",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(if (colors.isDark) WiseLime else NearBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("morph_search_text_input")
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { searchQuery = "" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0xFF2C3227) else Color(0xFFE2E6DC))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categoriesList.forEach { cat ->
                val isSel = selectedCategory == cat
                Surface(
                    onClick = { selectedCategory = cat },
                    shape = RoundedCornerShape(9999.dp),
                    color = if (isSel) {
                        if (colors.isDark) WiseLime else DarkGreen
                    } else {
                        if (colors.isDark) Color(0xFF222620) else Color(0xFFEAF0E6)
                    },
                    modifier = Modifier.height(28.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) {
                                if (colors.isDark) DarkGreen else Color.White
                            } else colors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results List
        if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = colors.textSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No matching results found",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredResults.forEach { item ->
                    Surface(
                        onClick = { onResultSelect(item.payload) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (colors.isDark) Color(0xFF1E211C) else Color(0xFFF6F8F3),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (colors.isDark) Color(0xFF2D3228) else Color(0xFFE2E6DD)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("search_result_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when (item.category) {
                                            "PEOPLE" -> if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)
                                            "ACTION" -> if (colors.isDark) Color(0xFF2A2D25) else Color(0xFFFAF0E6)
                                            "PROJECT" -> if (colors.isDark) Color(0xFF1E2F3C) else Color(0xFFE1F0FA)
                                            else -> if (colors.isDark) Color(0xFF252922) else Color(0xFFEFF3EA)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = when (item.category) {
                                        "PEOPLE" -> if (colors.isDark) WiseLime else DarkGreen
                                        "ACTION" -> if (colors.isDark) Color(0xFFF5C58A) else Color(0xFF7A4E1B)
                                        "PROJECT" -> if (colors.isDark) Color(0xFF73C2F5) else Color(0xFF0F5A8A)
                                        else -> colors.textPrimary
                                    },
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.subtitle,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = if (colors.isDark) Color(0xFF262A22) else Color(0xFFEAEDE6)
                            ) {
                                Text(
                                    text = item.category,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
