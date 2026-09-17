package ir.moeini.persiantodo.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.moeini.persiantodo.data.Task
import ir.moeini.persiantodo.data.TaskList
import ir.moeini.persiantodo.util.JalaliDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskSheet(
    existing: Task?,
    lists: List<TaskList>,
    onDismiss: () -> Unit,
    onSave: (
        title: String, note: String, listId: Long?, due: JalaliDate?,
        dueHour: Int?, dueMinute: Int?, alarmEnabled: Boolean,
        smsEnabled: Boolean, smsPhone: String?, smsText: String?,
        callEnabled: Boolean, callPhone: String?, callText: String?
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var listId by remember { mutableStateOf(existing?.listId) }
    var due by remember {
        mutableStateOf(
            if (existing?.hasDueDate == true)
                JalaliDate(existing.dueJalaliYear!!, existing.dueJalaliMonth!!, existing.dueJalaliDay!!)
            else null
        )
    }
    var dueHour by remember { mutableStateOf(existing?.dueHour) }
    var dueMinute by remember { mutableStateOf(existing?.dueMinute) }
    var alarmEnabled by remember { mutableStateOf(existing?.alarmEnabled ?: false) }

    var smsEnabled by remember { mutableStateOf(existing?.smsEnabled ?: false) }
    var smsPhone by remember { mutableStateOf(existing?.smsPhoneNumber ?: "") }
    var smsText by remember { mutableStateOf(existing?.smsText ?: "") }

    var callEnabled by remember { mutableStateOf(existing?.callEnabled ?: false) }
    var callPhone by remember { mutableStateOf(existing?.callPhoneNumber ?: "") }
    var callText by remember { mutableStateOf(existing?.callText ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.POST_NOTIFICATIONS else null
    val requestNotifPermission = notificationPermission?.let {
        rememberPermissionRequester(it) { granted -> if (!granted) alarmEnabled = false }
    }
    val requestSmsPermission = rememberPermissionRequester(Manifest.permission.SEND_SMS) { granted ->
        if (!granted) smsEnabled = false
    }
    val requestCallPermission = rememberPermissionRequester(Manifest.permission.CALL_PHONE) { granted ->
        if (!granted) callEnabled = false
    }

    val pickSmsContact = rememberContactPhonePicker { smsPhone = it }
    val pickCallContact = rememberContactPhonePicker { callPhone = it }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (existing == null) "وظیفهٔ جدید" else "ویرایش وظیفه",
                    style = MaterialTheme.typography.titleLarge
                )
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("یادداشت") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(12.dp))
            Text("زمان‌بندی", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))

            Row {
                AssistChip(
                    onClick = { showDatePicker = true },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text(due?.formatFull() ?: "تاریخ سررسید") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                if (due != null) {
                    AssistChip(
                        onClick = { showTimePicker = true },
                        label = {
                            Text(
                                if (dueHour != null && dueMinute != null)
                                    formatHourMinute(dueHour!!, dueMinute!!)
                                else "انتخاب ساعت"
                            )
                        }
                    )
                }
            }

            if (due != null) {
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { due = null; dueHour = null; dueMinute = null; alarmEnabled = false }) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("حذف تاریخ و ساعت")
                }
            }

            if (due != null && dueHour != null && dueMinute != null) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("یادآوری با اعلان", modifier = Modifier.weight(1f))
                    Switch(
                        checked = alarmEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                alarmEnabled = true
                                requestNotifPermission?.invoke()
                            } else alarmEnabled = false
                        }
                    )
                }
            }

            if (lists.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("فهرست", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = listId == null,
                        onClick = { listId = null },
                        label = { Text("بدون فهرست") },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    lists.forEach { l ->
                        FilterChip(
                            selected = listId == l.id,
                            onClick = { listId = l.id },
                            label = { Text(l.name) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- SMS reminder section ---
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Sms, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ارسال پیامک در این زمان", modifier = Modifier.weight(1f))
                Switch(
                    checked = smsEnabled,
                    onCheckedChange = { checked ->
                        if (checked && due != null && dueHour != null) {
                            smsEnabled = true
                            requestSmsPermission()
                        } else if (checked) {
                            // no due date/time yet — nothing to schedule against
                        } else smsEnabled = false
                    }
                )
            }
            if (smsEnabled) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = smsPhone,
                        onValueChange = { smsPhone = it },
                        label = { Text("شماره تلفن گیرنده") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = pickSmsContact) {
                        Icon(Icons.Default.Contacts, contentDescription = "انتخاب از مخاطبین")
                    }
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = smsText,
                    onValueChange = { smsText = it },
                    label = { Text("متن پیامک") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            Spacer(Modifier.height(12.dp))

            // --- Call / IVR-style section ---
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("تماس تلفنی در این زمان", modifier = Modifier.weight(1f))
                Switch(
                    checked = callEnabled,
                    onCheckedChange = { checked ->
                        if (checked && due != null && dueHour != null) {
                            callEnabled = true
                            requestCallPermission()
                        } else if (checked) {
                            // no due date/time yet
                        } else callEnabled = false
                    }
                )
            }
            if (callEnabled) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "توجه: اندروید اجازه پخش خودکار صدای ضبط‌شده در تماس واقعی را به برنامه‌های عادی نمی‌دهد. " +
                        "این ویژگی به‌صورت خودکار با شماره موردنظر تماس می‌گیرد و متن شما را روی صفحه نشان می‌دهد تا بخوانید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = callPhone,
                        onValueChange = { callPhone = it },
                        label = { Text("شماره تلفن گیرنده") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = pickCallContact) {
                        Icon(Icons.Default.Contacts, contentDescription = "انتخاب از مخاطبین")
                    }
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = callText,
                    onValueChange = { callText = it },
                    label = { Text("متنی که باید در تماس گفته شود") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(
                        title, note, listId, due, dueHour, dueMinute, alarmEnabled,
                        smsEnabled, smsPhone.ifBlank { null }, smsText.ifBlank { null },
                        callEnabled, callPhone.ifBlank { null }, callText.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("ذخیره")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        PersianDatePickerDialog(
            initial = due ?: JalaliDate.today(),
            onDismiss = { showDatePicker = false },
            onConfirm = { due = it; showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = dueHour ?: 9,
            initialMinute = dueMinute ?: 0,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m -> dueHour = h; dueMinute = m; showTimePicker = false }
        )
    }
}
