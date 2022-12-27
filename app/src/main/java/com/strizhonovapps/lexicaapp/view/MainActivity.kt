package com.strizhonovapps.lexicaapp.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.strizhonovapps.lexicaapp.ASKED_ABOUT_POWER_MANAGEMENT_KEY
import com.strizhonovapps.lexicaapp.R
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.model.wordSuggestions
import com.strizhonovapps.lexicaapp.notification.NotificationAlarmConfigurer
import com.strizhonovapps.lexicaapp.service.WordSuggestionService
import com.strizhonovapps.lexicaapp.viewsupport.bottombar.SmoothBottomBar
import hotchemi.android.rate.AppRate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


class MainActivity : FragmentActivity() {

    @Inject
    lateinit var wordSuggestionService: WordSuggestionService

    @Inject
    lateinit var alarmConfigurator: NotificationAlarmConfigurer

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity__main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        DiComponentFactory.initIfNecessary(this)
        DiComponentFactory.getInstance().inject(this)

        configurePowerManagement()
        alarmConfigurator.setNotificationAlarm(applicationContext)

        inflateSuggestedWords()

        configurePages()

        AppRate.with(this)
            .setMessage(R.string.dialog__content__rate_me)
            .monitor()
        AppRate.showRateDialogIfMeetsConditions(this)
    }

    private fun configurePowerManagement() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val powerManager =
            applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) return
        if (askedAboutPowerManagement()) return
        askUserToTurnOffPowerManagement()
    }

    private fun redirectToTurnOffPowerManagement() {
        try {
            when (Build.MANUFACTURER.uppercase(Locale.getDefault())) {
                "VIVO" -> {
                    startActivity(Intent("android.settings.SETTINGS"))
                }
                "OPPO", "REALME", "ONEPLUS" -> {
                    val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                    intent.data = Uri.fromParts("package", packageName, null as String?)
                    startActivity(intent)
                }
                else -> {
                    val intent = Intent("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS")
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Smth went wrong while turning off power management")
            Toast.makeText(
                applicationContext,
                getString(R.string.activity_main__toast__cant_turn_off_pwr_management),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun askUserToTurnOffPowerManagement() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setMessage(applicationContext.getString(R.string.activity_main__alert_message__allow_turn_off_power_management))
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            applicationContext.getString(R.string.dialog__content__ok)
        ) { dialog, _ ->
            preserveAskedAboutPowerStatus()
            dialog.dismiss()
            redirectToTurnOffPowerManagement()
        }
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            applicationContext.getString(R.string.dialog__content__cancel)
        ) { dialog, _ ->
            preserveAskedAboutPowerStatus()
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun preserveAskedAboutPowerStatus() {
        sharedPreferences.edit().apply {
            putBoolean(
                ASKED_ABOUT_POWER_MANAGEMENT_KEY,
                true
            )
            apply()
        }
    }

    private fun askedAboutPowerManagement() =
        sharedPreferences.getBoolean(ASKED_ABOUT_POWER_MANAGEMENT_KEY, false)

    private fun inflateSuggestedWords() {
        if (wordSuggestionService.isNoWordsForSuggestion()) {
            CoroutineScope(Dispatchers.Default).launch {
                wordSuggestionService.saveAllWordsForSuggestion(wordSuggestions)
            }
        }
    }

    private fun configurePages() {
        val viewPager = findViewById<ViewPager2>(R.id.activity_main__viewpager)
        val adapter = PagerAdapter(this)
        viewPager.isUserInputEnabled = false
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3
        val navigation = findViewById<View>(R.id.activity_main__nav_view) as SmoothBottomBar
        navigation.onItemSelected = { itemId -> viewPager.currentItem = itemId }
    }
}
