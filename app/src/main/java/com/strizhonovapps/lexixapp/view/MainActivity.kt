package com.strizhonovapps.lexixapp.view

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.viewsupport.bottombar.SmoothBottomBar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity__main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        configurePages()
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
