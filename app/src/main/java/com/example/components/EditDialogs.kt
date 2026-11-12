package com.example.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Invoice
import com.example.model.Meeting
import com.example.model.getCurrencySymbol
import com.example.ui.theme.GlobalChatTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

@Composable
private fun dialogTextColors() = ButtonDefaults.textButtonColors(contentColor = GlobalChatTheme.colors.textPrimary)

/** Edit the title, amount, due date and notes of an unpaid invoice you issued. */
@Composable
fun EditInvoiceDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, dueDate: String, notes: String) -> Unit
) {
    val colors = GlobalChatTheme.colors
    val context = LocalContext.current
    var title by remember { mutableStateOf(invoice.title) }
    var amountText by remember { mutableStateOf(invoice.amount.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }) }
    var dueDate by remember { mutableStateOf(invoice.dueDate) }
    var notes by remember { mutableStateOf(invoice.notes.orEmpty()) }
    val amount = amountText.toDoubleOrNull()
    val valid = title.trim().length >= 2 && amount != null && amount > 0 && dueDate.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBackground,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textPrimary,
        title = { Text("Edit invoice ${invoice.invoiceNumber}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("Title") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_invoice_title")
                )
                OutlinedTextField(
                    value = amountText, onValueChange = { if (it.length <= 12) amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (${invoice.getCurrencySymbol()})") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("edit_invoice_amount")
                )
                OutlinedButton(
                    onClick = {
                        val start = runCatching { LocalDate.parse(dueDate, dateFormat) }.getOrDefault(LocalDate.now())
                        DatePickerDialog(context, { _, y, m, d ->
                            dueDate = LocalDate.of(y, m + 1, d).format(dateFormat)
                        }, start.year, start.monthValue - 1, start.dayOfMonth).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("edit_invoice_due")
                ) { Text("Due ${dueDate.ifBlank { "—" }}") }
                OutlinedTextField(
                    value = notes, onValueChange = { if (it.length <= 300) notes = it },
                    label = { Text("Notes") }, minLines = 2, maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), amount ?: 0.0, dueDate, notes.trim()) },
                enabled = valid, colors = dialogTextColors(),
                modifier = Modifier.testTag("edit_invoice_save")
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss, colors = dialogTextColors()) { Text("Cancel") } }
    )
}

/** Edit title, notes, date, time and length of a meeting you organise. */
@Composable
fun EditMeetingDialog(
    meeting: Meeting,
    onDismiss: () -> Unit,
    onSave: (title: String, notes: String, scheduledAtMillis: Long, durationMinutes: Int) -> Unit
) {
    val colors = GlobalChatTheme.colors
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    val initialMillis = meeting.scheduledAt?.toDate()?.time ?: (System.currentTimeMillis() + 3_600_000L)
    var title by remember { mutableStateOf(meeting.title) }
    var notes by remember { mutableStateOf(meeting.notes.orEmpty()) }
    var millis by remember { mutableLongStateOf(initialMillis) }
    var duration by remember { mutableIntStateOf(meeting.durationMinutes) }
    val start = Instant.ofEpochMilli(millis).atZone(zone)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBackground,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textPrimary,
        title = { Text("Edit meeting") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("Title") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_meeting_title")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                millis = start.withYear(y).withMonth(m + 1).withDayOfMonth(d).toInstant().toEpochMilli()
                            }, start.year, start.monthValue - 1, start.dayOfMonth).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                        modifier = Modifier.weight(1f).testTag("edit_meeting_date")
                    ) { Text(start.format(dateFormat), fontSize = 13.sp) }
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, h, min ->
                                millis = start.withHour(h).withMinute(min).withSecond(0).toInstant().toEpochMilli()
                            }, start.hour, start.minute, false).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                        modifier = Modifier.weight(1f).testTag("edit_meeting_time")
                    ) { Text(start.format(timeFormat), fontSize = 13.sp) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(15, 30, 45, 60).forEach { minutes ->
                        FilterChip(
                            selected = duration == minutes,
                            onClick = { duration = minutes },
                            label = { Text("$minutes min", fontSize = 12.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes, onValueChange = { if (it.length <= 300) notes = it },
                    label = { Text("Notes") }, minLines = 2, maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), notes.trim(), millis, duration) },
                enabled = title.trim().length >= 2, colors = dialogTextColors(),
                modifier = Modifier.testTag("edit_meeting_save")
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss, colors = dialogTextColors()) { Text("Cancel") } }
    )
}

/** Asks before something is called off. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = GlobalChatTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.cardBackground,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = com.example.ui.theme.SentimentNegative),
                modifier = Modifier.testTag("confirm_dialog_confirm")
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss, colors = dialogTextColors()) { Text("Keep it") } }
    )
}
