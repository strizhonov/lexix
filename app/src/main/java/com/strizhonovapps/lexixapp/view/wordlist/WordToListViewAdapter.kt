package com.strizhonovapps.lexixapp.view.wordlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.model.WordCardSide
import com.strizhonovapps.lexixapp.service.ImageService
import com.strizhonovapps.lexixapp.service.LevelColorDefiner
import com.strizhonovapps.lexixapp.service.WordServiceImpl.WordAddon.isHard


class WordToListViewAdapter constructor(
    resource: Int,
    context: Context,
    private val levelColorDefiner: LevelColorDefiner,
    private val imageService: ImageService,
) : ArrayAdapter<Word>(context, resource, emptyArray()) {

    private var originalValues: MutableList<Word> = ArrayList()
    private var displayedValues: MutableList<Word> = ArrayList()

    override fun getCount() = displayedValues.size
    override fun getItem(position: Int) = displayedValues[position]
    override fun getItemId(position: Int) = position.toLong()
    override fun getFilter() = WordFilter(this, displayedValues, originalValues, context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var tempView = convertView
        if (tempView == null) {
            tempView = getTempView(parent)
            viewHolder = getViewHolder(tempView)
            tempView.tag = viewHolder
        } else {
            viewHolder = tempView.tag as ViewHolder
        }
        val current = this.getItem(position)
        inflateViewHolder(current, viewHolder)
        return requireNotNull(tempView)
    }

    fun setWords(words: List<Word>) {
        originalValues.clear()
        originalValues.addAll(words)
        displayedValues.clear()
        displayedValues.addAll(words)
        super.notifyDataSetChanged()
    }

    fun addEvery(words: List<Word>) {
        originalValues.addAll(words)
        displayedValues.addAll(words)
        super.notifyDataSetChanged()
    }

    private fun inflateViewHolder(current: Word, viewHolder: ViewHolder) {
        setImage(current, viewHolder)
        viewHolder.name?.text = current.name
        viewHolder.translation?.text = current.translation
        setLevel(viewHolder, current)
        viewHolder.archive?.visibility = if (current.isArchived) View.VISIBLE else View.GONE
        viewHolder.hard?.visibility = if (current.isHard()) View.VISIBLE else View.GONE
        setTag(viewHolder, current)
        setAllowedWordSide(current, viewHolder)
    }

    private fun setAllowedWordSide(current: Word, viewHolder: ViewHolder) {
        if (current.allowedWordCardSide == WordCardSide.ALL) {
            viewHolder.side?.visibility = View.GONE
            return
        }

        viewHolder.side?.visibility = View.VISIBLE
        val res = when (current.allowedWordCardSide) {
            WordCardSide.NATIVE -> R.drawable.baseline_arrow_upward_24
            WordCardSide.STUDY -> R.drawable.baseline_arrow_downward_24
            WordCardSide.ALL -> throw IllegalStateException("No resource for ALL word side")
        }
        viewHolder.side?.setImageResource(res)
    }

    private fun setTag(viewHolder: ViewHolder, current: Word) {
        viewHolder.tag?.text = current.tag
        viewHolder.tag?.visibility = if (current.tag.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    private fun setLevel(viewHolder: ViewHolder, current: Word) {
        viewHolder.lvl?.text =
            String.format(
                "%d %s",
                current.level,
                context.getString(R.string.fragment_word_list__level_abbreviation)
            )
        val lvlBackground = levelColorDefiner.defineBackground(current.level)
        val lvlColor = levelColorDefiner.defineColor(current.level)
        viewHolder.lvl?.background = ContextCompat.getDrawable(context, lvlBackground)
        viewHolder.lvl?.setTextColor(ContextCompat.getColor(context, lvlColor))
    }

    private fun setImage(current: Word, viewHolder: ViewHolder) {
        val wordImage = imageService.getSquaredImage(current)
        if (wordImage == null) {
            viewHolder.wordImage?.visibility = View.GONE
            viewHolder.wordIcon?.visibility = View.VISIBLE

            val firstNameLetter = current.name?.take(1)?.uppercase()
            viewHolder.wordIcon?.text =
                if (firstNameLetter.isNullOrBlank()) "?" else firstNameLetter
            viewHolder.wordIcon?.background = ContextCompat.getDrawable(
                context,
                R.drawable.border__circle__accent
            )
        } else {
            val roundedDrawable = RoundedBitmapDrawableFactory.create(context.resources, wordImage)
            roundedDrawable.cornerRadius = 1000f
            viewHolder.wordImage?.setImageDrawable(roundedDrawable)

            viewHolder.wordImage?.visibility = View.VISIBLE
            viewHolder.wordIcon?.visibility = View.GONE
        }
    }

    private fun getViewHolder(tempView: View): ViewHolder {
        val viewHolder = ViewHolder()
        viewHolder.wordIcon =
            tempView.findViewById(R.id.view_list_word_item__text_view__word_no_image_icon)
        viewHolder.wordImage =
            tempView.findViewById(R.id.view_list_word_item__image_view__word_image)
        viewHolder.name = tempView.findViewById(R.id.view_list_word_item__text_view__word_name)
        viewHolder.translation =
            tempView.findViewById(R.id.view_list_word_item__text_view__translation)
        viewHolder.lvl = tempView.findViewById(R.id.view_list_word_item__text_view__level)
        viewHolder.archive = tempView.findViewById(R.id.view_list_word_item__text_view__archive_tag)
        viewHolder.hard = tempView.findViewById(R.id.view_list_word_item__text_view__hard_tag)
        viewHolder.tag = tempView.findViewById(R.id.view_list_word_item__text_view__tag)
        viewHolder.side = tempView.findViewById(R.id.view_list_word_item__side)
        return viewHolder
    }

    private fun getTempView(parent: ViewGroup) =
        LayoutInflater.from(context)
            .inflate(R.layout.view__list_word_item, parent, false)

    data class ViewHolder(
        var wordImage: ImageView? = null,
        var wordIcon: TextView? = null,
        var name: TextView? = null,
        var translation: TextView? = null,
        var lvl: TextView? = null,
        var archive: TextView? = null,
        var hard: TextView? = null,
        var tag: TextView? = null,
        var side: ImageView? = null,
    )

}