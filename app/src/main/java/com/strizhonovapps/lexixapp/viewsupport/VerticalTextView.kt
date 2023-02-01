package com.strizhonovapps.lexixapp.viewsupport

import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class VerticalTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    private val gravityBottom: Boolean

    init {
        val gravity: Int = super.getGravity()
        gravityBottom = Gravity.isVertical(gravity)
                && gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.BOTTOM
        if (gravityBottom) {
            setGravity(gravity and Gravity.HORIZONTAL_GRAVITY_MASK or Gravity.TOP)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        super.setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        val textPaint: TextPaint = paint
        textPaint.color = currentTextColor
        textPaint.drawableState = drawableState
        canvas.save()
        if (gravityBottom) {
            canvas.translate(0f, height.toFloat())
            canvas.rotate(-90f)
        } else {
            canvas.translate(width.toFloat(), 0f)
            canvas.rotate(90f)
        }
        canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())
        layout.draw(canvas)
        canvas.restore()
    }

}