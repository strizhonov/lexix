package com.strizhonovapps.lexicaapp.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val training = PreTrainingFragment()
    private val wordList = WordListFragment()
    private val stats = StatsFragment()

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> training
        1 -> wordList
        2 -> stats
        else -> throw IllegalStateException("Unknown tab with #$position requested")
    }

}