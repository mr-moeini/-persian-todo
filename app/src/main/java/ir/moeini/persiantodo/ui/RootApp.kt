package ir.moeini.persiantodo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ir.moeini.persiantodo.ui.screens.CalendarScreen
import ir.moeini.persiantodo.viewmodel.TaskViewModel

private enum class RootTab(val label: String) { TASKS("وظایف"), CALENDAR("تقویم") }

@Composable
fun RootApp(viewModel: TaskViewModel) {
    var tab by remember { mutableStateOf(RootTab.TASKS) }
    val allTasks by viewModel.allTasks.collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == RootTab.TASKS,
                    onClick = { tab = RootTab.TASKS },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text(RootTab.TASKS.label) }
                )
                NavigationBarItem(
                    selected = tab == RootTab.CALENDAR,
                    onClick = { tab = RootTab.CALENDAR },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text(RootTab.CALENDAR.label) }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                RootTab.TASKS -> PersianTodoApp(viewModel)
                RootTab.CALENDAR -> CalendarScreen(
                    tasks = allTasks,
                    onTaskClick = { tab = RootTab.TASKS } // switch back to Tasks tab to edit it there
                )
            }
        }
    }
}
