package com.example.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.TimezoneEngine
import com.example.ui.theme.GlobalChatTheme

@Composable
private fun dialogButtonColors() = ButtonDefaults.textButtonColors(contentColor = GlobalChatTheme.colors.textPrimary)

internal fun formatHourLabel(hour: Int): String {
    val h = ((hour % 24) + 24) % 24
    val display = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
    return "$display:00 ${if (h < 12) "AM" else "PM"}"
}

@Composable
fun EditNameDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlobalChatTheme.colors.cardBackground,
        titleContentColor = GlobalChatTheme.colors.textPrimary,
        textContentColor = GlobalChatTheme.colors.textPrimary,
        title = { Text("Your name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 50) name = it },
                singleLine = true,
                label = { Text("Display name") }
            )
        },
        confirmButton = {
            TextButton(colors = dialogButtonColors(), onClick = { onSave(name.trim()) }, enabled = name.trim().length >= 2) { Text("Save") }
        },
        dismissButton = { TextButton(colors = dialogButtonColors(), onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun WorkingHoursDialog(start: Int, end: Int, onDismiss: () -> Unit, onSave: (Int, Int) -> Unit) {
    var startHour by remember { mutableIntStateOf(start) }
    var endHour by remember { mutableIntStateOf(end) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlobalChatTheme.colors.cardBackground,
        titleContentColor = GlobalChatTheme.colors.textPrimary,
        textContentColor = GlobalChatTheme.colors.textPrimary,
        title = { Text("Working hours") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Clients see these hours (Mon to Fri) in your local time.", fontSize = 13.sp)
                HourStepper("Starts", startHour, onMinus = { if (startHour > 0) startHour-- }, onPlus = { if (startHour < endHour - 1) startHour++ })
                HourStepper("Ends", endHour, onMinus = { if (endHour > startHour + 1) endHour-- }, onPlus = { if (endHour < 24) endHour++ })
            }
        },
        confirmButton = { TextButton(colors = dialogButtonColors(), onClick = { onSave(startHour, endHour) }) { Text("Save") } },
        dismissButton = { TextButton(colors = dialogButtonColors(), onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun HourStepper(label: String, hour: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        IconButton(onClick = onMinus) { Icon(Icons.Default.Remove, contentDescription = "Earlier") }
        Text(formatHourLabel(hour), fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onPlus) { Icon(Icons.Default.Add, contentDescription = "Later") }
    }
}

@Composable
fun AvailabilityDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val options = listOf("Available", "Busy", "Do not disturb")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlobalChatTheme.colors.cardBackground,
        titleContentColor = GlobalChatTheme.colors.textPrimary,
        textContentColor = GlobalChatTheme.colors.textPrimary,
        title = { Text("Availability") },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSave(option) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == current, onClick = { onSave(option) })
                        Text(option)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(colors = dialogButtonColors(), onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun TimezoneDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val colors = GlobalChatTheme.colors
    var query by remember { mutableStateOf("") }
    val deviceZone = remember { java.util.TimeZone.getDefault().id }
    val zones = remember {
        java.time.ZoneId.getAvailableZoneIds()
            .filter { it.contains('/') && !it.startsWith("Etc/") && !it.startsWith("SystemV/") }
            .sorted()
    }
    val matches = remember(query) {
        val q = query.trim().replace(' ', '_')
        if (q.isEmpty()) zones.take(40) else zones.filter { it.contains(q, ignoreCase = true) }.take(60)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlobalChatTheme.colors.cardBackground,
        titleContentColor = GlobalChatTheme.colors.textPrimary,
        textContentColor = GlobalChatTheme.colors.textPrimary,
        title = { Text("Your timezone") },
        text = {
            Column {
                Text(
                    "Use this phone's timezone ($deviceZone)",
                    color = colors.brandLime,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth().clickable { onSave(deviceZone) }.padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    label = { Text("Search city or region") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp).padding(top = 8.dp)) {
                    items(matches) { zone ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onSave(zone) }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                zone.replace('_', ' '),
                                modifier = Modifier.weight(1f),
                                fontWeight = if (zone == current) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(TimezoneEngine.formatCurrentTime(zone), fontSize = 12.sp, color = colors.textSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(colors = dialogButtonColors(), onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ReminderLeadDialog(current: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlobalChatTheme.colors.cardBackground,
        titleContentColor = GlobalChatTheme.colors.textPrimary,
        textContentColor = GlobalChatTheme.colors.textPrimary,
        title = { Text("Meeting reminders") },
        text = {
            Column {
                com.example.reminders.MeetingReminders.leadOptions.forEach { minutes ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSave(minutes) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = minutes == current, onClick = { onSave(minutes) })
                        Text(com.example.reminders.MeetingReminders.label(minutes))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(colors = dialogButtonColors(), onClick = onDismiss) { Text("Cancel") } }
    )
}
