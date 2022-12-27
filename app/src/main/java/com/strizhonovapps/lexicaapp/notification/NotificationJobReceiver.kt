package com.strizhonovapps.lexicaapp.notification

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.strizhonovapps.lexicaapp.*
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.service.WordService
import com.strizhonovapps.lexicaapp.servicehelper.isFirstDayAfterSecondDay
import com.strizhonovapps.lexicaapp.view.MainActivity
import java.util.*
import javax.inject.Inject

const val ACTIVE_WORDS_FOR_NOTIFICATION = 10
const val NOTIFICATION_LOWER_HOUR_EXCLUSIVE = 8
const val NOTIFICATION_UPPER_HOUR_EXCLUSIVE = 22

class NotificationJobReceiver : BroadcastReceiver() {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(context: Context, intent: Intent?) {
        DiComponentFactory.initIfNecessary(context)
        DiComponentFactory.getInstance().inject(this)

        configureNotificationsIfNeeded(context)
        notifyItsTimeToLearnIfNecessary(context)
    }

    private fun notifyItsTimeToLearnIfNecessary(context: Context) {
        val availableWords = wordService.getCountOfReadyForTrainingWords()
        if (isNotificationCanBeSent(availableWords, context)) {
            with(NotificationManagerCompat.from(context)) {
                notify(
                    System.currentTimeMillis().toInt(),
                    getNotificationBuilder(availableWords, context).build()
                )
            }

            sharedPreferences.edit().apply {
                putLong(
                    LAST_NOTIFIED_ABOUT_ACTIVE_WORDS_PREFS_KEY,
                    Calendar.getInstance().timeInMillis
                )
                putBoolean(
                    LAST_SESSION_FLAG_PREFS_KEY,
                    false
                )
                apply()
            }
        }
    }

    @VisibleForTesting
    fun isNotificationCanBeSent(availableWords: Int, context: Context): Boolean {
        val areEnoughWords = availableWords >= ACTIVE_WORDS_FOR_NOTIFICATION
        if (!areEnoughWords) return false

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isHourSuitRequirements =
            hour in (NOTIFICATION_LOWER_HOUR_EXCLUSIVE + 1) until NOTIFICATION_UPPER_HOUR_EXCLUSIVE
        if (!isHourSuitRequirements) return false
        val appIsOnForeground = isOnForeground(context)
        if (appIsOnForeground) return false

        return thereWasNoNotificationsTodayOrAfterLastSessionEnd()
    }

    private fun thereWasNoNotificationsTodayOrAfterLastSessionEnd(): Boolean {
        if (!sharedPreferences.contains(LAST_NOTIFIED_ABOUT_ACTIVE_WORDS_PREFS_KEY)) return true
        val lastNotificationDate = Calendar.getInstance().apply {
            timeInMillis =
                sharedPreferences.getLong(LAST_NOTIFIED_ABOUT_ACTIVE_WORDS_PREFS_KEY, 0L)
        }
        val today = Calendar.getInstance()
        val isTodayAfterLastNotificationDay = isFirstDayAfterSecondDay(today, lastNotificationDate)
        return isTodayAfterLastNotificationDay or sharedPreferences.getBoolean(
            LAST_SESSION_FLAG_PREFS_KEY,
            false
        )
    }

    private fun isOnForeground(context: Context): Boolean {
        return (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .runningAppProcesses
            .firstOrNull { process -> process.uid == context.applicationInfo.uid }
            ?.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    private fun getNotificationBuilder(
        wordsAvailable: Int,
        context: Context
    ): NotificationCompat.Builder {
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                ACTIVITY_FROM_NOTIFICATION_REQ_CODE,
                Intent(context, MainActivity::class.java),
                FLAG_IMMUTABLE
            )
        return NotificationCompat.Builder(context, HIGH_PRIORITY_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setContentTitle(context.getString(R.string.notification_title_template))
            .setContentText(
                String.format(
                    context.getString(R.string.notification_text_template),
                    wordsAvailable
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
    }

    private fun configureNotificationsIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.getNotificationChannel(HIGH_PRIORITY_NOTIFICATION_CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                HIGH_PRIORITY_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = context.getString(R.string.notification_channel_description)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

