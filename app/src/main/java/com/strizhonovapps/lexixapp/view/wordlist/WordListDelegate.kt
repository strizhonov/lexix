package com.strizhonovapps.lexixapp.view.wordlist

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.dao.allArchived
import com.strizhonovapps.lexixapp.dao.allAvailable
import com.strizhonovapps.lexixapp.dao.allHard
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.service.WordMetadataFacadeService
import com.strizhonovapps.lexixapp.service.WordService
import com.strizhonovapps.lexixapp.service.WordsTokenizer
import com.strizhonovapps.lexixapp.view.ModifyWordActivity
import com.strizhonovapps.lexixapp.view.WordIntentExtraMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

internal const val SEPARATOR_EXTRA_KEY = "separator"

@Singleton
class WordListDelegate @Inject constructor() {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var wordMetadataFacadeService: WordMetadataFacadeService

    @Inject
    lateinit var inputMethodManager: InputMethodManager

    @Inject
    lateinit var wordsTokenizer: WordsTokenizer

    private lateinit var wordListAdapter: WordToListViewAdapter
    private lateinit var activity: Activity
    private lateinit var components: WordListComponents
    private lateinit var wordAddingLauncher: ActivityResultLauncher<String>
    private lateinit var fileSelectorLauncher: ActivityResultLauncher<Intent>
    private lateinit var fileSavingLauncher: ActivityResultLauncher<Intent>

    private val inputFlag = InputMethodManager.SHOW_IMPLICIT
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    private var batch = WordsBatch(0)
    private var latestWordsFn = { b: WordsBatch? -> wordService.findAll(allAvailable, b, true) }
    private var latestCountFn = { wordService.count(allAvailable) }

    fun lateInit(
        components: WordListComponents,
        wordListAdapter: WordToListViewAdapter,
        wordAddingLauncher: ActivityResultLauncher<String>,
        fileSelectorLauncher: ActivityResultLauncher<Intent>,
        fileSavingLauncher: ActivityResultLauncher<Intent>,
        activity: Activity,
    ) {
        this.activity = activity
        this.components = components
        this.wordAddingLauncher = wordAddingLauncher
        this.fileSelectorLauncher = fileSelectorLauncher
        this.fileSavingLauncher = fileSavingLauncher
        this.wordListAdapter = wordListAdapter
    }

    fun showAllWords() {
        batch.reset()
        latestWordsFn = { batch -> wordService.findAll(allAvailable, batch, true) }
        latestCountFn = { wordService.count(allAvailable) }
        refreshListWithCount()
        setListName(R.string.fragment_word_list__group_title__all_words)
    }

    fun showArchivedWords() {
        batch.reset()
        latestWordsFn = { batch -> wordService.findAll(allArchived, batch, true) }
        latestCountFn = { wordService.count(allArchived) }
        refreshListWithCount()
        setListName(R.string.fragment_word_list__group_title__archived_words)
    }

    fun showHardWords() {
        batch.reset()
        latestWordsFn = { batch -> wordService.findAll(allHard, batch, true) }
        latestCountFn = { wordService.count(allHard) }
        refreshListWithCount()
        setListName(R.string.fragment_word_list__group_title__hard_words)
    }

    fun showDuplicatedWords() {
        latestWordsFn = { wordService.findDuplicatesByName() }
        latestCountFn = { wordService.findDuplicatesByName().size.toLong() }
        refreshListWithCount()
        setListName(R.string.fragment_word_list__group_title__duplicates)
    }

    fun showDuplicatedByTranslationWords() {
        latestWordsFn = { wordService.findDuplicatesByTranslation() }
        latestCountFn = { wordService.findDuplicatesByTranslation().size.toLong() }
        refreshListWithCount()
        setListName(R.string.fragment_word_list__group_title__duplicates_native)
    }

    fun scrollList(firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (totalItemCount < batch.end()) return
        if (firstVisibleItem + visibleItemCount < totalItemCount) return
        batch = batch.next()
        val wordsToAdd = latestWordsFn(batch)
        wordListAdapter.addEvery(wordsToAdd)
    }

    fun showLastView() {
        hideSearch(null)
        batch.reset()
        refreshListWithCount()
        val listNameText = components.wordLabelTextView.text
        if (listNameText.isNullOrBlank()) {
            setListName(R.string.fragment_word_list__group_title__all_words)
        }
    }

    fun hideSearch(windowToken: IBinder?) {
        components.shownSearchBlock.visibility = View.GONE
        components.wordCounterBlock.visibility = View.VISIBLE
        wordListAdapter.filter.filter("")
        windowToken.let { window -> inputMethodManager.hideSoftInputFromWindow(window, inputFlag) }
    }

    fun showSearch() {
        components.shownSearchBlock.visibility = View.VISIBLE
        components.wordCounterBlock.visibility = View.GONE
        components.search.requestFocus()
        inputMethodManager.showSoftInput(components.search, inputFlag)
    }

    fun openDrawer() {
        components.drawer.openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        components.drawer.closeDrawer(GravityCompat.START)
    }

    fun launchAddWord() {
        wordAddingLauncher.launch("")
    }

    fun openListViewItem(position: Int) {
        val presentId = components.listView.getItemAtPosition(position) as Word
        val word = wordService.get(presentId.id) ?: return
        val modifyIntent = Intent(activity, ModifyWordActivity::class.java)
        WordIntentExtraMapper(modifyIntent).setWord(word)
        activity.startActivity(modifyIntent)
    }

    fun search(constraint: CharSequence) {
        CoroutineScope(Dispatchers.IO).launch {
            val allNeededWords = latestWordsFn(null)
            CoroutineScope(Dispatchers.Main).launch {
                wordListAdapter.setWords(allNeededWords)
                wordListAdapter.filter.filter(constraint.toString())
            }
        }
    }

    fun clearWords() {
        ListDrawerDialogFactory(activity)
            .getEraseConfirmationDialog {
                wordService.erase()
                components.drawer.closeDrawer(GravityCompat.START)
                refreshListWithCount()
            }
            .show()
    }

    fun resetProgress() {
        ListDrawerDialogFactory(activity)
            .getResetProgressDialog {
                wordService.resetProgress()
                components.drawer.closeDrawer(GravityCompat.START)
                refreshListWithCount()
            }
            .show()
    }

    fun restoreWords() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/plain"
        fileSelectorLauncher.launch(
            Intent.createChooser(
                intent,
                activity.getString(R.string.dialog__content__select_file)
            )
        )
    }

    fun exportDb() {
        val intent = Intent(Intent.ACTION_SEND)
        val dateTime = LocalDateTime.now().format(formatter)
        val fileName = "LEXIX_DB_${dateTime}.DB"
        val fileWritingFn: () -> Unit = {
            activity.openFileOutput(fileName, Context.MODE_PRIVATE).use { fos ->
                val dbFile = activity.getDatabasePath("LEXIX_DB.DB")
                dbFile.inputStream().use { input ->
                    input.copyTo(fos)
                }
            }
        }
        val fileUri = createFile(fileName, fileWritingFn, intent)
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        activity.startActivity(intent)
    }

    fun exportFile() {
        val intent = Intent(Intent.ACTION_SEND)
        val dateTime = LocalDateTime.now().format(formatter)
        val fileName = "Lexix_${dateTime}.txt"
        val fileWritingFn: () -> Unit = {
            activity.openFileOutput(fileName, Context.MODE_PRIVATE).use { fos ->
                val allWords = wordService.findAll()
                val wordsAsString = wordsTokenizer.tokenize(allWords)
                fos.write(wordsAsString.toByteArray())
            }
        }
        val fileUri = createFile(fileName, fileWritingFn, intent)
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        activity.startActivity(intent)
    }

    fun afterWordAdded(word: Word?) {
        if (word == null) return

        refreshListWithCount()
        CoroutineScope(Dispatchers.IO).launch {
            val name = word.name
            if (name == null) {
                Log.w(this::class.java.name, "Word with id=${word.id} was not properly initialized")
                return@launch
            }
            val image = wordMetadataFacadeService.saveMetadata(word.id, name).first
            updateListItemImage(word.id, image)
        }
    }

    fun afterFileForUploadSelected(result: ActivityResult) {
        result.let(ActivityResult::getData)?.let { data -> showFileUploadingDialogOrToast(data) }
    }

    fun afterFileUploaded() {
        refreshListWithCount()
    }

    private fun updateListItemImage(id: Long, img: Bitmap?) {
        for (listViewIdx in 0 until wordListAdapter.count) {
            val word = wordListAdapter.getItem(listViewIdx)
            if (word.id != id) continue
            val neededWordView = components.listView.getChildAt(listViewIdx)
            val wordImage =
                neededWordView?.findViewById<ImageView>(R.id.view_list_word_item__image_view__word_image)
            CoroutineScope(Dispatchers.Main).launch {
                wordImage?.setImageBitmap(img)
                wordListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showFileUploadingDialogOrToast(data: Intent) {
        try {
            showUploadingDialog(data.data ?: throw IllegalStateException("File not uploaded"))
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Unable to save words from file.", e)
            Toast.makeText(
                activity,
                activity.getString(R.string.fragment_word_list__toast__unable_to_save_from_file),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showUploadingDialog(data: Uri) {
        val separatorEditText = EditText(activity)
        val listener = { _: DialogInterface, _: Int ->
            val separator = separatorEditText.text.toString()
            val fileSavingIntent = Intent(activity, FromFileSavingBackgroundActivity::class.java)
            fileSavingIntent.data = data
            fileSavingIntent.putExtra(SEPARATOR_EXTRA_KEY, separator)
            fileSavingLauncher.launch(fileSavingIntent)
        }
        ListDrawerDialogFactory(activity)
            .getSeparatorDialog(listener, separatorEditText)
            .show()
    }

    private fun createFile(fileName: String, fileWritingFn: () -> Unit, intent: Intent): Uri? {
        fileWritingFn()
        intent.type = "text/plain"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        return FileProvider.getUriForFile(
            activity,
            activity.packageName + ".provider",
            File(activity.filesDir, fileName)
        )
    }

    private fun refreshListWithCount() {
        CoroutineScope(Dispatchers.IO).launch {
            val count = latestCountFn()
            val words = latestWordsFn(batch)
            CoroutineScope(Dispatchers.Main).launch {
                components.wordCounterTextView.text = count.toString()
                wordListAdapter.setWords(words)
            }
        }
    }

    private fun setListName(listNameRes: Int) {
        components.wordLabelTextView.text = activity.getString(listNameRes)
    }

}