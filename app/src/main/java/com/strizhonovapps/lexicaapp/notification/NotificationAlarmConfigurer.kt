package com.strizhonovapps.lexicaapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.strizhonovapps.lexicaapp.ALARM_ID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NotificationAlarmConfigurer @Inject constructor() {

    fun setNotificationAlarm(context: Context) {
        val myIntent = Intent(context, NotificationJobReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                ALARM_ID,
                myIntent,
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
