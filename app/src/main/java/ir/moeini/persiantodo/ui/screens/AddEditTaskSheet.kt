package ir.moeini.persiantodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onSave: (title: String, note: String, listId: Long?, due: JalaliDate?) -> Unit,
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
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            Spacer(Modifier.height(8.dp))

            AssistChip(
                onClick = { showDatePicker = true },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                label = { Text(due?.formatFull() ?: "تاریخ سررسید") }
            )

            if (due != null) {
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { due = null }) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("حذف تاریخ")
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

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onSave(title, note, listId, due) },
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
}
