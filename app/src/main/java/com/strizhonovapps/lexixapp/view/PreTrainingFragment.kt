package com.strizhonovapps.lexixapp.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.dao.allAvailable
import com.strizhonovapps.lexixapp.model.TrainingType
import com.strizhonovapps.lexixapp.service.WordService
import com.strizhonovapps.lexixapp.util.PrefsDecorator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

const val CLOSEST_WORDS_TO_RESET = 15

@AndroidEntryPoint
class PreTrainingFragment : Fragment(), View.OnClickListener {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var preferences: PrefsDecorator

    private var fromStudyTrainingTypeButton: Button? = null
    private var fromNativeTrainingTypeButton: Button? = null
    private var mixedTrainingTypeButton: Button? = null
    private var startTrainingButton: Button? = null
    private var wordsToLearnTextView: TextView? = null
    private var wordsPerDayTextView: TextView? = null
    private var noWordsForNowMessage: TextView? = null
    private var noWordsAtAllMessage: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment__pre_training, container, false)
        initViewComponents(view)
        setListeners()
        refresh()
        return view
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fragment_pre_training__button__from_study -> setActiveModeButton(TrainingType.STUDY_TO_NATIVE)
            R.id.fragment_pre_training__button__from_native -> setActiveModeButton(TrainingType.NATIVE_TO_STUDY)
            R.id.fragment_pre_training__button__mixed_mode -> setActiveModeButton(TrainingType.MIXED)
            R.id.fragment_pre_training__button__start_training -> {
                if (noWordsYet()) wordService.resetClosestWordsDates(CLOSEST_WORDS_TO_RESET)
                val intent = Intent(context, TrainingActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun refresh() {
        CoroutineScope(Dispatchers.IO).launch {
            val averageExpectedWordsAndActiveWords =
                wordService.getAverageExpectedWordsAndCountOfActiveWords()
            CoroutineScope(Dispatchers.Main).launch {
                val lastTrainingType = preferences.getTrainingType()
                    ?.uppercase(Locale.ROOT)
                    ?.let { trainingType -> TrainingType.valueOf(trainingType) }
                    ?: TrainingType.MIXED
                setActiveModeButton(lastTrainingType)

                wordsPerDayTextView?.text = averageExpectedWordsAndActiveWords.first.toString()

                val activeWords = averageExpectedWordsAndActiveWords.second
                wordsToLearnTextView?.text = activeWords.toString()

                if (activeWords > 0) {
                    setReadyForTrainingView()
                } else if (noWordsAtAllForLearning()) {
                    setNoTrainingAvalableView()
                } else if (noWordsYet()) {
                    setReadyForTrainingAfterConfirmationView()
                } else {
                    throw IllegalStateException("Unknown pre-training state.")
                }
            }
        }
    }

    private fun setReadyForTrainingAfterConfirmationView() {
        noWordsAtAllMessage?.visibility = View.GONE

        noWordsForNowMessage?.visibility = View.VISIBLE

        fromStudyTrainingTypeButton?.visibility = View.VISIBLE
        fromNativeTrainingTypeButton?.visibility = View.VISIBLE
        mixedTrainingTypeButton?.visibility = View.VISIBLE
        startTrainingButton?.apply {
            visibility = View.VISIBLE
            background = ContextCompat.getDrawable(context, R.drawable.plate__light_2)
            text = context.getString(R.string.fragment_pre_training__button__start_anyway)
        }
    }

    private fun setNoTrainingAvalableView() {
        noWordsAtAllMessage?.visibility = View.VISIBLE

        noWordsForNowMessage?.visibility = View.INVISIBLE

        fromStudyTrainingTypeButton?.visibility = View.INVISIBLE
        fromNativeTrainingTypeButton?.visibility = View.INVISIBLE
        mixedTrainingTypeButton?.visibility = View.INVISIBLE
        startTrainingButton?.visibility = View.INVISIBLE
    }

    private fun setReadyForTrainingView() {
        noWordsAtAllMessage?.visibility = View.GONE

        noWordsForNowMessage?.visibility = View.INVISIBLE

        fromStudyTrainingTypeButton?.visibility = View.VISIBLE
        fromNativeTrainingTypeButton?.visibility = View.VISIBLE
        mixedTrainingTypeButton?.visibility = View.VISIBLE
        startTrainingButton?.apply {
            visibility = View.VISIBLE
            background = ContextCompat.getDrawable(context, R.drawable.plate__accent)
            text = context.getString(R.string.fragment_pre_training__button__start)
        }
    }

    private fun setActiveModeButton(trainingType: TrainingType) {
        fromStudyTrainingTypeButton?.setBackgroundResource(R.drawable.plate__light_1)
        fromNativeTrainingTypeButton?.setBackgroundResource(R.drawable.plate__light_1)
        mixedTrainingTypeButton?.setBackgroundResource(R.drawable.plate__light_1)

        preferences.setTrainingType(trainingType.name)

        val buttonToHighlight = when (trainingType) {
            TrainingType.STUDY_TO_NATIVE -> fromStudyTrainingTypeButton
            TrainingType.MIXED -> mixedTrainingTypeButton
            TrainingType.NATIVE_TO_STUDY -> fromNativeTrainingTypeButton
        }
        buttonToHighlight?.setBackgroundResource(R.drawable.plate__accent_alternative)
    }

    private fun noWordsAtAllForLearning() = wordService.findAll(allAvailable).isEmpty()

    @Suppress("BooleanMethodIsAlwaysInverted")
    private fun noWordsYet(): Boolean =
        wordService.getCountOfReadyForTrainingWords() == 0
                && wordService.findAll(allAvailable).isNotEmpty()

    private fun setListeners() {
        fromStudyTrainingTypeButton?.setOnClickListener(this)
        fromNativeTrainingTypeButton?.setOnClickListener(this)
        mixedTrainingTypeButton?.setOnClickListener(this)
        startTrainingButton?.setOnClickListener(this)
    }

    private fun initViewComponents(view: View) {
        fromStudyTrainingTypeButton =
            view.findViewById(R.id.fragment_pre_training__button__from_study)
        fromNativeTrainingTypeButton =
            view.findViewById(R.id.fragment_pre_training__button__from_native)
        mixedTrainingTypeButton = view.findViewById(R.id.fragment_pre_training__button__mixed_mode)
        startTrainingButton = view.findViewById(R.id.fragment_pre_training__button__start_training)
        wordsToLearnTextView = view.findViewById(R.id.fragment_pre_training__text_view__word_count)
        wordsPerDayTextView = view.findViewById(R.id.fragment_pre_training__text_view__per_day)
        noWordsForNowMessage =
            view.findViewById(R.id.fragment_pre_training__text_view__no_words_for_now)
        noWordsAtAllMessage =
            view.findViewById(R.id.fragment_pre_training__text_view__no_words_at_all)
    }

}