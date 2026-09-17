package ir.moeini.persiantodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.moeini.persiantodo.util.JalaliDate
import ir.moeini.persiantodo.util.PersianCalendarUtils
import ir.moeini.persiantodo.util.toPersianDigits

/**
 * A lightweight Jalali (Shamsi) month-grid date picker, shown as a dialog.
 * Weeks start on Saturday (شنبه) per Iranian convention.
 */
@Composable
fun PersianDatePickerDialog(
    initial: JalaliDate = JalaliDate.today(),
    onDismiss: () -> Unit,
    onConfirm: (JalaliDate) -> Unit
) {
    var viewYear by remember { mutableIntStateOf(initial.year) }
    var viewMonth by remember { mutableIntStateOf(initial.month) }
    var selected by remember { mutableStateOf(initial) }

    // Saturday-first weekday order (شنبه یکشنبه دوشنبه سه‌شنبه چهارشنبه پنج‌شنبه جمعه)
    val weekdayHeaders = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("تأیید") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (viewMonth == 1) { viewMonth = 12; viewYear-- } else viewMonth--
                }) { Icon(Icons.Default.ChevronRight, contentDescription = "ماه قبل") }

                Text(
                    "${PersianCalendarUtils.monthNames[viewMonth - 1]} ${toPersianDigits(viewYear)}",
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = {
                    if (viewMonth == 12) { viewMonth = 1; viewYear++ } else viewMonth++
                }) { Icon(Icons.Default.ChevronLeft, contentDescription = "ماه بعد") }
            }
        },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdayHeaders.forEach { h ->
                        Text(
                            h,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                val firstOfMonth = JalaliDate(viewYear, viewMonth, 1)
                // weekdayName() gives ی/د/س/چ/پ/ج/ش style names; map to Saturday-first offset 0..6
                val gregForFirst = PersianCalendarUtils.toGregorian(firstOfMonth)
                val isoDow = gregForFirst.dayOfWeek.value // Mon=1..Sun=7
                // Saturday=6(iso) should be offset 0; Sunday=7 -> 1; Monday=1 -> 2 ... Friday=5 -> 6
                val offset = ((isoDow - 6) % 7 + 7) % 7

                val daysInMonth = PersianCalendarUtils.daysInMonth(viewYear, viewMonth)
                val cells = mutableListOf<Int?>()
                repeat(offset) { cells.add(null) }
                for (d in 1..daysInMonth) cells.add(d)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(cells.size) { idx ->
                        val day = cells[idx]
                        if (day == null) {
                            Box(Modifier.aspectRatio(1f))
                        } else {
                            val isSelected = selected.year == viewYear &&
                                selected.month == viewMonth && selected.day == day
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selected = JalaliDate(viewYear, viewMonth, day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    toPersianDigits(day),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
