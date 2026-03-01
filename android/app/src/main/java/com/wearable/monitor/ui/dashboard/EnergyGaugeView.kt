package com.wearable.monitor.ui.dashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.wearable.monitor.R

class EnergyGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var score: Int = 0
    private var maxScore: Int = 100

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFFE8E0FF.toInt()
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val rect = RectF()

    fun setScore(value: Int) {
        score = value.coerceIn(0, maxScore)
        progressPaint.color = when {
            score >= 80 -> ContextCompat.getColor(context, R.color.ok)
            score >= 50 -> ContextCompat.getColor(context, R.color.accent)
            else -> ContextCompat.getColor(context, R.color.danger)
        }
        textPaint.color = progressPaint.color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val strokeWidth = resources.getDimension(R.dimen.energy_circle_stroke)
        bgPaint.strokeWidth = strokeWidth
        progressPaint.strokeWidth = strokeWidth

        val padding = strokeWidth / 2f
        rect.set(padding, padding, width - padding, height - padding)

        // Background circle
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)

        // Progress arc
        val sweepAngle = (score.toFloat() / maxScore) * 360f
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        // Center text
        textPaint.textSize = width * 0.23f
        val textY = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(score.toString(), width / 2f, textY, textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = resources.getDimensionPixelSize(R.dimen.energy_circle_size)
        val resolvedWidth = resolveSize(size, widthMeasureSpec)
        val resolvedHeight = resolveSize(size, heightMeasureSpec)
        val finalSize = minOf(resolvedWidth, resolvedHeight)
        setMeasuredDimension(finalSize, finalSize)
    }
}
