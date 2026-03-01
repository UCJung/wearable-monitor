package com.wearable.monitor.ui.setup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.wearable.monitor.R

enum class StepState { DONE, CURRENT, TODO }

class StepIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var stepCount = 5
    private var stepStates: List<StepState> = List(stepCount) { StepState.TODO }

    private val nodeSize = resources.getDimensionPixelSize(R.dimen.step_node_size).toFloat()
    private val lineHeight = resources.getDimensionPixelSize(R.dimen.step_line_height).toFloat()
    private val glowRadius = 4f * resources.displayMetrics.density

    private val paintNodeDone = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val paintNodeCurrent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val paintNodeTodo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(64, 255, 255, 255)  // 25% white
        style = Paint.Style.FILL
    }

    private val paintGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(128, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = glowRadius
    }

    private val paintLineDone = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(204, 255, 255, 255)  // 80% white
        strokeWidth = lineHeight
        strokeCap = Paint.Cap.ROUND
    }

    private val paintLineTodo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(77, 255, 255, 255)  // 30% white
        strokeWidth = lineHeight
        strokeCap = Paint.Cap.ROUND
    }

    private val paintTextDone = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.primary)
        textSize = 12f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val paintTextCurrent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.primary)
        textSize = 12f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val paintTextTodo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(179, 255, 255, 255)  // 70% white
        textSize = 12f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
    }

    private val paintCheck = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.primary)
        textSize = 14f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
    }

    fun setSteps(states: List<StepState>) {
        stepStates = states
        stepCount = states.size
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (nodeSize + glowRadius * 2 + 8 * resources.displayMetrics.density).toInt()
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (stepCount == 0) return

        val radius = nodeSize / 2f
        val centerY = height / 2f
        val totalWidth = width.toFloat()
        val padding = radius + glowRadius
        val availableWidth = totalWidth - padding * 2
        val spacing = if (stepCount > 1) availableWidth / (stepCount - 1) else 0f

        // 연결선 그리기
        for (i in 0 until stepCount - 1) {
            val startX = padding + i * spacing + radius
            val endX = padding + (i + 1) * spacing - radius
            val linePaint = if (stepStates[i] == StepState.DONE) paintLineDone else paintLineTodo
            canvas.drawLine(startX, centerY, endX, centerY, linePaint)
        }

        // 노드 그리기
        for (i in 0 until stepCount) {
            val cx = padding + i * spacing
            val state = stepStates[i]

            when (state) {
                StepState.DONE -> {
                    canvas.drawCircle(cx, centerY, radius, paintNodeDone)
                    // 체크 마크
                    val textY = centerY - (paintCheck.descent() + paintCheck.ascent()) / 2
                    canvas.drawText("✓", cx, textY, paintCheck)
                }
                StepState.CURRENT -> {
                    // 글로우 효과
                    canvas.drawCircle(cx, centerY, radius + glowRadius / 2, paintGlow)
                    canvas.drawCircle(cx, centerY, radius, paintNodeCurrent)
                    val textY = centerY - (paintTextCurrent.descent() + paintTextCurrent.ascent()) / 2
                    canvas.drawText("${i + 1}", cx, textY, paintTextCurrent)
                }
                StepState.TODO -> {
                    canvas.drawCircle(cx, centerY, radius, paintNodeTodo)
                    val textY = centerY - (paintTextTodo.descent() + paintTextTodo.ascent()) / 2
                    canvas.drawText("${i + 1}", cx, textY, paintTextTodo)
                }
            }
        }
    }
}
