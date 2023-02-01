package com.strizhonovapps.lexixapp.notification

import android.Manifest
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.service.WordService
import com.strizhonovapps.lexixapp.util.PrefsDecorator
import com.strizhonovapps.lexixapp.util.now
import com.strizhonovapps.lexixapp.util.toLocalDateTime
import com.strizhonovapps.lexixapp.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

private const val ACTIVE_WORDS_FOR_NOTIFICATION = 15

// TODO to prefs
private const val NOTIFICATION_LOWER_HOUR = 8
private const val NOTIFICATION_LOWER_MINUTE = 0
private const val NOTIFICATION_UPPER_HOUR = 22
private const val NOTIFICATION_UPPER_MINUTE = 0

private const val HIGH_PRIORITY_NOTIFICATION_CHANNEL_ID =
    "HIGH_PRIORITY_TIME_TO_LEARN_NOTIFICATION_CHANNEL"
private const val ACTIVITY_FROM_NOTIFICATION_REQ_CODE = 111

@AndroidEntryPoint
class NotificationJobReceiver : BroadcastReceiver() {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var preferences: PrefsDecorator

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != INTENT_ACTION) {
            Log.w(this.javaClass.name, "Unsupported action ${intent?.action}")
            return
        }

        configureNotificationsIfNeeded(context)
        notifyItsTimeToLearnIfNecessary(context)
    }

    private fun notifyItsTimeToLearnIfNecessary(context: Context) {
        val availableWords = wordService.getCountOfReadyForTrainingWords()
        if (!canNotificationBeSent(availableWords, context)) return

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val selfPermission = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (selfPermission != PackageManager.PERMISSION_GRANTED) return
            }
            notify(
                now().toInt(),
                getNotificationBuilder(availableWords, context).build()
            )
        }
        preferences.setLastNotifiedAboutActiveWords(now())
    }

    @VisibleForTesting
    fun canNotificationBeSent(availableWords: Int, context: Context): Boolean {
        val areEnoughWords = availableWords >= ACTIVE_WORDS_FOR_NOTIFICATION
        if (!areEnoughWords) return false

        val now = LocalDateTime.now()

        val start = now
            .withHour(NOTIFICATION_LOWER_HOUR)
            .withMinute(NOTIFICATION_LOWER_MINUTE)
            .truncatedTo(ChronoUnit.MINUTES)
        val end = now
            .withHour(NOTIFICATION_UPPER_HOUR)
            .withMinute(NOTIFICATION_UPPER_MINUTE)
            .truncatedTo(ChronoUnit.MINUTES)

        val isDayTimeSuitRequirements = now.isAfter(start).and(now.isBefore(end))
        if (!isDayTimeSuitRequirements) return false

        val appIsOnForeground = isOnForeground(context)
        if (appIsOnForeground) return false

        return !alreadyNotifiedToday()
    }

    private fun alreadyNotifiedToday(): Boolean {
        val lastNotificationTs = preferences.getLastNotifiedAboutActiveWords() ?: return false

        val lastNotificationDate = toLocalDateTime(lastNotificationTs)
        return LocalDate.now().isEqual(lastNotificationDate.toLocalDate())
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
        val pendingIntent = PendingIntent.getActivity(
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

