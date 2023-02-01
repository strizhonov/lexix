package com.strizhonovapps.lexixapp.view

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.model.AllowedWordCardSide
import com.strizhonovapps.lexixapp.model.TrainingType
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.service.ImageService
import com.strizhonovapps.lexixapp.service.LevelColorDefiner
import com.strizhonovapps.lexixapp.service.WordService
import com.strizhonovapps.lexixapp.util.PrefsDecorator
import com.strizhonovapps.lexixapp.util.UrlAudioPlayer
import com.strizhonovapps.lexixapp.util.VibratorDecorator
import com.strizhonovapps.lexixapp.viewsupport.SegmentedProgressBar
import com.strizhonovapps.lexixapp.viewsupport.blur.Blur
import com.strizhonovapps.lexixapp.viewsupport.blur.BlurFactor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import javax.inject.Inject

@AndroidEntryPoint
class TrainingActivity : ComponentActivity(), View.OnClickListener {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var imageService: ImageService

    @Inject
    lateinit var levelColorDefiner: LevelColorDefiner

    @Inject
    lateinit var preferences: PrefsDecorator

    @Inject
    lateinit var vibrator: VibratorDecorator

    @Inject
    lateinit var player: UrlAudioPlayer

    private var knownButton: LinearLayout? = null
    private var skipButton: LinearLayout? = null
    private var unknownButton: LinearLayout? = null
    private var editButton: ImageView? = null
    private var deleteButton: ImageView? = null
    private var wordStudyTextView: TextView? = null
    private var transcriptionTextView: TextView? = null
    private var wordNativeTextView: TextView? = null
    private var wordLevelTextView: TextView? = null
    private var noImageTextView: TextView? = null
    private var wordTagTextView: TextView? = null
    private var showButton: ImageView? = null
    private var wordImageView: ImageView? = null
    private var wordSideImageView: ImageView? = null
    private var wordAudioButton: ImageView? = null
    private var wordProgressBar: SegmentedProgressBar? = null

    private var currentWord: Word? = null

    private var trainingType: TrainingType? = null

    private var activeWords = 0
    private var sessionWords = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__training)

        trainingType = preferences.getTrainingType()
            ?.uppercase()
            ?.let { trainingType -> TrainingType.valueOf(trainingType) }
            ?: TrainingType.MIXED

        initViewComponents()
        setListeners()
        refreshView()
    }

    override fun onResume() {
        super.onResume()
        refreshView()
    }

    override fun onClick(v: View) {
        val word = currentWord
        if (word == null) {
            Log.w(this::class.java.name, "Unable to perform training operations without word")
            return
        }
        when (v.id) {
            R.id.activity_training__image_view__delete_word -> {
                wordService.delete(word.id)
                refreshView()
            }

            R.id.activity_training__image_view__edit_word -> {
                val modifyIntent = Intent(applicationContext, ModifyWordActivity::class.java)
                WordIntentExtraMapper(modifyIntent).setWord(word)
                startActivity(modifyIntent)
            }

            R.id.activity_training__image_view__show_translation -> {
                setAfterShowButtonView(word)
            }

            R.id.activity_training__layout__known_word -> {
                CoroutineScope(Dispatchers.Default).launch {
                    wordService.processKnown(word)
                }
                sessionWords = sessionWords.inc()
                refreshView(word)
                if (activeWords == 0) updateLastSessionEnd()
            }

            R.id.activity_training__layout__unknown_word -> {
                CoroutineScope(Dispatchers.Default).launch {
                    wordService.processUnknown(word)
                }
                vibrator.vibrate(25)
                sessionWords = sessionWords.inc()
                refreshView(word)
                if (activeWords == 0) updateLastSessionEnd()
            }

            R.id.activity_training__layout__skip_word -> {
                CoroutineScope(Dispatchers.Default).launch {
                    wordService.skipWord(word)
                }
                sessionWords = sessionWords.inc()
                refreshView(word)
                if (activeWords == 0) updateLastSessionEnd()
            }

            R.id.activity_training__image_view__word_audio -> {
                player.play(word.audioUrl)
            }
        }
    }

    private fun setListeners() {
        showButton?.setOnClickListener(this)
        knownButton?.setOnClickListener(this)
        skipButton?.setOnClickListener(this)
        unknownButton?.setOnClickListener(this)
        editButton?.setOnClickListener(this)
        deleteButton?.setOnClickListener(this)
        wordAudioButton?.setOnClickListener(this)
    }

    private fun initViewComponents() {
        wordImageView = findViewById(R.id.activity_training__image_view__word_image)
        wordStudyTextView = findViewById(R.id.activity_training__text_view__word_name)
        wordStudyTextView?.movementMethod = ScrollingMovementMethod()
        wordNativeTextView = findViewById(R.id.activity_training__text_view__word_translation)
        wordNativeTextView?.movementMethod = ScrollingMovementMethod()
        showButton = findViewById(R.id.activity_training__image_view__show_translation)
        knownButton = findViewById(R.id.activity_training__layout__known_word)
        skipButton = findViewById(R.id.activity_training__layout__skip_word)
        unknownButton = findViewById(R.id.activity_training__layout__unknown_word)
        editButton = findViewById(R.id.activity_training__image_view__edit_word)
        deleteButton = findViewById(R.id.activity_training__image_view__delete_word)
        wordLevelTextView = findViewById(R.id.activity_training__text_view__word_level)
        wordTagTextView = findViewById(R.id.activity_training__text_view__word_tag)
        wordAudioButton = findViewById(R.id.activity_training__image_view__word_audio)
        wordProgressBar = findViewById(R.id.activity_training__progress_bar__session)
        transcriptionTextView = findViewById(R.id.activity_training__text_view__word_transcription)
        noImageTextView = findViewById(R.id.activity_training__image_view__word_no_image)
        wordSideImageView = findViewById(R.id.activity_training__side)
    }

    private fun refreshView(wordToExclude: Word? = null) {
        val currentWordAndCountOfActiveWords = wordService.getCurrentWordAndCountOfActiveWords(
            trainingType = trainingType,
            wordToExclude = wordToExclude
        )
        this.activeWords = currentWordAndCountOfActiveWords.second

        wordProgressBar?.setDivisions(sessionWords.plus(activeWords))
        wordProgressBar?.enableFirstDivisions(sessionWords)
        CoroutineScope(Dispatchers.Main).launch {
            refreshViewWithNewWord(currentWordAndCountOfActiveWords.first)
        }
    }

    private fun refreshViewWithNewWord(word: Word?) {
        this.currentWord = word
        if (word == null) finish()
        else setWordView(word)
    }

    private fun setWordView(word: Word) {
        wordNativeTextView?.scrollTo(0, 0)
        wordStudyTextView?.scrollTo(0, 0)
        showButton?.setImageResource(R.drawable.ic__visibility)
        wordNativeTextView?.visibility = View.INVISIBLE

        val wordSideToShow = defineWordSideToShow(word.allowedWordCardSide)
        when (wordSideToShow) {
            AllowedWordCardSide.NATIVE -> {
                wordNativeTextView?.text = word.name
                wordStudyTextView?.text = word.translation
            }

            AllowedWordCardSide.STUDY -> {
                wordStudyTextView?.text = word.name
                wordNativeTextView?.text = word.translation
            }

            else -> throw IllegalStateException("Illegal training type $trainingType")
        }

        setWordSideView(word.allowedWordCardSide)
        setTagView(word.tag)
        setTranscription(word.transcription, wordSideToShow)
        setAudioButton(wordSideToShow, word.audioUrl)
        setWordImage(word = word, blur = true)
        setLevelView(word)
    }

    private fun defineWordSideToShow(allowedWordCardSide: AllowedWordCardSide): AllowedWordCardSide {
        val wordSideToShow = when (trainingType) {
            TrainingType.MIXED -> {
                if (allowedWordCardSide == AllowedWordCardSide.STUDY) AllowedWordCardSide.STUDY
                else if (allowedWordCardSide == AllowedWordCardSide.NATIVE) AllowedWordCardSide.NATIVE
                else if (Random().nextBoolean()) AllowedWordCardSide.NATIVE
                else AllowedWordCardSide.STUDY
            }

            TrainingType.NATIVE_TO_STUDY -> AllowedWordCardSide.NATIVE
            TrainingType.STUDY_TO_NATIVE -> AllowedWordCardSide.STUDY
            else -> throw IllegalStateException("Illegal training type $trainingType")
        }
        return wordSideToShow
    }

    private fun setWordSideView(allowedWordCardSide: AllowedWordCardSide) {
        if (allowedWordCardSide != AllowedWordCardSide.ALL) {
            wordSideImageView?.visibility = View.VISIBLE
            val res = when (allowedWordCardSide) {
                AllowedWordCardSide.NATIVE -> R.drawable.baseline_arrow_upward_24
                AllowedWordCardSide.STUDY -> R.drawable.baseline_arrow_downward_24
                else -> throw IllegalStateException()
            }
            wordSideImageView?.setImageResource(res)
        } else {
            wordSideImageView?.visibility = View.GONE
        }
    }

    private fun setTagView(tag: String?) {
        if (tag == null) {
            wordTagTextView?.visibility = View.INVISIBLE
        } else {
            wordTagTextView?.visibility = View.VISIBLE
            wordTagTextView?.text = tag
        }
    }

    private fun setTranscription(transcription: String?, wordSide: AllowedWordCardSide) {
        if (transcription.isNullOrBlank() || wordSide == AllowedWordCardSide.NATIVE) {
            transcriptionTextView?.visibility = View.GONE
        } else {
            transcriptionTextView?.visibility = View.VISIBLE
            transcriptionTextView?.text = getString(R.string.transcription_wrapper, transcription)
        }
    }

    private fun setAudioButton(wordSideToShow: AllowedWordCardSide, url: String?) {
        wordAudioButton?.apply {
            if (wordSideToShow == AllowedWordCardSide.NATIVE || url.isNullOrBlank()) {
                setOnClickListener { }
                setImageResource(R.drawable.ic__play_inactive)
            } else {
                setImageResource(R.drawable.ic__play)
                setOnClickListener(this@TrainingActivity)
            }
        }
    }

    private fun setLevelView(word: Word) {
        wordLevelTextView?.text =
            String.format(
                "%d %s",
                word.level,
                applicationContext.getString(R.string.fragment_word_list__level_abbreviation)
            )
        val lvlBackground = levelColorDefiner.defineBackground(word.level)
        val lvlColor = levelColorDefiner.defineColor(word.level)
        wordLevelTextView?.background = ContextCompat.getDrawable(applicationContext, lvlBackground)
        wordLevelTextView?.setTextColor(ContextCompat.getColor(applicationContext, lvlColor))
    }

    private fun setWordImage(word: Word, blur: Boolean) {
        val image = imageService.getSquaredImage(word)
        if (image == null) {
            wordImageView?.visibility = View.GONE
            noImageTextView?.visibility = View.VISIBLE
            return
        }

        noImageTextView?.visibility = View.GONE
        wordImageView?.visibility = View.VISIBLE
        val img = RoundedBitmapDrawableFactory.create(
            applicationContext.resources,
            if (blur) Blur.of(
                applicationContext,
                image,
                BlurFactor(height = image.height, width = image.width)
            ) else image
        )
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            4f,
            resources.displayMetrics
        )
        img.cornerRadius = px
        wordImageView?.setImageDrawable(img)
    }

    private fun setAfterShowButtonView(word: Word) {
        wordNativeTextView?.visibility = View.VISIBLE
        showButton?.setImageResource(R.drawable.ic__visibility_inactive)
        setWordImage(word = word, blur = false)
        if (word.audioUrl.isNullOrBlank().not()) {
            wordAudioButton?.setImageResource(R.drawable.ic__play)
            wordAudioButton?.setOnClickListener(this)
        }
    }

    private fun updateLastSessionEnd() {
        sessionWords = 0
    }
}