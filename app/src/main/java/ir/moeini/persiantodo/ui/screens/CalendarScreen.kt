package ir.moeini.persiantodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.moeini.persiantodo.data.Task
import ir.moeini.persiantodo.util.JalaliDate
import ir.moeini.persiantodo.util.PersianCalendarUtils
import ir.moeini.persiantodo.util.toPersianDigits

private enum class CalendarView(val label: String) {
    TODAY("امروز"), WEEK("۷ روز"), MONTH("ماه")
}

@Composable
fun CalendarScreen(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit
) {
    var view by remember { mutableStateOf(CalendarView.TODAY) }
    val today = remember { JalaliDate.today() }

    val tasksByDate = remember(tasks) {
        tasks.filter { it.hasDueDate }
            .groupBy { JalaliDate(it.dueJalaliYear!!, it.dueJalaliMonth!!, it.dueJalaliDay!!) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(3.dp)
        ) {
            CalendarView.entries.forEach { v ->
                val selected = v == view
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { view = v }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        v.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (view) {
            CalendarView.TODAY -> DayAgenda(date = today, tasks = tasksByDate[today].orEmpty(), onTaskClick = onTaskClick)
            CalendarView.WEEK -> WeekAgenda(startDate = today, tasksByDate = tasksByDate, onTaskClick = onTaskClick)
            CalendarView.MONTH -> MonthGrid(anchor = today, tasksByDate = tasksByDate, onTaskClick = onTaskClick)
        }
    }
}

@Composable
private fun DayAgenda(date: JalaliDate, tasks: List<Task>, onTaskClick: (Task) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "${date.weekdayName()} ${date.formatFull()}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        if (tasks.isEmpty()) {
            Text("کاری برای این روز ثبت نشده", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(tasks, key = { it.id }) { task -> DayTaskChip(task, onTaskClick) }
            }
        }
    }
}

@Composable
private fun WeekAgenda(
    startDate: JalaliDate,
    tasksByDate: Map<JalaliDate, List<Task>>,
    onTaskClick: (Task) -> Unit
) {
    val days = remember(startDate) {
        (0..6).map { offset -> addDays(startDate, offset) }
    }
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(days) { day ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    "${day.weekdayName()} ${toPersianDigits(day.day)} ${day.monthName()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                val dayTasks = tasksByDate[day].orEmpty()
                if (dayTasks.isEmpty()) {
                    Text(
                        "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    dayTasks.forEach { task -> DayTaskChip(task, onTaskClick) }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun MonthGrid(
    anchor: JalaliDate,
    tasksByDate: Map<JalaliDate, List<Task>>,
    onTaskClick: (Task) -> Unit
) {
    var year by remember { mutableIntStateOf(anchor.year) }
    var month by remember { mutableIntStateOf(anchor.month) }
    var selectedDay by remember { mutableStateOf<JalaliDate?>(null) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (month == 1) { month = 12; year-- } else month--
            }) { Text("<") }

            Text(
                "${PersianCalendarUtils.monthNames[month - 1]} ${toPersianDigits(year)}",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = {
                if (month == 12) { month = 1; year++ } else month++
            }) { Text(">") }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { h ->
                Text(h, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
            }
        }

        val firstOfMonth = JalaliDate(year, month, 1)
        val gregForFirst = PersianCalendarUtils.toGregorian(firstOfMonth)
        val isoDow = gregForFirst.dayOfWeek.value
        val offset = ((isoDow - 6) % 7 + 7) % 7
        val daysInMonth = PersianCalendarUtils.daysInMonth(year, month)
        val cells = mutableListOf<JalaliDate?>()
        repeat(offset) { cells.add(null) }
        for (d in 1..daysInMonth) cells.add(JalaliDate(year, month, d))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.padding(horizontal = 8.dp).height(280.dp)
        ) {
            items(cells.size) { idx ->
                val date = cells[idx]
                if (date == null) {
                    Box(Modifier.aspectRatio(0.9f))
                } else {
                    val dayTasks = tasksByDate[date].orEmpty()
                    val isToday = date == anchor
                    Column(
                        modifier = Modifier
                            .aspectRatio(0.9f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { selectedDay = date }
                            .padding(2.dp)
                    ) {
                        Text(
                            toPersianDigits(date.day),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        dayTasks.take(2).forEach { task ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                        if (dayTasks.size > 2) {
                            Text("+${toPersianDigits(dayTasks.size - 2)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        selectedDay?.let { day ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DayAgenda(date = day, tasks = tasksByDate[day].orEmpty(), onTaskClick = onTaskClick)
        }
    }
}

@Composable
private fun DayTaskChip(task: Task, onTaskClick: (Task) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onTaskClick(task) }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(task.title, style = MaterialTheme.typography.bodyLarge)
            if (task.hasDueTime) {
                Text(
                    formatHourMinute(task.dueHour!!, task.dueMinute!!),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun addDays(date: JalaliDate, days: Int): JalaliDate {
    val g = PersianCalendarUtils.toGregorian(date).plusDays(days.toLong())
    return PersianCalendarUtils.toJalali(g)
}
