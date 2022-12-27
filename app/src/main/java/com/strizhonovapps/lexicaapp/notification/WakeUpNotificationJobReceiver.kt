package com.strizhonovapps.lexicaapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import javax.inject.Inject

class WakeUpNotificationJobReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmConfigurator: NotificationAlarmConfigurer

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!intent?.action.equals("android.intent.action.BOOT_COMPLETED")) {
            Log.w(this.javaClass.name, "Unsupported action ${intent?.action}")
            return
        }

        context?.let { nonNullContext ->
            DiComponentFactory.initIfNecessary(nonNullContext)
            DiComponentFactory.getInstance().inject(this)

            alarmConfigurator.setNotificationAlarm(nonNullContext)
        }
    }
}