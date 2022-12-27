package com.strizhonovapps.lexicaapp.view

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.strizhonovapps.lexicaapp.*
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.model.TrainingType
import com.strizhonovapps.lexicaapp.model.Word
import com.strizhonovapps.lexicaapp.service.ImageService
import com.strizhonovapps.lexicaapp.service.LevelColorDefiner
import com.strizhonovapps.lexicaapp.service.WordService
import com.strizhonovapps.lexicaapp.viewsupport.SegmentedProgressBar
import com.strizhonovapps.lexicaapp.viewsupport.blur.Blur
import com.strizhonovapps.lexicaapp.viewsupport.blur.BlurFactor
import java.util.*
import javax.inject.Inject

class TrainingActivity : Activity(), View.OnClickListener {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var imageService: ImageService

    @Inject
    lateinit var levelColorDefiner: LevelColorDefiner

    @Inject
    lateinit var preferences: SharedPreferences

    private var wordCardLayout: LinearLayout? = null
    private var knownButton: LinearLayout? = null
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
    private var wordAudioButton: ImageView? = null
    private var wordProgressBar: SegmentedProgressBar? = null

    private var currentWord: Word? = null

    private var trainingType: TrainingType? = null

    private var activeWords = 0
    private var sessionWords = 0

    init {
        DiComponentFactory.getInstance().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__training)
        trainingType = intent.getSerializableExtra(TRAINING_TYPE) as TrainingType
        initViewComponents()
        setListeners()
        refreshView()
    }

    override fun onResume() {
        super.onResume()
        refreshView()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.activity_training__image_view__delete_word -> {
                currentWord?.let(Word::id)?.let(wordService::delete)
                refreshView()
            }
            R.id.activity_training__image_view__edit_word -> {
                currentWord?.let { word -> // TODO toast if null
                    val modifyIntent = Intent(applicationContext, ModifyWordActivity::class.java)
                    modifyIntent
                        .putExtra(NAME_KEY, word.name)
                        .putExtra(TRANSLATION_KEY, word.translation)
                        .putExtra(ID_KEY, word.id)
                        .putExtra(TARGET_DATE_TIME_KEY, word.targetDate.time)
                        .putExtra(LVL_KEY, word.level)
                        .putExtra(MODIFICATION_DATE_TIME_KEY, word.modificationDate.time)
                    startActivity(modifyIntent)
                }
            }
            R.id.activity_training__image_view__show_translation -> {
                setAfterShowButtonView()
            }
            R.id.activity_training__layout__known_word -> {
                currentWord?.let { word -> // TODO toast if null
                    wordService.processKnown(word, activeWords)
                }
                sessionWords = sessionWords.inc()
                refreshView()
                if (activeWords == 0) updateLastSessionEnd()
            }
            R.id.activity_training__layout__unknown_word -> {
                currentWord?.let { word -> // TODO toast if null
                    wordService.processUnknown(word, activeWords)
                }
                sessionWords = sessionWords.inc()
                refreshView()
                if (activeWords == 0) updateLastSessionEnd()
            }
            R.id.activity_training__image_view__word_audio -> {
                val audioUrl = currentWord?.audioUrl
                if (audioUrl.isNullOrBlank().not()) {
                    val mp = MediaPlayer()
                    mp.setDataSource(audioUrl)
                    mp.prepare()
                    mp.start()
                }
            }
        }
    }

    private fun setListeners() {
        showButton?.setOnClickListener(this)
        knownButton?.setOnClickListener(this)
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
        unknownButton = findViewById(R.id.activity_training__layout__unknown_word)
        editButton = findViewById(R.id.activity_training__image_view__edit_word)
        deleteButton = findViewById(R.id.activity_training__image_view__delete_word)
        wordCardLayout = findViewById(R.id.activity_training__layout__wordcard)
        wordLevelTextView = findViewById(R.id.activity_training__text_view__word_level)
        wordTagTextView = findViewById(R.id.activity_training__text_view__word_tag)
        wordAudioButton = findViewById(R.id.activity_training__image_view__word_audio)
        wordProgressBar = findViewById(R.id.activity_training__progress_bar__session)
        transcriptionTextView = findViewById(R.id.activity_training__text_view__word_transcription)
        noImageTextView = findViewById(R.id.activity_training__image_view__word_no_image)
    }

    private fun refreshView() {
        val currentWordAndCountOfActiveWords = wordService.getCurrentWordAndCountOfActiveWords()
        this.activeWords = currentWordAndCountOfActiveWords.second

        wordProgressBar?.setDivisions(sessionWords.plus(activeWords))
        wordProgressBar?.setEnabledDivisions(
            if (sessionWords == 0) emptyList() else (0 until sessionWords).toList()
        )
        runBackgroundThenUi(
            this,
            { currentWordAndCountOfActiveWords.first },
            (::refreshView)
        )
    }

    private fun refreshView(word: Word?) {
        this.currentWord = word
        if (word == null)
            finish()
        else
            setWordView(word)
    }

    private fun setWordView(current: Word) {
        val actualViewTrainingType = when (trainingType) {
            TrainingType.MIXED -> if (Random().nextBoolean()) TrainingType.NATIVE_TO_STUDY else TrainingType.STUDY_TO_NATIVE
            TrainingType.NATIVE_TO_STUDY -> TrainingType.NATIVE_TO_STUDY
            TrainingType.STUDY_TO_NATIVE -> TrainingType.STUDY_TO_NATIVE
            else -> throw IllegalStateException("Illegal training type $trainingType")
        }
        when (actualViewTrainingType) {
            TrainingType.NATIVE_TO_STUDY -> {
                wordNativeTextView?.text = current.name
                wordStudyTextView?.text = current.translation
                TrainingType.NATIVE_TO_STUDY
            }
            TrainingType.STUDY_TO_NATIVE -> {
                wordStudyTextView?.text = current.name
                wordNativeTextView?.text = current.translation
                TrainingType.STUDY_TO_NATIVE
            }
            else -> throw IllegalStateException("Illegal training type $trainingType")
        }
        wordNativeTextView?.scrollTo(0, 0)
        wordStudyTextView?.scrollTo(0, 0)
        wordLevelTextView?.text = current.level.toString()
        val tag = current.tag
        if (tag == null) {
            wordTagTextView?.visibility = View.INVISIBLE
        } else {
            wordTagTextView?.visibility = View.VISIBLE
            wordTagTextView?.text = tag
        }

        wordAudioButton?.apply {
            if (actualViewTrainingType == TrainingType.NATIVE_TO_STUDY || current.audioUrl.isNullOrBlank()) {
                setOnClickListener { }
                setImageResource(R.drawable.ic__play_inactive)
            } else {
                setImageResource(R.drawable.ic__play)
                setOnClickListener(this@TrainingActivity)
            }
        }

        val transcription = current.transcription
        if (transcription.isNullOrBlank() || actualViewTrainingType == TrainingType.NATIVE_TO_STUDY) {
            transcriptionTextView?.visibility = View.GONE
        } else {
            transcriptionTextView?.visibility = View.VISIBLE
            transcriptionTextView?.text = "/$transcription/"
        }

        showButton?.setImageResource(R.drawable.ic__visibility)
        wordNativeTextView?.visibility = View.INVISIBLE

        setWordImage(blur = true)
        setLevelView()
    }

    private fun setLevelView() {
        wordLevelTextView?.text =
            String.format(
                "%d %s",
                currentWord?.level,
                applicationContext.getString(R.string.fragment_word_list__level_abbreviation)
            )
        val lvlBackground = levelColorDefiner.defineBackground(currentWord!!.level)
        val lvlColor = levelColorDefiner.defineColor(currentWord!!.level)
        wordLevelTextView?.background = ContextCompat.getDrawable(applicationContext, lvlBackground)
        wordLevelTextView?.setTextColor(ContextCompat.getColor(applicationContext, lvlColor))
    }

    private fun setWordImage(blur: Boolean) {
        val image = currentWord?.let { nonNullCurrent ->
            imageService.getSquaredImage(nonNullCurrent)
        }
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
        val px =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
        img.cornerRadius = px
        wordImageView!!.setImageDrawable(img)
    }

    private fun setAfterShowButtonView() {
        wordNativeTextView?.visibility = View.VISIBLE
        showButton?.setImageResource(R.drawable.ic__visibility_inactive)
        setWordImage(blur = false)
        if (currentWord?.audioUrl.isNullOrBlank().not()) {
            wordAudioButton?.setImageResource(R.drawable.ic__play)
            wordAudioButton?.setOnClickListener(this)
        }
    }

    private fun updateLastSessionEnd() {
        val edit = preferences.edit()
        edit.putBoolean(LAST_SESSION_FLAG_PREFS_KEY, true)
        edit.apply()
        sessionWords = 0
    }
}