package com.strizhonovapps.lexixapp.viewsupport

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.model.IconedAndNamed

class WithIconListAdapterProvider(private val context: Context, private val textSize: Float) {

    fun getListAdapter(dataArray: Array<out IconedAndNamed>) =
        object : ArrayAdapter<IconedAndNamed>(
            context,
            R.layout.spinner_lang__dropdown_item,
            android.R.id.text1,
            dataArray
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                getCustomView(position, convertView, parent)

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View = getCustomView(position, convertView, parent)

            private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val itemTextView = view.findViewById<TextView>(android.R.id.text1)
                itemTextView.textSize = textSize
                itemTextView.setCompoundDrawablesWithIntrinsicBounds(
                    dataArray[position].getIconRes(),
                    0,
                    0,
                    0
                )
                val nameRes = dataArray[position].getNameRes()
                itemTextView.text = context.getString(nameRes)
                itemTextView.compoundDrawablePadding =
                    (5 * context.resources.displayMetrics.density + 5f).toInt()
                return view
            }
        }

}