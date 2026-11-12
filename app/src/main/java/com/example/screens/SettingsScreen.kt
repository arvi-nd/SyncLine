package com.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.WiseAvatar
import com.example.components.WiseCard
import com.example.ui.theme.GlobalChatTheme
import com.example.viewmodel.GlobalChatViewModel

private enum class SettingsDialog { NONE, NAME, TIMEZONE, HOURS, AVAILABILITY, REMINDERS }

@Composable
fun SettingsScreen(
    viewModel: GlobalChatViewModel,
    onBack: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val isDark by viewModel.isDarkTheme.collectAsState()
    var openDialog by remember { mutableStateOf(SettingsDialog.NONE) }
    var notice by remember { mutableStateOf<String?>(null) }
    var reminderLead by remember { mutableStateOf(viewModel.reminderLeadMinutes) }
    val notificationPermission = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) notice = "Reminders need notification permission. You can allow it in the phone's app settings."
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.screenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
        ) {
            // Header: same place as the Overview title (it has a date line above it)
            Spacer(modifier = Modifier.height(25.dp))
            Text(
                text = "Settings",
                fontFamily = com.example.ui.theme.DisplayBlackFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary,
                letterSpacing = (-1.2).sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            val currentUser by viewModel.currentUser.collectAsState()
            val userProfile by viewModel.userProfile.collectAsState()

            val emailText = userProfile?.email?.takeIf { it.isNotBlank() }
                ?: currentUser?.email
                ?: "Not signed in"

            val nameText = userProfile?.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser?.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                ?: "User Account"

            val initialText = nameText.take(2).uppercase()
            val workStart = userProfile?.workingStartHour ?: 9
            val workEnd = userProfile?.workingEndHour ?: 18
            val availability = userProfile?.availability ?: "Available"
            val save: (String?, String?, Int?, Int?, String?) -> Unit = { n, tz, ws, we, av ->
                viewModel.updateProfileSettings(n, tz, ws, we, av) { ok ->
                    notice = if (ok) "Saved." else "Could not save. Check your connection and try again."
                }
                openDialog = SettingsDialog.NONE
            }
            when (openDialog) {
                SettingsDialog.NAME -> EditNameDialog(nameText, { openDialog = SettingsDialog.NONE }) { save(it, null, null, null, null) }
                SettingsDialog.TIMEZONE -> TimezoneDialog(userProfile?.timezone.orEmpty(), { openDialog = SettingsDialog.NONE }) { save(null, it, null, null, null) }
                SettingsDialog.HOURS -> WorkingHoursDialog(workStart, workEnd, { openDialog = SettingsDialog.NONE }) { st, en -> save(null, null, st, en, null) }
                SettingsDialog.AVAILABILITY -> AvailabilityDialog(availability, { openDialog = SettingsDialog.NONE }) { save(null, null, null, null, it) }
                SettingsDialog.REMINDERS -> ReminderLeadDialog(reminderLead, { openDialog = SettingsDialog.NONE }) { minutes ->
                    reminderLead = minutes
                    viewModel.setReminderLead(minutes)
                    openDialog = SettingsDialog.NONE
                    if (minutes > 0 && android.os.Build.VERSION.SDK_INT >= 33) {
                        notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                SettingsDialog.NONE -> {}
            }

            // Profile Card
            WiseCard(
                cornerRadius = 24.dp,
                onClick = { openDialog = SettingsDialog.NAME },
                modifier = Modifier.fillMaxWidth().testTag("settings_profile_card")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WiseAvatar(
                        name = nameText,
                        initial = initialText,
                        backgroundColor = Color(0xFFC7E2FE),
                        size = 56.dp,
                        isOnline = currentUser != null
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nameText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = emailText,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        if (currentUser != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap to edit your name",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.brandLime
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            }

            notice?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, fontSize = 13.sp, color = colors.textSecondary, modifier = Modifier.testTag("settings_notice"))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "General",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Settings Rows
            WiseCard(
                cornerRadius = 20.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsRowItem(
                        icon = Icons.Outlined.Brightness4,
                        title = "Appearance",
                        value = if (isDark) "Dark" else "Light",
                        onClick = { viewModel.toggleTheme() },
                        testTag = "settings_row_appearance"
                    )

                    SettingsRowItem(
                        icon = Icons.Outlined.Public,
                        title = "Timezone",
                        value = userProfile?.timezone?.takeIf { it.isNotBlank() }?.substringAfterLast('/')?.replace('_', ' ') ?: "Not set",
                        onClick = { openDialog = SettingsDialog.TIMEZONE },
                        testTag = "settings_row_timezone"
                    )

                    SettingsRowItem(
                        icon = Icons.Outlined.Schedule,
                        title = "Working hours",
                        value = "${formatHourLabel(workStart)} - ${formatHourLabel(workEnd)}",
                        onClick = { openDialog = SettingsDialog.HOURS },
                        testTag = "settings_row_hours"
                    )

                    SettingsRowItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Meeting reminders",
                        value = com.example.reminders.MeetingReminders.label(reminderLead),
                        onClick = { openDialog = SettingsDialog.REMINDERS },
                        testTag = "settings_row_reminders"
                    )

                    SettingsRowItem(
                        icon = Icons.Outlined.CheckCircle,
                        title = "Availability",
                        value = availability,
                        onClick = { openDialog = SettingsDialog.AVAILABILITY },
                        testTag = "settings_row_availability"
                    )

                    SettingsRowItem(
                        icon = Icons.Outlined.Lock,
                        title = "Change password",
                        onClick = {
                            viewModel.sendPasswordReset(
                                emailStr = emailText,
                                onSuccess = { notice = "Password reset link sent to $emailText." },
                                onError = { notice = it }
                            )
                        },
                        testTag = "settings_row_password"
                    )

                    SettingsRowItem(
                        icon = Icons.Outlined.Info,
                        title = "About SyncLine",
                        value = "v${com.example.BuildConfig.VERSION_NAME}",
                        onClick = { notice = "SyncLine ${com.example.BuildConfig.VERSION_NAME} · freelancer chat, projects, invoices and timezones." }
                    )

                    SettingsRowItem(
                        icon = Icons.AutoMirrored.Outlined.ExitToApp,
                        title = "Sign Out",
                        onClick = {
                            viewModel.signOut {
                                onSignOut?.invoke()
                            }
                        },
                        testTag = "settings_row_sign_out"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    testTag: String = "settings_row"
) {
    val colors = GlobalChatTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        value?.let {
            Text(
                text = it,
                fontSize = 13.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
