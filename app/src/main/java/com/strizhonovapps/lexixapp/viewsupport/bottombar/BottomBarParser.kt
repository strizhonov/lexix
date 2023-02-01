package com.strizhonovapps.lexixapp.viewsupport.bottombar

import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class BottomBarParser(private val context: Context, res: Int) {

    private val parser: XmlResourceParser = context.resources.getXml(res)

    fun parse(): List<BottomBarItem> {
        val items: MutableList<BottomBarItem> = mutableListOf()
        var eventType: Int?
        do {
            eventType = parser.next()
            if (eventType == XmlResourceParser.START_TAG && "item" == parser.name) {
                items.add(getTabConfig(parser))
            }
        } while (eventType != XmlResourceParser.END_DOCUMENT)
        return items.toList()
    }

    private fun getTabConfig(parser: XmlResourceParser): BottomBarItem {
        val attributeCount = parser.attributeCount
        var itemText: String? = null
        var itemDrawable: Drawable? = null
        for (i in 0 until attributeCount) {
            when (parser.getAttributeName(i)) {
                "title" -> itemText = context.getString(parser.getAttributeResourceValue(i, 0))
                "icon" -> itemDrawable =
                    ContextCompat.getDrawable(context, parser.getAttributeResourceValue(i, 0))
            }
        }
        return BottomBarItem(
            "  $itemText",
            itemDrawable ?: throw IllegalStateException("Unable to find icon for bottom bar."),
            alpha = 0
        )
    }
}