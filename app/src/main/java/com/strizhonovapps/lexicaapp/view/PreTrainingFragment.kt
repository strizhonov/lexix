package com.strizhonovapps.lexicaapp.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.strizhonovapps.lexicaapp.R
import com.strizhonovapps.lexicaapp.TRAINING_PREFS_KEY
import com.strizhonovapps.lexicaapp.TRAINING_TYPE
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.model.TrainingType
import com.strizhonovapps.lexicaapp.service.WordService
import javax.inject.Inject

const val CLOSEST_WORDS_TO_RESET = 10

class PreTrainingFragment : Fragment(), View.OnClickListener {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var preferences: SharedPreferences

    init {
        DiComponentFactory.getInstance().inject(this)
    }

    private var fromStudyTrainingTypeButton: Button? = null
    private var fromNativeTrainingTypeButton: Button? = null
    private var mixedTrainingTypeButton: Button? = null
    private var startTrainingButton: Button? = null
    private var wordsToLearnTextView: TextView? = null
    private var noWordsForNowMessage: TextView? = null
    private var noWordsAtAllMessage: TextView? = null

    private var selectedTrainingType = TrainingType.STUDY_TO_NATIVE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment__pre_training, container, false)
        initViewComponents(view)
        setListeners()
        return view
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val savedTrainingType = preferences.getString(TRAINING_PREFS_KEY, null)
        val lastTrainingType =
            TrainingType.values().find { it.name.equals(savedTrainingType, ignoreCase = true) }
        setActiveModeButton(lastTrainingType ?: TrainingType.STUDY_TO_NATIVE)

        val activeWords = wordService.getCountOfReadyForTrainingWords()
        wordsToLearnTextView?.text = activeWords.toString()
        if (activeWords != 0) {
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
        } else if (noWordsAtAllForLearning()) {
            noWordsAtAllMessage?.visibility = View.VISIBLE

            noWordsForNowMessage?.visibility = View.INVISIBLE

            fromStudyTrainingTypeButton?.visibility = View.INVISIBLE
            fromNativeTrainingTypeButton?.visibility = View.INVISIBLE
            mixedTrainingTypeButton?.visibility = View.INVISIBLE
            startTrainingButton?.visibility = View.INVISIBLE
        } else if (noWordsYet()) {
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
        } else {
            throw IllegalStateException("Unknown pre-training state, active words = $activeWords")
        }
    }

    private fun noWordsYet(): Boolean =
        wordService.getCountOfReadyForTrainingWords() == 0
                && wordService.findAllAvailableForTraining().isNotEmpty()

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fragment_pre_training__button__from_study -> setActiveModeButton(TrainingType.STUDY_TO_NATIVE)
            R.id.fragment_pre_training__button__from_native -> setActiveModeButton(TrainingType.NATIVE_TO_STUDY)
            R.id.fragment_pre_training__button__mixed_mode -> setActiveModeButton(TrainingType.MIXED)
            R.id.fragment_pre_training__button__start_training -> {
                if (noWordsYet()) wordService.resetClosestWordsDates(CLOSEST_WORDS_TO_RESET)
                val intent = Intent(context, TrainingActivity::class.java)
                intent.putExtra(TRAINING_TYPE, selectedTrainingType)
                startActivity(intent)
            }
        }
    }

    private fun setActiveModeButton(trainingType: TrainingType) {
        fromStudyTrainingTypeButton?.setBackgroundResource(R.drawable.plate__light_1)
        fromNativeTrainingTypeButton?.setBackgroundResource(R.drawable.plate__light_1)
        mixedTrainingTypeButton?.setBackgroundResource(R.drawable.plate__light_1)
        selectedTrainingType = trainingType
        preferences.edit().apply {
            putString(
                TRAINING_PREFS_KEY,
                trainingType.name
            )
            apply()
        }
        val buttonToHighlight = when (trainingType) {
            TrainingType.STUDY_TO_NATIVE -> fromStudyTrainingTypeButton
            TrainingType.MIXED -> mixedTrainingTypeButton
            TrainingType.NATIVE_TO_STUDY -> fromNativeTrainingTypeButton
        }
        buttonToHighlight?.setBackgroundResource(R.drawable.plate__accent_alternative)
    }

    private fun noWordsAtAllForLearning() = wordService.findAllAvailableForTraining().isEmpty()

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
        noWordsForNowMessage =
            view.findViewById(R.id.fragment_pre_training__text_view__no_words_for_now)
        noWordsAtAllMessage =
            view.findViewById(R.id.fragment_pre_training__text_view__no_words_at_all)
    }

}