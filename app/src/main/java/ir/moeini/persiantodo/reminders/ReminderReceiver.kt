package ir.moeini.persiantodo.reminders

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import ir.moeini.persiantodo.MainActivity
import ir.moeini.persiantodo.PersianTodoApplication
import ir.moeini.persiantodo.R
import android.telephony.SmsManager

/**
 * Fires when a task's scheduled reminder time arrives. Shows a system notification,
 * and — if the user opted in per-task — sends an SMS and/or starts an outgoing call.
 *
 * Note on the "call" feature: Android does not allow third-party apps to inject
 * synthesized audio into a live cellular call (this is intentionally restricted
 * platform-wide to prevent robocall abuse). What we CAN do is auto-dial the number
 * and show the user's script on screen so they can read it — true automated
 * text-to-speech-to-callee requires a cloud voice service (e.g. Twilio Voice).
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "یادآوری وظیفه"
        val note = intent.getStringExtra(EXTRA_NOTE).orEmpty()

        showNotification(context, taskId, title, note)

        val smsEnabled = intent.getBooleanExtra(EXTRA_SMS_ENABLED, false)
        if (smsEnabled) {
            val phone = intent.getStringExtra(EXTRA_SMS_PHONE)
            val text = intent.getStringExtra(EXTRA_SMS_TEXT)
            if (!phone.isNullOrBlank() && !text.isNullOrBlank()) {
                sendSms(context, phone, text)
            }
        }

        val callEnabled = intent.getBooleanExtra(EXTRA_CALL_ENABLED, false)
        if (callEnabled) {
            val phone = intent.getStringExtra(EXTRA_CALL_PHONE)
            val callText = intent.getStringExtra(EXTRA_CALL_TEXT)
            if (!phone.isNullOrBlank()) {
                startCall(context, taskId, title, phone, callText.orEmpty())
            }
        }
    }

    private fun showNotification(context: Context, taskId: Long, title: String, note: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, taskId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, PersianTodoApplication.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(note.ifBlank { "زمان انجام این وظیفه رسیده است" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(taskId.toInt(), builder.build())
        }
    }

    private fun sendSms(context: Context, phone: String, text: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) return
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(text)
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
        } catch (_: Exception) {
            // Swallow — device may not support cellular SMS (e.g. tablet-only, no SIM).
        }
    }

    private fun startCall(context: Context, taskId: Long, taskTitle: String, phone: String, script: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) return
        // Also surface the script as a notification, since the OS call screen will
        // cover the app — the user needs the text visible to read aloud during the call.
        if (script.isNotBlank()) {
            val builder = NotificationCompat.Builder(context, PersianTodoApplication.REMINDER_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setContentTitle("متن تماس: $taskTitle")
                .setContentText(script)
                .setStyle(NotificationCompat.BigTextStyle().bigText(script))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify((taskId.toInt() * -1), builder.build())
            }
        }
        try {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(callIntent)
        } catch (_: Exception) {
            // No telephony (tablet/emulator without a SIM) — nothing more we can do.
        }
    }
}
