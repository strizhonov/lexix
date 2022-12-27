package com.strizhonovapps.lexicaapp.viewsupport

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.content.ContextCompat
import com.strizhonovapps.lexicaapp.R

class SegmentedProgressBar : View {

    var bgRect: RectF? = null

    private val progressBarBackgroundPaint: Paint = Paint()
    private val progressBarPaint: Paint = Paint()
    private val dividerPaint: Paint = Paint()
    private var progressBarWidth = 0
    private var dividerWidth = 1f
    private var isDividerEnabled = true
    private var divisions = 0
    private var enabledDivisions: List<Int> = ArrayList()
    private var dividerPositions: ArrayList<Float>? = null
    private var cornerRadius = 4f

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        dividerPositions = ArrayList()
        cornerRadius = 0f
        val typedArray: TypedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.SegmentedProgressBar, 0, 0)
        try {
            dividerPaint.color = typedArray.getColor(
                R.styleable.SegmentedProgressBar_dividerColor,
                ContextCompat.getColor(context, R.color.light_3)
            )
            progressBarBackgroundPaint.color = typedArray.getColor(
                R.styleable.SegmentedProgressBar_progressBarBackgroundColor,
                ContextCompat.getColor(context, R.color.gray)
            )
            progressBarPaint.color = typedArray.getColor(
                R.styleable.SegmentedProgressBar_progressBarColor,
                ContextCompat.getColor(context, R.color.accent)
            )
            dividerWidth =
                typedArray.getDimension(R.styleable.SegmentedProgressBar_dividerWidth, dividerWidth)
            isDividerEnabled =
                typedArray.getBoolean(R.styleable.SegmentedProgressBar_isDividerEnabled, true)
            divisions = typedArray.getInteger(R.styleable.SegmentedProgressBar_divisions, divisions)
            cornerRadius =
                typedArray.getDimension(R.styleable.SegmentedProgressBar_cornerRadius, 2f)
        } finally {
            typedArray.recycle()
        }
        val viewTreeObserver: ViewTreeObserver = viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (width > 0) {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this)
                        progressBarWidth = width
                        dividerPositions?.clear()
                        if (divisions > 1) {
                            for (i in 1 until divisions) {
                                dividerPositions?.add((progressBarWidth * i).toFloat() / divisions)
                            }
                        }
                        bgRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                        invalidate()

                    }
                }
            })
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bgRect != null) {
            canvas.drawRoundRect(bgRect!!, cornerRadius, cornerRadius, progressBarBackgroundPaint)
            for (enabledDivision in enabledDivisions) {
                if (enabledDivision < divisions) {
                    var left = 0f
                    if (enabledDivision != 0) {
                        left = dividerPositions!![enabledDivision - 1] + dividerWidth
                    }
                    val right =
                        if (enabledDivision >= dividerPositions!!.size) progressBarWidth.toFloat() else dividerPositions!![enabledDivision]
                    val rect = RectF(left, 0f, right, height.toFloat())
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, progressBarPaint)
                    when (enabledDivision) {
                        0 -> {
                            canvas.drawRect(
                                left + cornerRadius,
                                0f,
                                right,
                                height.toFloat(),
                                progressBarPaint
                            )
                        }
                        divisions - 1 -> {
                            canvas.drawRect(
                                left,
                                0f,
                                right - cornerRadius,
                                height.toFloat(),
                                progressBarPaint
                            )
                        }
                        else -> {
                            canvas.drawRect(rect, progressBarPaint)
                        }
                    }
                }
            }
            if (divisions > 1 && isDividerEnabled) {
                for (i in 1 until divisions) {
                    val leftPosition = dividerPositions!![i - 1]
                    canvas.drawRect(
                        leftPosition,
                        0f,
                        leftPosition + dividerWidth,
                        height.toFloat(),
                        dividerPaint
                    )
                }
            }
        }
    }

    override fun setBackgroundColor(color: Int) {
        progressBarBackgroundPaint.color = color
    }

    fun setDivisions(divisions: Int) {
        if (divisions < 1) {
            Log.w(this.javaClass.name, "setDivisions: Number of Divisions cannot be less than 1")
            return
        }
        this.divisions = divisions
        dividerPositions!!.clear()
        if (divisions > 1) {
            for (i in 1 until divisions) {
                dividerPositions!!.add((progressBarWidth * i).toFloat() / divisions)
            }
        }
        invalidate()
    }

    fun setEnabledDivisions(enabledDivisions: List<Int>) {
        this.enabledDivisions = enabledDivisions
        invalidate()
    }
}