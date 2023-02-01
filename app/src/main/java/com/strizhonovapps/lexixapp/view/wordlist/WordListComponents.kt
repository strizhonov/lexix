package com.strizhonovapps.lexixapp.view.wordlist

import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

data class WordListComponents(
    var addWordActionButton: FloatingActionButton,
    var searchButton: Button,
    var backFromSearchButton: Button,
    var wordCounterBlock: RelativeLayout,
    var shownSearchBlock: RelativeLayout,
    var wordCounterTextView: TextView,
    var wordLabelTextView: TextView,
    var search: EditText,
    var listView: ListView,
    var navigationView: NavigationView,
    var drawer: DrawerLayout,
)