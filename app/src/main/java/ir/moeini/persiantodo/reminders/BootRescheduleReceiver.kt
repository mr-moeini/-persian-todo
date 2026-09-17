package ir.moeini.persiantodo.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.moeini.persiantodo.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context.applicationContext)
                val snapshot = db.taskDao().getAll().first()
                snapshot.forEach { task ->
                    if (!task.isCompleted && (task.alarmEnabled || task.smsEnabled || task.callEnabled)) {
                        ReminderScheduler.schedule(context, task)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
