package ir.moeini.persiantodo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A user-created list (e.g. "خرید", "کار", "پروژه‌ها"). */
@Entity(tableName = "task_lists")
data class TaskList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#3F51B5",
    val orderIndex: Int = 0
)

/**
 * A single todo task.
 * dueJalaliYear/Month/Day store the due date directly in the Jalali calendar so the
 * app never has to reconvert stored dates — the Gregorian LocalDate is derived on demand
 * only when needed (e.g. for sorting or notifications).
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val isCompleted: Boolean = false,
    val isImportant: Boolean = false,
    val isMyDay: Boolean = false,
    val listId: Long? = null,
    val dueJalaliYear: Int? = null,
    val dueJalaliMonth: Int? = null,
    val dueJalaliDay: Int? = null,
    val createdAtEpochDay: Long = 0,
    val repeatRule: String? = null // null = no repeat; e.g. "DAILY", "WEEKLY", "MONTHLY"
) {
    val hasDueDate: Boolean get() = dueJalaliYear != null && dueJalaliMonth != null && dueJalaliDay != null
}
