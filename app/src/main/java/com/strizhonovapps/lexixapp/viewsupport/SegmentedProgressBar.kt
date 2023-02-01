package com.strizhonovapps.lexixapp.viewsupport

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.content.ContextCompat
import com.strizhonovapps.lexixapp.R

class SegmentedProgressBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val preAllocatedRectF = RectF()
    private val progressBarBackgroundPaint: Paint = Paint()
    private val progressBarPaint: Paint = Paint()
    private val dividerPaint: Paint = Paint()

    private val dividerWidth: Float
    private val cornerRadius: Float
    private val isDividerEnabled: Boolean

    private val maxDivisions = 50
    private var shrinkCoef = 1

    private var bgRect: RectF? = null
    private var progressBarWidth = 0f
    private var divisions = 0
    private var enabledDivisions: List<Int> = ArrayList()
    private var dividerPositions: ArrayList<Float> = ArrayList()


    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SegmentedProgressBar,
            0,
            0
        )
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
                typedArray.getDimension(R.styleable.SegmentedProgressBar_dividerWidth, 0f)
            isDividerEnabled =
                typedArray.getBoolean(R.styleable.SegmentedProgressBar_isDividerEnabled, true)
            cornerRadius =
                typedArray.getDimension(R.styleable.SegmentedProgressBar_cornerRadius, 0f)
        } finally {
            typedArray.recycle()
        }

        val viewTreeObserver: ViewTreeObserver = viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (width == 0) return
                    getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    progressBarWidth = width.toFloat()
                    refreshDividerPositions()
                    bgRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                    invalidate()
                }
            })
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bgRect = bgRect ?: return

        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, progressBarBackgroundPaint)
        for (enabledDivision in enabledDivisions) {
            enableDivision(enabledDivision, canvas)
        }
        if (divisions <= 1 || !isDividerEnabled) return
        for (i in 0 until divisions - 1) {
            val dividerCenter = dividerPositions[i]
            canvas.drawRect(
                dividerCenter - dividerWidth / 2,
                0f,
                dividerCenter + dividerWidth / 2,
                height.toFloat(),
                dividerPaint
            )
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
        if (divisions >= maxDivisions) {
            shrinkCoef = divisions / maxDivisions
            this.divisions = maxDivisions
        } else {
            this.divisions = divisions
            shrinkCoef = 1
        }
        refreshDividerPositions()
        invalidate()
    }

    private fun refreshDividerPositions() {
        dividerPositions.clear()
        if (divisions <= 1) return
        for (divisionIdx in 1 until divisions) {
            val position = (progressBarWidth * divisionIdx) / divisions
            dividerPositions.add(position)
        }
    }

    fun enableFirstDivisions(count: Int) {
        this.enabledDivisions =
            if (count == 0) emptyList()
            else (0 until count)
                .map { idx -> idx.div(shrinkCoef) }
                .distinct()
                .toList()
        invalidate()
    }

    private fun enableDivision(enabledDivision: Int, canvas: Canvas) {
        if (enabledDivision >= divisions) return

        val left = if (enabledDivision == 0) 0f
        else dividerPositions[enabledDivision - 1]

        val right = if (enabledDivision >= dividerPositions.size) progressBarWidth
        else dividerPositions[enabledDivision]

        preAllocatedRectF.left = left
        preAllocatedRectF.top = 0f
        preAllocatedRectF.right = right
        preAllocatedRectF.bottom = height.toFloat()

        canvas.drawRoundRect(preAllocatedRectF, cornerRadius, cornerRadius, progressBarPaint)

        when (enabledDivision) {
            0 ->
                canvas.drawRect(
                    left + cornerRadius,
                    0f,
                    right,
                    height.toFloat(),
                    progressBarPaint
                )

            divisions - 1 ->
                canvas.drawRect(
                    left,
                    0f,
                    right - cornerRadius,
                    height.toFloat(),
                    progressBarPaint
                )

            else -> canvas.drawRect(preAllocatedRectF, progressBarPaint)
        }
    }
}