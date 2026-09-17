package ir.moeini.persiantodo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ir.moeini.persiantodo.data.Task
import ir.moeini.persiantodo.util.JalaliDate

@Composable
fun TaskRow(
    task: Task,
    onToggleCompleted: () -> Unit,
    onToggleImportant: () -> Unit,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleCompleted() })
        },
        headlineContent = {
            Text(
                task.title,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
        },
        supportingContent = if (task.hasDueDate) {
            {
                val due = JalaliDate(task.dueJalaliYear!!, task.dueJalaliMonth!!, task.dueJalaliDay!!)
                Text(due.formatFull(), style = MaterialTheme.typography.bodyMedium)
            }
        } else null,
        trailingContent = {
            IconButton(onClick = onToggleImportant) {
                Icon(
                    imageVector = if (task.isImportant) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "مهم"
                )
            }
        }
    )
}
