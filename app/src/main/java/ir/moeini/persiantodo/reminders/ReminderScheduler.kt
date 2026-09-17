package ir.moeini.persiantodo.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.moeini.persiantodo.data.Task
import ir.moeini.persiantodo.util.JalaliDate

const val EXTRA_TASK_ID = "extra_task_id"
const val EXTRA_TITLE = "extra_title"
const val EXTRA_NOTE = "extra_note"
const val EXTRA_SMS_ENABLED = "extra_sms_enabled"
const val EXTRA_SMS_PHONE = "extra_sms_phone"
const val EXTRA_SMS_TEXT = "extra_sms_text"
const val EXTRA_CALL_ENABLED = "extra_call_enabled"
const val EXTRA_CALL_PHONE = "extra_call_phone"
const val EXTRA_CALL_TEXT = "extra_call_text"

object ReminderScheduler {

    private fun pendingIntentFor(context: Context, task: Task): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TITLE, task.title)
            putExtra(EXTRA_NOTE, task.note)
            putExtra(EXTRA_SMS_ENABLED, task.smsEnabled)
            putExtra(EXTRA_SMS_PHONE, task.smsPhoneNumber)
            putExtra(EXTRA_SMS_TEXT, task.smsText)
            putExtra(EXTRA_CALL_ENABLED, task.callEnabled)
            putExtra(EXTRA_CALL_PHONE, task.callPhoneNumber)
            putExtra(EXTRA_CALL_TEXT, task.callText)
        }
        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Schedules (or reschedules) the alarm for this task, if it has a due date+time and alarmEnabled/smsEnabled/callEnabled. */
    fun schedule(context: Context, task: Task) {
        val needsAlarm = task.alarmEnabled || task.smsEnabled || task.callEnabled
        if (!needsAlarm || !task.hasDueDate || !task.hasDueTime || task.isCompleted) {
            cancel(context, task)
            return
        }
        val triggerAt = JalaliDate.epochMillisAt(
            JalaliDate(task.dueJalaliYear!!, task.dueJalaliMonth!!, task.dueJalaliDay!!),
            task.dueHour!!, task.dueMinute!!
        )
        if (triggerAt <= System.currentTimeMillis()) return // don't schedule in the past

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntentFor(context, task)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            // Exact-alarm permission was revoked; fall back to an inexact alarm rather than crash.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntentFor(context, task))
    }
}
