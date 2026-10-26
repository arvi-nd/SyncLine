package com.example.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.components.BottomNavBar
import com.example.components.BottomNavDestination
import com.example.components.CreateBottomSheet
import com.example.components.ShowcaseDebugDialog
import com.example.screens.AssistantScreen
import com.example.screens.CalendarScreen
import com.example.screens.ChatScreen
import com.example.screens.ClientDetailsScreen
import com.example.screens.ContactsScreen
import com.example.screens.EmptyStateScreen
import com.example.screens.FilesScreen
import com.example.screens.HomeScreen
import com.example.screens.InboxScreen
import com.example.screens.LoginScreen
import com.example.screens.MoreMenuScreen
import com.example.screens.NotificationsScreen
import com.example.screens.ProjectDetailsScreen
import com.example.screens.ProjectsScreen
import com.example.screens.ScheduleMessageScreen
import com.example.screens.SearchScreen
import com.example.screens.SettingsScreen
import com.example.screens.SplashScreen
import com.example.screens.TimezonesScreen
import com.example.model.AttachmentType
import com.example.ui.theme.GlobalChatTheme
import com.example.viewmodel.GlobalChatViewModel

@Composable
fun GlobalChatNavGraph(
    viewModel: GlobalChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var isCreateSheetOpen by remember { mutableStateOf(false) }
    var currentBottomTab by remember { mutableStateOf(BottomNavDestination.HOME) }
    val isDebugDialogOpen by viewModel.isDebugDialogOpen.collectAsState()
    val chatDraft by viewModel.chatDraft.collectAsState()
    val activeClientId by viewModel.activeChatClientId.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            // Screen 1: Splash
            composable(Screen.Splash.route) {
                SplashScreen(
                    onGetStarted = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Screen 2: Login
            composable(Screen.Login.route) {
                LoginScreen(
                    onSignInSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Screen 3 / Main Screen with BottomNav: Home (Overview), Chat (Inbox & Conversation), Activity, Account
            composable(Screen.Main.route) {
                var activeChatConversationId by remember { mutableStateOf<String?>(null) }

                if (activeChatConversationId != null) {
                    BackHandler {
                        activeChatConversationId = null
                    }
                } else if (currentBottomTab != BottomNavDestination.HOME) {
                    BackHandler {
                        currentBottomTab = BottomNavDestination.HOME
                    }
                }

                val isChatConversationActive = currentBottomTab == BottomNavDestination.CHAT && activeChatConversationId != null

                Scaffold(
                    containerColor = GlobalChatTheme.colors.screenBackground,
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        BottomNavBar(
                            currentDestination = currentBottomTab,
                            isChatMode = isChatConversationActive,
                            chatText = chatDraft,
                            onChatTextChange = { viewModel.setChatDraft(it) },
                            onSendMessage = {
                                viewModel.sendCurrentMessage(activeChatConversationId ?: activeClientId)
                            },
                            onNavigate = { destination ->
                                if (destination == BottomNavDestination.CHAT && currentBottomTab == BottomNavDestination.CHAT && activeChatConversationId != null) {
                                    activeChatConversationId = null
                                } else {
                                    currentBottomTab = destination
                                }
                            },
                            onPlusClick = {
                                isCreateSheetOpen = true
                            },
                            onSearchClick = {
                                navController.navigate(Screen.Search.route)
                            },
                            onActionPillClick = { actionId ->
                                if (actionId.startsWith("project_created:")) {
                                    val payload = actionId.removePrefix("project_created:")
                                    val parts = payload.split(":")
                                    val projName = parts.getOrNull(0)?.ifBlank { "New Project" } ?: "New Project"
                                    val colorLight = parts.getOrNull(1)?.toLongOrNull() ?: 0xFFE2F6D5L
                                    val colorDark = parts.getOrNull(2)?.toLongOrNull() ?: 0xFF243A1BL
                                    val targetClient = viewModel.getClient(activeChatConversationId ?: activeClientId)
                                    viewModel.addProjectAndSendToChat(
                                        conversationId = activeChatConversationId ?: activeClientId,
                                        title = projName,
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        colorHexLight = colorLight,
                                        colorHexDark = colorDark
                                    )
                                } else if (actionId.startsWith("project_selected:")) {
                                    val projName = actionId.removePrefix("project_selected:")
                                    val (colorLight, colorDark) = when (projName) {
                                        "Brand Identity" -> Pair(0xFFE1F0FAL, 0xFF1E2F3CL)
                                        "Social Media Reels" -> Pair(0xFFFCEBD9L, 0xFF3B2B1EL)
                                        else -> Pair(0xFFE2F6D5L, 0xFF243A1BL)
                                    }
                                    val targetClient = viewModel.getClient(activeChatConversationId ?: activeClientId)
                                    viewModel.addProjectAndSendToChat(
                                        conversationId = activeChatConversationId ?: activeClientId,
                                        title = projName,
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        colorHexLight = colorLight,
                                        colorHexDark = colorDark
                                    )
                                } else if (actionId.startsWith("attach_option:")) {
                                    val opt = actionId.removePrefix("attach_option:")
                                    val attachText = when (opt) {
                                        "choose_file" -> "📎 Attached file: Project_Brief_v2.pdf (2.4 MB)"
                                        "photo_video" -> "📷 Attached media: Design_Mockup.png (4.1 MB)"
                                        "share_link" -> "🔗 Shared link: https://figma.com/file/wise-global-chat"
                                        "recent_files" -> "📁 Attached recent file: Contract_Signed.pdf"
                                        else -> "📎 Attached file"
                                    }
                                    viewModel.sendMessage(activeChatConversationId ?: activeClientId, attachText)
                                } else if (actionId.startsWith("file_attached:")) {
                                    val payload = actionId.removePrefix("file_attached:")
                                    val parts = payload.split(":")
                                    val fName = parts.getOrNull(0)?.ifBlank { "Document.pdf" } ?: "Document.pdf"
                                    val fSize = parts.getOrNull(1)?.ifBlank { "1.2 MB" } ?: "1.2 MB"
                                    val fType = parts.getOrNull(2)?.ifBlank { "PDF" } ?: "PDF"
                                    val attType = when (fType.uppercase()) {
                                        "PDF" -> AttachmentType.PDF
                                        "DOC", "DOCX" -> AttachmentType.DOCX
                                        "ZIP", "RAR" -> AttachmentType.ZIP
                                        "TXT" -> AttachmentType.TXT
                                        else -> AttachmentType.PDF
                                    }
                                    viewModel.addFileAttachmentAndSendToChat(
                                        conversationId = activeChatConversationId ?: activeClientId,
                                        fileName = fName,
                                        fileSize = fSize,
                                        fileType = fType,
                                        attachmentType = attType
                                    )
                                } else if (actionId.startsWith("photo_attached:")) {
                                    val payload = actionId.removePrefix("photo_attached:")
                                    val parts = payload.split(":")
                                    val fName = parts.getOrNull(0)?.ifBlank { "Photo.jpg" } ?: "Photo.jpg"
                                    val fSize = parts.getOrNull(1)?.ifBlank { "2.4 MB" } ?: "2.4 MB"
                                    val fType = parts.getOrNull(2)?.ifBlank { "JPG" } ?: "JPG"
                                    viewModel.addFileAttachmentAndSendToChat(
                                        conversationId = activeChatConversationId ?: activeClientId,
                                        fileName = fName,
                                        fileSize = fSize,
                                        fileType = fType,
                                        attachmentType = AttachmentType.IMAGE
                                    )
                                } else if (actionId.startsWith("link_shared:")) {
                                    val payload = actionId.removePrefix("link_shared:")
                                    val parts = payload.split(":::")
                                    val linkTitle = parts.getOrNull(0)?.trim() ?: ""
                                    val linkUrl = parts.getOrNull(1)?.trim() ?: ""
                                    val formattedUrl = if (linkUrl.startsWith("http://") || linkUrl.startsWith("https://")) linkUrl else "https://$linkUrl"
                                    viewModel.sendLinkToChat(
                                        conversationId = activeChatConversationId ?: activeClientId,
                                        title = linkTitle,
                                        url = formattedUrl
                                    )
                                } else if (actionId == "schedule_meeting") {
                                    val targetClient = viewModel.getClient(activeChatConversationId ?: activeClientId)
                                    viewModel.addMeetingAndSendToChat(
                                        conversationId = activeChatConversationId ?: activeClientId,
                                        title = "Project Sync",
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        dateString = "Tomorrow",
                                        time = "3:00 PM",
                                        duration = "30 min",
                                        status = "Confirmed"
                                    )
                                } else if (actionId.startsWith("meeting_scheduled:")) {
                                    val payload = actionId.removePrefix("meeting_scheduled:")
                                    val parts = payload.split(":")
                                    val mTitle = parts.getOrNull(0)?.ifBlank { "Project review" } ?: "Project review"
                                    val mDate = parts.getOrNull(1)?.ifBlank { "Oct 28" } ?: "Oct 28"
                                    val mTime = parts.getOrNull(2)?.ifBlank { "10:00 AM" } ?: "10:00 AM"
                                    val mDuration = parts.getOrNull(3)?.ifBlank { "30 min" } ?: "30 min"
                                    val mNotes = parts.getOrNull(4)?.takeIf { it.isNotBlank() }
                                    val targetClient = viewModel.getClient(activeChatConversationId ?: activeClientId)
                                    viewModel.addMeetingAndSendToChat(
                                        conversationId = activeChatConversationId ?: activeClientId,
                                        title = mTitle,
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        dateString = mDate,
                                        time = mTime,
                                        duration = mDuration,
                                        status = "Confirmed",
                                        notes = mNotes
                                    )
                                } else if (actionId.startsWith("invoice_created:")) {
                                    val payload = actionId.removePrefix("invoice_created:")
                                    val parts = payload.split(":::")
                                    val invTitle = parts.getOrNull(0)?.ifBlank { "Video editing — October" } ?: "Video editing — October"
                                    val invProject = parts.getOrNull(1)?.takeIf { it.isNotBlank() && it != "Select project (optional)" }
                                    val rawAmount = parts.getOrNull(2)?.replace("$", "")?.replace(",", "")?.trim() ?: "1250.00"
                                    val invAmount = rawAmount.toDoubleOrNull() ?: 1250.0
                                    val invDueDate = parts.getOrNull(3)?.ifBlank { "Oct 31, 2026" } ?: "Oct 31, 2026"
                                    val invNotes = parts.getOrNull(4)?.takeIf { it.isNotBlank() }
                                    val targetClient = viewModel.getClient(activeChatConversationId ?: activeClientId)
                                    viewModel.addInvoiceAndSendToChat(
                                        conversationId = activeChatConversationId ?: activeClientId,
                                        title = invTitle,
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        projectName = invProject,
                                        amount = invAmount,
                                        dueDate = invDueDate,
                                        notes = invNotes
                                    )
                                } else if (actionId == "view_meetings") {
                                    val targetClient = viewModel.getClient(activeChatConversationId ?: activeClientId)
                                    viewModel.sendMessage(
                                        activeChatConversationId ?: activeClientId,
                                        "📅 Upcoming scheduled meetings with ${targetClient?.name ?: "Sarah Jenkins"}:\n• Design Review - Tomorrow, 3:00 PM EST\n• Milestone Checkpoint - Friday, 11:00 AM EST"
                                    )
                                }
                            },
                            clientName = viewModel.getClient(activeChatConversationId ?: activeClientId)?.name ?: "Sarah Jenkins"
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        when (currentBottomTab) {
                            BottomNavDestination.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onOpenChat = { clientId ->
                                        viewModel.setActiveChatClient(clientId)
                                        activeChatConversationId = clientId
                                        currentBottomTab = BottomNavDestination.CHAT
                                    },
                                    onViewAllChats = {
                                        activeChatConversationId = null
                                        currentBottomTab = BottomNavDestination.CHAT
                                    },
                                    onOpenProject = { projectId ->
                                        navController.navigate(Screen.ProjectDetails.createRoute(projectId))
                                    },
                                    onOpenCalendar = {
                                        navController.navigate(Screen.Calendar.route)
                                    },
                                    onOpenSearch = {
                                        navController.navigate(Screen.Search.route)
                                    },
                                    onOpenNotifications = {
                                        currentBottomTab = BottomNavDestination.ACTIVITY
                                    }
                                )
                            }
                            BottomNavDestination.CHAT -> {
                                if (activeChatConversationId != null) {
                                    ChatScreen(
                                        clientId = activeChatConversationId!!,
                                        viewModel = viewModel,
                                        onBack = { activeChatConversationId = null },
                                        onOpenClientDetails = { cId ->
                                            navController.navigate(Screen.ClientDetails.createRoute(cId))
                                        },
                                        showInternalComposer = false
                                    )
                                } else {
                                    InboxScreen(
                                        viewModel = viewModel,
                                        onOpenChat = { clientId ->
                                            viewModel.setActiveChatClient(clientId)
                                            activeChatConversationId = clientId
                                        },
                                        onOpenSearch = {
                                            navController.navigate(Screen.Search.route)
                                        },
                                        onOpenCreate = {
                                            isCreateSheetOpen = true
                                        }
                                    )
                                }
                            }
                            BottomNavDestination.ACTIVITY -> {
                                NotificationsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentBottomTab = BottomNavDestination.HOME }
                                )
                            }
                            BottomNavDestination.ACCOUNT -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentBottomTab = BottomNavDestination.HOME }
                                )
                            }
                        }
                    }
                }
            }

            // Screen 4: Chat Screen (Dedicated route)
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("clientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId") ?: "c1"
                viewModel.setActiveChatClient(clientId)
                Scaffold(
                    containerColor = GlobalChatTheme.colors.screenBackground,
                    bottomBar = {
                        BottomNavBar(
                            currentDestination = BottomNavDestination.CHAT,
                            isChatMode = true,
                            chatText = chatDraft,
                            onChatTextChange = { viewModel.setChatDraft(it) },
                            onSendMessage = { viewModel.sendCurrentMessage(clientId) },
                            onNavigate = { destination ->
                                if (destination == BottomNavDestination.HOME) {
                                    navController.popBackStack()
                                } else if (destination == BottomNavDestination.CHAT) {
                                    // Already in chat
                                } else {
                                    currentBottomTab = destination
                                    navController.popBackStack(Screen.Main.route, false)
                                }
                            },
                            onPlusClick = {
                                isCreateSheetOpen = true
                            },
                            onSearchClick = {
                                navController.navigate(Screen.Search.route)
                            },
                            onActionPillClick = { actionId ->
                                if (actionId.startsWith("project_created:")) {
                                    val payload = actionId.removePrefix("project_created:")
                                    val parts = payload.split(":")
                                    val projName = parts.getOrNull(0)?.ifBlank { "New Project" } ?: "New Project"
                                    val colorLight = parts.getOrNull(1)?.toLongOrNull() ?: 0xFFE2F6D5L
                                    val colorDark = parts.getOrNull(2)?.toLongOrNull() ?: 0xFF243A1BL
                                    val targetClient = viewModel.getClient(clientId)
                                    viewModel.addProjectAndSendToChat(
                                        conversationId = clientId,
                                        title = projName,
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        colorHexLight = colorLight,
                                        colorHexDark = colorDark
                                    )
                                } else if (actionId.startsWith("project_selected:")) {
                                    val projName = actionId.removePrefix("project_selected:")
                                    val (colorLight, colorDark) = when (projName) {
                                        "Brand Identity" -> Pair(0xFFE1F0FAL, 0xFF1E2F3CL)
                                        "Social Media Reels" -> Pair(0xFFFCEBD9L, 0xFF3B2B1EL)
                                        else -> Pair(0xFFE2F6D5L, 0xFF243A1BL)
                                    }
                                    val targetClient = viewModel.getClient(clientId)
                                    viewModel.addProjectAndSendToChat(
                                        conversationId = clientId,
                                        title = projName,
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        colorHexLight = colorLight,
                                        colorHexDark = colorDark
                                    )
                                } else if (actionId.startsWith("attach_option:")) {
                                    val opt = actionId.removePrefix("attach_option:")
                                    val attachText = when (opt) {
                                        "choose_file" -> "📎 Attached file: Project_Brief_v2.pdf (2.4 MB)"
                                        "photo_video" -> "📷 Attached media: Design_Mockup.png (4.1 MB)"
                                        "share_link" -> "🔗 Shared link: https://figma.com/file/wise-global-chat"
                                        "recent_files" -> "📁 Attached recent file: Contract_Signed.pdf"
                                        else -> "📎 Attached file"
                                    }
                                    viewModel.sendMessage(clientId, attachText)
                                } else if (actionId.startsWith("file_attached:")) {
                                    val payload = actionId.removePrefix("file_attached:")
                                    val parts = payload.split(":")
                                    val fName = parts.getOrNull(0)?.ifBlank { "Document.pdf" } ?: "Document.pdf"
                                    val fSize = parts.getOrNull(1)?.ifBlank { "1.2 MB" } ?: "1.2 MB"
                                    val fType = parts.getOrNull(2)?.ifBlank { "PDF" } ?: "PDF"
                                    val attType = when (fType.uppercase()) {
                                        "PDF" -> AttachmentType.PDF
                                        "DOC", "DOCX" -> AttachmentType.DOCX
                                        "ZIP", "RAR" -> AttachmentType.ZIP
                                        "TXT" -> AttachmentType.TXT
                                        else -> AttachmentType.PDF
                                    }
                                    viewModel.addFileAttachmentAndSendToChat(
                                        conversationId = clientId,
                                        fileName = fName,
                                        fileSize = fSize,
                                        fileType = fType,
                                        attachmentType = attType
                                    )
                                } else if (actionId.startsWith("photo_attached:")) {
                                    val payload = actionId.removePrefix("photo_attached:")
                                    val parts = payload.split(":")
                                    val fName = parts.getOrNull(0)?.ifBlank { "Photo.jpg" } ?: "Photo.jpg"
                                    val fSize = parts.getOrNull(1)?.ifBlank { "2.4 MB" } ?: "2.4 MB"
                                    val fType = parts.getOrNull(2)?.ifBlank { "JPG" } ?: "JPG"
                                    viewModel.addFileAttachmentAndSendToChat(
                                        conversationId = clientId,
                                        fileName = fName,
                                        fileSize = fSize,
                                        fileType = fType,
                                        attachmentType = AttachmentType.IMAGE
                                    )
                                } else if (actionId.startsWith("link_shared:")) {
                                    val payload = actionId.removePrefix("link_shared:")
                                    val parts = payload.split(":::")
                                    val linkTitle = parts.getOrNull(0)?.trim() ?: ""
                                    val linkUrl = parts.getOrNull(1)?.trim() ?: ""
                                    val formattedUrl = if (linkUrl.startsWith("http://") || linkUrl.startsWith("https://")) linkUrl else "https://$linkUrl"
                                    viewModel.sendLinkToChat(
                                        conversationId = clientId,
                                        title = linkTitle,
                                        url = formattedUrl
                                    )
                                } else if (actionId == "schedule_meeting") {
                                    val targetClient = viewModel.getClient(clientId)
                                    viewModel.addMeetingAndSendToChat(
                                        conversationId = clientId,
                                        title = "Project Sync",
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        dateString = "Tomorrow",
                                        time = "3:00 PM",
                                        duration = "30 min",
                                        status = "Confirmed"
                                    )
                                } else if (actionId.startsWith("meeting_scheduled:")) {
                                    val payload = actionId.removePrefix("meeting_scheduled:")
                                    val parts = payload.split(":")
                                    val mTitle = parts.getOrNull(0)?.ifBlank { "Project review" } ?: "Project review"
                                    val mDate = parts.getOrNull(1)?.ifBlank { "Oct 28" } ?: "Oct 28"
                                    val mTime = parts.getOrNull(2)?.ifBlank { "10:00 AM" } ?: "10:00 AM"
                                    val mDuration = parts.getOrNull(3)?.ifBlank { "30 min" } ?: "30 min"
                                    val mNotes = parts.getOrNull(4)?.takeIf { it.isNotBlank() }
                                    val targetClient = viewModel.getClient(clientId)
                                    viewModel.addMeetingAndSendToChat(
                                        conversationId = clientId,
                                        title = mTitle,
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        dateString = mDate,
                                        time = mTime,
                                        duration = mDuration,
                                        status = "Confirmed",
                                        notes = mNotes
                                    )
                                } else if (actionId.startsWith("invoice_created:")) {
                                    val payload = actionId.removePrefix("invoice_created:")
                                    val parts = payload.split(":::")
                                    val invTitle = parts.getOrNull(0)?.ifBlank { "Video editing — October" } ?: "Video editing — October"
                                    val invProject = parts.getOrNull(1)?.takeIf { it.isNotBlank() && it != "Select project (optional)" }
                                    val rawAmount = parts.getOrNull(2)?.replace("$", "")?.replace(",", "")?.trim() ?: "1250.00"
                                    val invAmount = rawAmount.toDoubleOrNull() ?: 1250.0
                                    val invDueDate = parts.getOrNull(3)?.ifBlank { "Oct 31, 2026" } ?: "Oct 31, 2026"
                                    val invNotes = parts.getOrNull(4)?.takeIf { it.isNotBlank() }
                                    val targetClient = viewModel.getClient(clientId)
                                    viewModel.addInvoiceAndSendToChat(
                                        conversationId = clientId,
                                        title = invTitle,
                                        clientName = targetClient?.name ?: "Sarah Jenkins",
                                        projectName = invProject,
                                        amount = invAmount,
                                        dueDate = invDueDate,
                                        notes = invNotes
                                    )
                                } else if (actionId == "view_meetings") {
                                    val targetClient = viewModel.getClient(clientId)
                                    viewModel.sendMessage(
                                        clientId,
                                        "📅 Upcoming scheduled meetings with ${targetClient?.name ?: "Sarah Jenkins"}:\n• Design Review - Tomorrow, 3:00 PM EST\n• Milestone Checkpoint - Friday, 11:00 AM EST"
                                    )
                                }
                            },
                            clientName = viewModel.getClient(clientId)?.name ?: "Sarah Jenkins"
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ChatScreen(
                            clientId = clientId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onOpenClientDetails = { cId ->
                                navController.navigate(Screen.ClientDetails.createRoute(cId))
                            },
                            showInternalComposer = false
                        )
                    }
                }
            }

            // Screen 5: Client Details / Timezone
            composable(
                route = Screen.ClientDetails.route,
                arguments = listOf(navArgument("clientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId") ?: "c1"
                ClientDetailsScreen(
                    clientId = clientId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onScheduleMessage = { cId ->
                        navController.navigate(Screen.ScheduleMessage.createRoute(cId))
                    }
                )
            }

            // Screen 6: Global Timezones
            composable(Screen.Timezones.route) {
                TimezonesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 8 / 9: Project Details
            composable(
                route = Screen.ProjectDetails.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: "p2"
                ProjectDetailsScreen(
                    projectId = projectId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onViewChat = { clientId ->
                        navController.navigate(Screen.Chat.createRoute(clientId))
                    }
                )
            }

            // Screen 10: Files & Attachments
            composable(Screen.Files.route) {
                FilesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 11: Schedule Message
            composable(
                route = Screen.ScheduleMessage.route,
                arguments = listOf(navArgument("clientId") { type = NavType.StringType })
            ) { backStackEntry ->
                val clientId = backStackEntry.arguments?.getString("clientId") ?: "c1"
                ScheduleMessageScreen(
                    clientId = clientId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onScheduledSuccess = { navController.popBackStack() }
                )
            }

            // Screen 12: Calendar / Meetings
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 13: Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 14: Search
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenResult = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // Screen 15: Notifications
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 16: Assistant
            composable(Screen.Assistant.route) {
                AssistantScreen(
                    onBack = { navController.popBackStack() },
                    onViewDetails = {
                        navController.navigate(Screen.Chat.createRoute("c1"))
                    }
                )
            }

            // Screen 17: Empty State
            composable(Screen.EmptyState.route) {
                EmptyStateScreen(
                    onNewMessage = {
                        navController.navigate(Screen.Chat.createRoute("c1"))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Central Plus Create Action Bottom Sheet
        if (isCreateSheetOpen) {
            CreateBottomSheet(
                onDismiss = { isCreateSheetOpen = false },
                onNewChat = {
                    navController.navigate(Screen.Chat.createRoute("c1"))
                },
                onNewProject = {
                    navController.navigate(Screen.ProjectDetails.createRoute("p1"))
                },
                onScheduleMessage = {
                    navController.navigate(Screen.ScheduleMessage.createRoute("c1"))
                }
            )
        }

        // Debug-only Showcase Inspector Dialog
        ShowcaseDebugDialog(
            isOpen = isDebugDialogOpen,
            onDismiss = { viewModel.closeDebugDialog() },
            onSelectScreen = { route ->
                navController.navigate(route)
            }
        )
    }
}
