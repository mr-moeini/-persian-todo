package ir.moeini.persiantodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.moeini.persiantodo.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("تأیید") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
        title = { Text("انتخاب ساعت") },
        text = {
            Box(modifier = Modifier.padding(8.dp)) {
                TimePicker(state = state)
            }
        }
    )
}

fun formatHourMinute(hour: Int, minute: Int): String =
    "${toPersianDigits(hour.toString().padStart(2, '0'))}:${toPersianDigits(minute.toString().padStart(2, '0'))}"
