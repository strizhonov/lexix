package com.strizhonovapps.lexixapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import javax.inject.Inject
import javax.inject.Singleton

private const val ALARM_ID = 1000
internal const val INTENT_ACTION = "com.strizhonovapps.lexixapp.NOTIFY"

@Singleton
class NotificationAlarmConfigurer @Inject constructor() {

    fun setNotificationAlarm(context: Context) {
        val notificationIntent = Intent(context, NotificationJobReceiver::class.java)
        notificationIntent.action = INTENT_ACTION
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                ALARM_ID,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            AlarmManager.INTERVAL_HOUR,
            AlarmManager.INTERVAL_HOUR,
            pendingIntent
        )
    }
}