package ir.moeini.persiantodo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class PersianTodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "یادآوری وظایف",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان یادآوری برای وظایف دارای زمان یادآوری"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "task_reminders"
    }
}
