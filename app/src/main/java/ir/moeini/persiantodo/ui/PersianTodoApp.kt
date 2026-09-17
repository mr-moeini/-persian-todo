package ir.moeini.persiantodo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.moeini.persiantodo.R
import ir.moeini.persiantodo.data.Task
import ir.moeini.persiantodo.data.TaskList
import ir.moeini.persiantodo.ui.screens.AddEditTaskSheet
import ir.moeini.persiantodo.ui.screens.TaskRow
import ir.moeini.persiantodo.util.JalaliDate
import ir.moeini.persiantodo.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

private sealed class ScreenDest(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object MyDay : ScreenDest("روز من", Icons.Default.WbSunny)
    object Important : ScreenDest("مهم", Icons.Default.Star)
    object Planned : ScreenDest("برنامه‌ریزی‌شده", Icons.Default.CalendarMonth)
    object Tasks : ScreenDest("وظایف", Icons.Default.CheckCircle)
    object Completed : ScreenDest("انجام‌شده", Icons.Default.Done)
    data class CustomList(val list: TaskList) : ScreenDest(list.name, Icons.Default.List)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersianTodoApp(viewModel: TaskViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentDest by remember { mutableStateOf<ScreenDest>(ScreenDest.MyDay) }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showNewListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    val lists by viewModel.allLists.collectAsState(initial = emptyList())

    val tasksFlow = when (val d = currentDest) {
        ScreenDest.MyDay -> viewModel.myDayTasks
        ScreenDest.Important -> viewModel.importantTasks
        ScreenDest.Planned -> viewModel.plannedTasks
        ScreenDest.Tasks -> viewModel.unassignedTasks
        ScreenDest.Completed -> viewModel.completedTasks
        is ScreenDest.CustomList -> viewModel.tasksForList(d.list.id)
    }
    val tasks by tasksFlow.collectAsState(initial = emptyList())

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))

                listOf(
                    ScreenDest.MyDay, ScreenDest.Important,
                    ScreenDest.Planned, ScreenDest.Tasks
                ).forEach { dest ->
                    NavigationDrawerItem(
                        icon = { Icon(dest.icon, contentDescription = null) },
                        label = { Text(dest.label) },
                        selected = currentDest == dest,
                        onClick = {
                            currentDest = dest
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                lists.forEach { l ->
                    val dest = ScreenDest.CustomList(l)
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text(l.name) },
                        selected = currentDest.let { it is ScreenDest.CustomList && it.list.id == l.id },
                        onClick = {
                            currentDest = dest
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text(stringResource(R.string.new_list)) },
                    selected = false,
                    onClick = { showNewListDialog = true },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(ScreenDest.Completed.icon, contentDescription = null) },
                    label = { Text(ScreenDest.Completed.label) },
                    selected = currentDest == ScreenDest.Completed,
                    onClick = {
                        currentDest = ScreenDest.Completed
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentDest.label) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "منو")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    editingTask = null
                    showAddSheet = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task_hint))
                }
            }
        ) { padding ->
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(stringResource(R.string.empty_list), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onToggleCompleted = { viewModel.toggleCompleted(task) },
                            onToggleImportant = { viewModel.toggleImportant(task) },
                            onClick = {
                                editingTask = task
                                showAddSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddEditTaskSheet(
            existing = editingTask,
            lists = lists,
            onDismiss = { showAddSheet = false },
            onSave = { titleText, note, listId, due ->
                val existing = editingTask
                if (existing == null) {
                    val isMyDay = currentDest == ScreenDest.MyDay
                    val isImportant = currentDest == ScreenDest.Important
                    viewModel.addTask(
                        title = titleText, note = note, listId = listId, due = due,
                        isMyDay = isMyDay, isImportant = isImportant
                    )
                } else {
                    viewModel.updateTask(
                        existing.copy(
                            title = titleText.trim(),
                            note = note,
                            listId = listId,
                            dueJalaliYear = due?.year,
                            dueJalaliMonth = due?.month,
                            dueJalaliDay = due?.day
                        )
                    )
                }
                showAddSheet = false
            },
            onDelete = editingTask?.let { t -> { viewModel.deleteTask(t); showAddSheet = false } }
        )
    }

    if (showNewListDialog) {
        AlertDialog(
            onDismissRequest = { showNewListDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addList(newListName)
                    newListName = ""
                    showNewListDialog = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showNewListDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
            title = { Text(stringResource(R.string.new_list)) },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text(stringResource(R.string.list_name_hint)) },
                    singleLine = true
                )
            }
        )
    }
}
