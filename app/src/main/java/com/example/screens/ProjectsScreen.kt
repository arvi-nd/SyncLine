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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.components.WiseCard
import com.example.components.WiseFilterChip
import com.example.components.WiseStatusChip
import com.example.model.Project
import com.example.model.ProjectStatus
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.GlobalChatTheme
import com.example.ui.theme.NearBlack
import com.example.ui.theme.WiseLime
import com.example.viewmodel.GlobalChatViewModel
import com.example.viewmodel.ProjectsFilter

import com.example.components.DeleteProjectConfirmationDialog
import com.example.components.ProjectOptionsMorphMenu
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ProjectsScreen(
    viewModel: GlobalChatViewModel,
    onOpenProjectDetails: (String) -> Unit,
    onOpenCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val projects by viewModel.projects.collectAsState()
    val filter by viewModel.projectsFilter.collectAsState()
    val isDeleting by viewModel.isDeletingProject.collectAsState()
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    var deleteErrorMsg by remember { mutableStateOf<String?>(null) }

    val filteredProjects = when (filter) {
        ProjectsFilter.ACTIVE -> projects.filter { it.status == ProjectStatus.IN_PROGRESS || it.status == ProjectStatus.PLANNING }
        ProjectsFilter.COMPLETED -> projects.filter { it.status == ProjectStatus.COMPLETED }
        ProjectsFilter.ALL -> projects
    }

    if (projectToDelete != null) {
        val proj = projectToDelete!!
        DeleteProjectConfirmationDialog(
            projectTitle = proj.title,
            isDeleting = isDeleting,
            errorMessage = deleteErrorMsg,
            onConfirmDelete = {
                deleteErrorMsg = null
                viewModel.deleteProject(
                    projectId = proj.id,
                    onSuccess = {
                        projectToDelete = null
                    },
                    onError = { err ->
                        deleteErrorMsg = err
                    }
                )
            },
            onDismiss = {
                if (!isDeleting) projectToDelete = null
            },
            confirmTag = "confirm_delete_project_list_btn",
            cancelTag = "cancel_delete_project_list_btn"
        )
    }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Projects",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(WiseLime)
                        .clickable(onClick = onOpenCreate)
                        .testTag("projects_add_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Project",
                        tint = DarkGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WiseFilterChip(
                    label = "Active",
                    isSelected = filter == ProjectsFilter.ACTIVE,
                    onClick = { viewModel.setProjectsFilter(ProjectsFilter.ACTIVE) }
                )
                WiseFilterChip(
                    label = "Completed",
                    isSelected = filter == ProjectsFilter.COMPLETED,
                    onClick = { viewModel.setProjectsFilter(ProjectsFilter.COMPLETED) }
                )
                WiseFilterChip(
                    label = "All",
                    isSelected = filter == ProjectsFilter.ALL,
                    onClick = { viewModel.setProjectsFilter(ProjectsFilter.ALL) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Project Cards
            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (colors.isDark) Color(0xFF243A1B) else Color(0xFFE2F6D5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (colors.isDark) WiseLime else DarkGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "No projects yet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Create a new project to collaborate, track milestones, and share task progress with your client.",
                            fontSize = 13.5.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Surface(
                            onClick = onOpenCreate,
                            shape = RoundedCornerShape(9999.dp),
                            color = WiseLime,
                            modifier = Modifier.testTag("empty_state_create_project_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = DarkGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Create Project",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredProjects, key = { it.id }) { project ->
                        ProjectCardItem(
                            project = project,
                            onClick = { onOpenProjectDetails(project.id) },
                            onDelete = { projectToDelete = project }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCardItem(
    project: Project,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val colors = GlobalChatTheme.colors

    WiseCard(
        cornerRadius = 22.dp,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color(0xFF222820) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (colors.isDark) WiseLime else NearBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${project.clientName} · ${project.dueDate}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                if (onDelete != null) {
                    ProjectOptionsMorphMenu(
                        projectTitle = project.title,
                        onDeleteClick = onDelete,
                        buttonTag = "project_card_menu_${project.id}",
                        menuTag = "project_card_delete_item_${project.id}"
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Files",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status chip & Progress percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WiseStatusChip(status = project.status)

                Text(
                    text = "${project.progress}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { project.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(9999.dp)),
                color = WiseLime,
                trackColor = colors.cardBorder.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
