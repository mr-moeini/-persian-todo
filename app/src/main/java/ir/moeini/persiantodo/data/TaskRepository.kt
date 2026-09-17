package ir.moeini.persiantodo.data

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskListDao: TaskListDao
) {
    val allTasks = taskDao.getAll()
    val myDayTasks = taskDao.getMyDay()
    val importantTasks = taskDao.getImportant()
    val plannedTasks = taskDao.getPlanned()
    val unassignedTasks = taskDao.getUnassignedTasks()
    val completedTasks = taskDao.getCompleted()
    val allLists = taskListDao.getAll()

    fun tasksForList(listId: Long) = taskDao.getTasksForList(listId)

    suspend fun addTask(task: Task) = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(task: Task) = taskDao.delete(task)

    suspend fun addList(list: TaskList) = taskListDao.insert(list)
    suspend fun updateList(list: TaskList) = taskListDao.update(list)
    suspend fun deleteList(list: TaskList) = taskListDao.delete(list)
}
