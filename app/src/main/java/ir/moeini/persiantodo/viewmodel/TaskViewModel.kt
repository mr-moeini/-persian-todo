package ir.moeini.persiantodo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.moeini.persiantodo.data.Task
import ir.moeini.persiantodo.data.TaskList
import ir.moeini.persiantodo.data.TaskRepository
import ir.moeini.persiantodo.reminders.ReminderScheduler
import ir.moeini.persiantodo.util.JalaliDate
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskViewModel(
    private val repository: TaskRepository,
    private val appContext: Context
) : ViewModel() {

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
        dueHour: Int? = null,
        dueMinute: Int? = null,
        alarmEnabled: Boolean = false,
        smsEnabled: Boolean = false,
        smsPhoneNumber: String? = null,
        smsText: String? = null,
        callEnabled: Boolean = false,
        callPhoneNumber: String? = null,
        callText: String? = null,
        isMyDay: Boolean = false,
        isImportant: Boolean = false
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                note = note,
                listId = listId,
                dueJalaliYear = due?.year,
                dueJalaliMonth = due?.month,
                dueJalaliDay = due?.day,
                dueHour = dueHour,
                dueMinute = dueMinute,
                alarmEnabled = alarmEnabled,
                smsEnabled = smsEnabled,
                smsPhoneNumber = smsPhoneNumber,
                smsText = smsText,
                callEnabled = callEnabled,
                callPhoneNumber = callPhoneNumber,
                callText = callText,
                isMyDay = isMyDay,
                isImportant = isImportant,
                createdAtEpochDay = LocalDate.now().toEpochDay()
            )
            val newId = repository.addTask(task)
            ReminderScheduler.schedule(appContext, task.copy(id = newId))
        }
    }

    fun toggleCompleted(task: Task) = viewModelScope.launch {
        val updated = task.copy(isCompleted = !task.isCompleted)
        repository.updateTask(updated)
        if (updated.isCompleted) ReminderScheduler.cancel(appContext, updated)
        else ReminderScheduler.schedule(appContext, updated)
    }

    fun toggleImportant(task: Task) = viewModelScope.launch {
        repository.updateTask(task.copy(isImportant = !task.isImportant))
    }

    fun toggleMyDay(task: Task) = viewModelScope.launch {
        repository.updateTask(task.copy(isMyDay = !task.isMyDay))
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
        ReminderScheduler.schedule(appContext, task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        ReminderScheduler.cancel(appContext, task)
        repository.deleteTask(task)
    }

    fun addList(name: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repository.addList(TaskList(name = name.trim()))
    }

    fun deleteList(list: TaskList) = viewModelScope.launch { repository.deleteList(list) }

    class Factory(
        private val repository: TaskRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TaskViewModel(repository, appContext) as T
    }
}
