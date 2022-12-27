package com.strizhonovapps.lexicaapp.view

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.strizhonovapps.lexicaapp.R
import com.strizhonovapps.lexicaapp.model.Word
import java.util.*

class WordFilter(
    private val arrayAdapter: ArrayAdapter<Word>,
    private val displayedValues: MutableList<Word>,
    private val originalValues: MutableList<Word>,
    private val context: Context
) : Filter() {

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        displayedValues.clear()
        safelyAddAll(results.values)
        arrayAdapter.notifyDataSetChanged()
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        if (originalValues.isEmpty()) {
            originalValues.addAll(displayedValues)
        }
        return if (constraint.isEmpty())
            getNonFilteredResults()
        else
            filterResults(constraint)
    }

    private fun filterResults(constraint: CharSequence): FilterResults {
        val filteredList = originalValues.filter { word ->
            doesWordsContainsConstraint(word, constraint)
        }
        val results = FilterResults()
        results.count = filteredList.size
        results.values = filteredList
        return results
    }

    private fun getNonFilteredResults(): FilterResults {
        val results = FilterResults()
        results.count = originalValues.size
        results.values = originalValues
        return results
    }

    private fun doesWordsContainsConstraint(word: Word, constraint: CharSequence): Boolean {
        val valueToCompare = "${word.name} " +
                "${word.translation} " +
                "${word.level} " +
                context.getString(R.string.fragment_word_list__level_abbreviation) + " " +
                if (word.isArchived) context.getString(R.string.fragment_word_list__text_view__archive_tag) else "" +
                        if (word.isHard()) context.getString(R.string.fragment_word_list__text_view__hard_tag) else ""
        return valueToCompare.lowercase(Locale.ROOT)
            .contains(constraint.toString().lowercase(Locale.ROOT))
    }

    private fun safelyAddAll(values: Any?) {
        if (values is Collection<*>) {
            values.filterIsInstance<Word>().forEach { displayedValues.add(it) }
        }
    }

}