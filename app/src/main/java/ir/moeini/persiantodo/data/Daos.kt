package ir.moeini.persiantodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAtEpochDay DESC")
    fun getAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isMyDay = 1 AND isCompleted = 0 ORDER BY createdAtEpochDay DESC")
    fun getMyDay(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isImportant = 1 AND isCompleted = 0 ORDER BY createdAtEpochDay DESC")
    fun getImportant(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueJalaliYear IS NOT NULL AND isCompleted = 0 ORDER BY dueJalaliYear, dueJalaliMonth, dueJalaliDay")
    fun getPlanned(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND listId IS NULL ORDER BY createdAtEpochDay DESC")
    fun getUnassignedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE listId = :listId AND isCompleted = 0 ORDER BY createdAtEpochDay DESC")
    fun getTasksForList(listId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY createdAtEpochDay DESC")
    fun getCompleted(): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists ORDER BY orderIndex ASC")
    fun getAll(): Flow<List<TaskList>>

    @Insert
    suspend fun insert(list: TaskList): Long

    @Update
    suspend fun update(list: TaskList)

    @Delete
    suspend fun delete(list: TaskList)
}
