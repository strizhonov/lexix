package com.strizhonovapps.lexicaapp.view

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.strizhonovapps.lexicaapp.*
import com.strizhonovapps.lexicaapp.di.DiComponentFactory
import com.strizhonovapps.lexicaapp.model.Word
import com.strizhonovapps.lexicaapp.service.ImageService
import com.strizhonovapps.lexicaapp.service.LevelColorDefiner
import com.strizhonovapps.lexicaapp.service.WordOperationsServiceImpl
import com.strizhonovapps.lexicaapp.service.WordService
import javax.inject.Inject


class WordListFragment : Fragment(), OnClickListener {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var langService: WordOperationsServiceImpl

    @Inject
    lateinit var imageService: ImageService

    @Inject
    lateinit var levelColorDefiner: LevelColorDefiner

    private var addWordActionButton: FloatingActionButton? = null
    private var progressBar: ProgressBar? = null
    private var searchButton: Button? = null
    private var backFromSearchButton: Button? = null
    private var wordCounterBlock: RelativeLayout? = null
    private var shownSearchBlock: RelativeLayout? = null
    private var wordCounterTextView: TextView? = null
    private var wordLabelTextView: TextView? = null
    private var search: EditText? = null
    private var listView: ListView? = null
    private var navigationView: NavigationView? = null
    private var mDrawer: DrawerLayout? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    private var latestFirstPosition = 0

    private lateinit var fileSelectorLauncher: ActivityResultLauncher<Intent>
    private lateinit var fileSavingLauncher: ActivityResultLauncher<Intent>
    private lateinit var wordAddingLauncher: ActivityResultLauncher<String>

    init {
        DiComponentFactory.getInstance().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment__word_list, container, false)

        mDrawer = view.findViewById(R.id.drawer_layout)
        mDrawerToggle = ActionBarDrawerToggle(
            activity,
            mDrawer,
            R.string.list_drawer__description__open,
            R.string.list_drawer__description__close
        ).apply { syncState() }

        navigationView = view.findViewById(R.id.fragment_word_list__navigation_view__drawer)
        wordCounterTextView = view.findViewById(R.id.fragment_word_list__text_view__word_counter)
        wordLabelTextView =
            view.findViewById(R.id.fragment_word_list__text_view__displayed_words_group)
        searchButton = view.findViewById(R.id.fragment_word_list__button__search)
        backFromSearchButton = view.findViewById(R.id.fragment_word_list__button__back_from_search)
        progressBar = view.findViewById(R.id.fragment_word_list__progress_bar__on_loading)
        wordCounterBlock = view.findViewById(R.id.fragment_word_list__layout__search)
        shownSearchBlock = view.findViewById(R.id.fragment_word_list__layout__shown_search)

        view.findViewById<Button>(R.id.fragment_word_list__button__open_drawer)
            .setOnClickListener(this)
        searchButton?.setOnClickListener(this)
        backFromSearchButton?.setOnClickListener(this)
        navigationView?.setNavigationItemSelectedListener(::onDrawerMenuClick)

        fileSelectorLauncher = createFileSelectorLauncher()
        fileSavingLauncher = createFileSavingLauncher()
        wordAddingLauncher = createWordAddingLauncher()

        initListView(view)
        initSearch(view)
        initFloatingButtons(view)

        runBackgroundThenUi(
            this.requireActivity(),
            { wordService.findAll().sortedByDescending { it.id } },
            ::refreshListView
        )

        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fragment_word_list__button__search -> {
                showSearch(v)
            }
            R.id.fragment_word_list__button__back_from_search -> {
                hideSearch(v)
                runBackgroundThenUi(
                    this.requireActivity(),
                    { wordService.findAll().sortedByDescending { it.id } },
                    ::refreshListView
                )
            }
            R.id.fragment_word_list__button__open_drawer -> {
                mDrawer?.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        runBackgroundThenUi(
            this.requireActivity(),
            { wordService.findAll().sortedByDescending { it.id } },
            ::refreshListView
        )
    }

    override fun onPause() {
        latestFirstPosition = listView?.firstVisiblePosition ?: 1
        super.onPause()
    }

    private fun onDrawerMenuClick(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.menu_word_list__all_words -> {
            mDrawer?.closeDrawer(GravityCompat.START)
            runBackgroundThenUi(
                requireActivity(),
                { wordService.findAll().sortedByDescending { it.id } },
                ::refreshListView
            )
            true
        }
        R.id.menu_word_list__archived_words -> {
            mDrawer?.closeDrawer(GravityCompat.START)
            runBackgroundThenUi(
                requireActivity(),
                { wordService.findAllArchived().sortedByDescending { it.id } },
                ::refreshListView
            )
            true
        }
        R.id.menu_word_list__hard_words -> {
            mDrawer?.closeDrawer(GravityCompat.START)
            runBackgroundThenUi(
                requireActivity(),
                { wordService.findAllHard().sortedByDescending { it.id } },
                ::refreshListView
            )
            true
        }
        R.id.menu_word_list__clear -> {
            ListDrawerDialogFactory(requireActivity(), wordService)
                .getEraseConfirmationDialog {
                    mDrawer?.closeDrawer(GravityCompat.START)
                    runBackgroundThenUi(
                        requireActivity(),
                        { wordService.findAll().sortedByDescending { it.id } },
                        ::refreshListView
                    )
                }
                .show()
            true
        }
        R.id.menu_word_list__reset_progress -> {
            ListDrawerDialogFactory(requireActivity(), wordService)
                .getResetProgressDialog {
                    mDrawer?.closeDrawer(GravityCompat.START)
                    runBackgroundThenUi(
                        requireActivity(),
                        { wordService.findAll().sortedByDescending { it.id } },
                        ::refreshListView
                    )
                }
                .show()
            true
        }
        R.id.menu_word_list__restore -> {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "text/plain"
            fileSelectorLauncher.launch(
                Intent.createChooser(
                    intent,
                    getString(R.string.dialog__content__select_file)
                )
            )
            true
        }
        else -> false
    }

    private fun hideSearch(v: View) {
        shownSearchBlock?.visibility = GONE
        wordCounterBlock?.visibility = VISIBLE
        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(v.windowToken, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showSearch(v: View) {
        shownSearchBlock?.visibility = VISIBLE
        wordCounterBlock?.visibility = GONE
        search?.requestFocus()
        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun initListView(view: View) {
        listView = view.findViewById(R.id.fragment_word_list__list_view__main)
        listView?.emptyView = view.findViewById(R.id.fragment_word_list__text_view__no_words)
        listView?.isTextFilterEnabled = true
        listView?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            onListItemClick(position)
        }
    }

    private fun onListItemClick(position: Int) {
        val presentId = listView?.getItemAtPosition(position) as Word
        val word = wordService.get(presentId.id) ?: return
        val modifyIntent = Intent(context, ModifyWordActivity::class.java)
        modifyIntent.putExtra(ID_KEY, word.id)
            .putExtra(NAME_KEY, word.name)
            .putExtra(TRANSLATION_KEY, word.translation)
            .putExtra(LVL_KEY, word.level)
            .putExtra(TARGET_DATE_TIME_KEY, word.targetDate.time)
            .putExtra(MODIFICATION_DATE_TIME_KEY, word.modificationDate.time)
            .putExtra(IS_ARCHIVED_KEY, word.isArchived)
            .putExtra(TIMES_SHOWN_KEY, word.timesShown)
            .putExtra(IS_HARD_WORD, word.isHard())
        startActivity(modifyIntent)
    }

    private fun initSearch(view: View) {
        search = view.findViewById(R.id.fragment_word_list__edit_text__search_text)
        search?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                (listView?.adapter as WordToListViewAdapter).filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun initFloatingButtons(view: View) {
        addWordActionButton = view.findViewById(R.id.fragment_word_list__floating_button__add_word)
        addWordActionButton?.setOnClickListener {
            wordAddingLauncher.launch("")
        }
    }

    private fun createFileSelectorLauncher(): ActivityResultLauncher<Intent> {
        val contract = ActivityResultContracts.StartActivityForResult()
        val callback = ActivityResultCallback<ActivityResult> { result ->
            result
                ?.let(ActivityResult::getData)
                ?.let(::showFileUploadingDialogOrToast)
        }
        return registerForActivityResult(contract, callback)
    }

    private fun createFileSavingLauncher(): ActivityResultLauncher<Intent> {
        val contract = ActivityResultContracts.StartActivityForResult()
        val callback = ActivityResultCallback<ActivityResult> { result ->
            result?.let {
                hideLoading()
            }
        }
        return registerForActivityResult(contract, callback)
    }

    private fun createWordAddingLauncher(): ActivityResultLauncher<String> {
        val contract = AddWordContract<String>()
        val callback = ActivityResultCallback<Pair<String, Long>?> { nameAndId ->
            if (nameAndId == null || (nameAndId.first == "" && nameAndId.second == -1L))
                return@ActivityResultCallback
            runBackgroundThenUi(
                requireActivity(),
                { langService.downloadWordMetadata(nameAndId.first, nameAndId.second) },
                { wordMetadata -> updateListItemImage(nameAndId.second, wordMetadata?.image) }
            )
        }
        return registerForActivityResult(contract, callback)
    }

    private fun updateListItemImage(id: Long, img: Bitmap?) {
        for (listViewIdx in 0 until listView?.adapter?.count!!) {
            val word = listView?.adapter?.getItem(listViewIdx) as Word
            if (word.id == id) {
                val neededWordView = listView?.getChildAt(listViewIdx)
                val someText =
                    neededWordView?.findViewById<ImageView>(R.id.view_list_word_item__image_view__word_image)
                someText?.setImageBitmap(img)
            }
            (listView?.adapter as WordToListViewAdapter).notifyDataSetChanged()
        }
    }

    private fun hideLoading() {
        progressBar?.visibility = GONE
        setDefaultVisibility()
    }

    private fun setDefaultVisibility() {
        addWordActionButton?.visibility = VISIBLE
        listView?.visibility = VISIBLE
        wordCounterBlock?.visibility = VISIBLE

        shownSearchBlock?.visibility = GONE
    }

    private fun showLoading() {
        progressBar?.visibility = VISIBLE

        addWordActionButton?.visibility = GONE
        listView?.visibility = GONE
        shownSearchBlock?.visibility = GONE
        wordCounterBlock?.visibility = GONE
    }

    private fun showFileUploadingDialogOrToast(data: Intent?) {
        try {
            data?.data?.let { showUploadingDialog(it) }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Unable to save words from file.", e)
            Toast.makeText(
                requireContext(),
                getString(R.string.fragment_word_list__toast__unable_to_save_from_file),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showUploadingDialog(data: Uri) {
        val separatorEditText = EditText(activity)
        val listener = { _: DialogInterface, _: Int ->
            val separator = separatorEditText.text.toString()
            val fileSavingIntent =
                Intent(context, FromFileSavingBackgroundActivity::class.java)
            fileSavingIntent.data = data
            fileSavingIntent.putExtra(SEPARATOR_KEY, separator)
            showLoading()
            fileSavingLauncher.launch(fileSavingIntent)
        }
        ListDrawerDialogFactory(
            requireActivity(),
            wordService
        )
            .getSeparatorDialog(listener, separatorEditText)
            .show()
    }

    private fun refreshListView(words: List<Word>) {
        wordLabelTextView?.text =
            context?.getString(R.string.fragment_word_list__group_title__all_words)
        hideLoading()
        updateAdapter(words)
        listView?.setSelection(latestFirstPosition)
        wordCounterTextView?.text = (listView?.adapter as WordToListViewAdapter).count.toString()
    }

    private fun updateAdapter(words: List<Word>) {
        if (listView?.adapter == null) {
            listView?.adapter = WordToListViewAdapter(
                R.layout.view__list_word_item,
                requireContext(),
                words,
                levelColorDefiner,
                imageService
            )
        } else {
            (listView?.adapter as WordToListViewAdapter?)?.words = words
        }
    }

}