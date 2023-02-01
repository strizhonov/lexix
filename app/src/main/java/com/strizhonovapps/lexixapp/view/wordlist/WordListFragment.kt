package com.strizhonovapps.lexixapp.view.wordlist

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.service.ImageService
import com.strizhonovapps.lexixapp.service.LevelColorDefiner
import com.strizhonovapps.lexixapp.view.AddWordContract
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WordListFragment : Fragment(), OnClickListener {

    @Inject
    lateinit var wordListDelegate: WordListDelegate

    @Inject
    lateinit var levelColorDefiner: LevelColorDefiner

    @Inject
    lateinit var imageService: ImageService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment__word_list, container, false)

        view.findViewById<Button>(R.id.fragment_word_list__button__open_drawer)
            .setOnClickListener(this)

        val wordListAdapter = initWordListAdapter()
        val listView = initListView(view, wordListAdapter)

        wordListDelegate.lateInit(
            WordListComponents(
                initAddWordFloatingButton(view),
                initSearchButton(view),
                initBackFromSearchButton(view),
                view.findViewById(R.id.fragment_word_list__layout__search),
                view.findViewById(R.id.fragment_word_list__layout__shown_search),
                view.findViewById(R.id.fragment_word_list__text_view__word_counter),
                view.findViewById(R.id.fragment_word_list__text_view__list_name),
                initSearch(view),
                listView,
                initNavigationView(view),
                initDrawer(view),
            ),
            wordListAdapter,
            createWordAddingLauncher(),
            createFileSelectorLauncher(),
            createFileSavingLauncher(),
            this.requireActivity()
        )
        wordListDelegate.showLastView()

        return view
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fragment_word_list__button__search -> {
                wordListDelegate.showSearch()
            }

            R.id.fragment_word_list__button__back_from_search -> {
                wordListDelegate.hideSearch(view.windowToken)
            }

            R.id.fragment_word_list__button__open_drawer -> {
                wordListDelegate.openDrawer()
            }

            R.id.fragment_word_list__floating_button__add_word -> {
                wordListDelegate.launchAddWord()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        wordListDelegate.showLastView()
    }

    private fun onDrawerMenuClick(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.menu_word_list__all_words -> {
            wordListDelegate.showAllWords()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__archived_words -> {
            wordListDelegate.showArchivedWords()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__duplicated_words -> {
            wordListDelegate.showDuplicatedWords()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__duplicated_by_translation_words -> {
            wordListDelegate.showDuplicatedByTranslationWords()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__hard_words -> {
            wordListDelegate.showHardWords()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__clear -> {
            wordListDelegate.clearWords()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__reset_progress -> {
            wordListDelegate.resetProgress()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__restore -> {
            wordListDelegate.restoreWords()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__export_db -> {
            wordListDelegate.exportDb()
            wordListDelegate.closeDrawer()
            true
        }

        R.id.menu_word_list__export_file -> {
            wordListDelegate.exportFile()
            wordListDelegate.closeDrawer()
            true
        }

        else -> false
    }

    private val onScrollListener = object : AbsListView.OnScrollListener {
        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}

        override fun onScroll(
            view: AbsListView?,
            firstVisibleItem: Int,
            visibleItemCount: Int,
            totalItemCount: Int
        ) {
            wordListDelegate.scrollList(firstVisibleItem, visibleItemCount, totalItemCount)
        }
    }

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            wordListDelegate.search(s)
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private fun initWordListAdapter() =
        WordToListViewAdapter(
            R.layout.view__list_word_item,
            this.requireActivity(),
            levelColorDefiner,
            imageService
        )

    private fun initSearch(view: View): EditText {
        return view.findViewById<EditText>(R.id.fragment_word_list__edit_text__search_text)
            .also { search ->
                search.addTextChangedListener(searchTextWatcher)
            }
    }

    private fun initNavigationView(view: View): NavigationView {
        return view.findViewById<NavigationView>(R.id.fragment_word_list__navigation_view__drawer)
            .also { navigationView ->
                navigationView?.setNavigationItemSelectedListener { menuItem ->
                    onDrawerMenuClick(menuItem)
                }
            }
    }

    private fun initListView(view: View, listAdapter: WordToListViewAdapter): ListView {
        return view.findViewById<ListView>(R.id.fragment_word_list__list_view__main)
            .also { listView ->
                listView.isTextFilterEnabled = true
                listView.emptyView =
                    view.findViewById(R.id.fragment_word_list__text_view__no_words)
                listView.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        wordListDelegate.openListViewItem(position)
                    }
                listView.setOnScrollListener(onScrollListener)
                listView.adapter = listAdapter
            }
    }

    private fun initBackFromSearchButton(view: View): Button {
        return view.findViewById<Button>(R.id.fragment_word_list__button__back_from_search)
            .also { bachFromSearchButton ->
                bachFromSearchButton.setOnClickListener(this)
            }
    }

    private fun initSearchButton(view: View): Button {
        val searchButton = view.findViewById<Button>(R.id.fragment_word_list__button__search)
            .also { searchButton ->
                searchButton.setOnClickListener(this)
            }
        return searchButton
    }

    private fun initAddWordFloatingButton(view: View): FloatingActionButton {
        return view.findViewById<FloatingActionButton>(R.id.fragment_word_list__floating_button__add_word)
            .also { fab ->
                fab.setOnClickListener(this)
            }
    }

    private fun initDrawer(view: View): DrawerLayout {
        return view.findViewById<DrawerLayout>(R.id.drawer_layout)
            .also { drawer ->
                ActionBarDrawerToggle(
                    activity,
                    drawer,
                    R.string.list_drawer__description__open,
                    R.string.list_drawer__description__close
                ).apply { syncState() }
            }
    }

    private fun createWordAddingLauncher(): ActivityResultLauncher<String> {
        val contract = AddWordContract<String>()
        val callback =
            ActivityResultCallback<Word?> { word -> wordListDelegate.afterWordAdded(word) }
        return registerForActivityResult(contract, callback)
    }

    private fun createFileSelectorLauncher(): ActivityResultLauncher<Intent> {
        val contract = ActivityResultContracts.StartActivityForResult()
        val callback = ActivityResultCallback<ActivityResult> { result ->
            wordListDelegate.afterFileForUploadSelected(result)
        }
        return registerForActivityResult(contract, callback)
    }

    private fun createFileSavingLauncher(): ActivityResultLauncher<Intent> {
        val contract = ActivityResultContracts.StartActivityForResult()
        val callback = ActivityResultCallback<ActivityResult> { _ ->
            wordListDelegate.afterFileUploaded()
        }
        return registerForActivityResult(contract, callback)
    }

}