package ir.moeini.persiantodo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.moeini.persiantodo.data.Task
import ir.moeini.persiantodo.data.TaskList
import ir.moeini.persiantodo.data.TaskRepository
import ir.moeini.persiantodo.util.JalaliDate
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val allTasks = repository.allTasks
    val myDayTasks = repository.myDayTasks
    val importantTasks = repository.importantTasks
    val plannedTasks = repository.plannedTasks
    val unassignedTasks = repository.unassignedTasks
    val completedTasks = repository.completedTasks
    val allLists = repository.allLists

    fun tasksForList(listId: Long) = repository.tasksForList(listId)

    fun addTask(
        title: String,
        note: String = "",
        listId: Long? = null,
        due: JalaliDate? = null,
        isMyDay: Boolean = false,
        isImportant: Boolean = false
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addTask(
                Task(
                    title = title.trim(),
                    note = note,
                    listId = listId,
                    dueJalaliYear = due?.year,
                    dueJalaliMonth = due?.month,
                    dueJalaliDay = due?.day,
                    isMyDay = isMyDay,
                    isImportant = isImportant,
                    createdAtEpochDay = LocalDate.now().toEpochDay()
                )
            )
        }
    }

    fun toggleCompleted(task: Task) = viewModelScope.launch {
        repository.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    fun toggleImportant(task: Task) = viewModelScope.launch {
        repository.updateTask(task.copy(isImportant = !task.isImportant))
    }

    fun toggleMyDay(task: Task) = viewModelScope.launch {
        repository.updateTask(task.copy(isMyDay = !task.isMyDay))
    }

    fun updateTask(task: Task) = viewModelScope.launch { repository.updateTask(task) }

    fun deleteTask(task: Task) = viewModelScope.launch { repository.deleteTask(task) }

    fun addList(name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repository.addList(TaskList(name = name.trim()))
    }

    fun deleteList(list: TaskList) = viewModelScope.launch { repository.deleteList(list) }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TaskViewModel(repository) as T
    }
}
