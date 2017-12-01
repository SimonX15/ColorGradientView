package com.app.simon.viewlib

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import org.jetbrains.anko.sp


/**
 * desc: 渐变色的圆
 * date: 2017/11/30
 *
 * @author xw
 */
class SweepGradientCircleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    /** 圆弧画笔 */
    private val circlePaint = Paint()
    /** 次数画笔 */
    private val timesPaint = Paint()
    /** 文字画笔 */
    private val textPaint = Paint()

    /** 次数画笔宽度 */
    private var timesPaintWidth = CIRCLE_PAINT_WIDTH

    /** 次数字体大小 */
    private var timesSize = sp(TIMES_TEXT_SIZE).toFloat()

    /** 文本字体大小 */
    private var textSize = sp(TEXT_TEXT_SIZE).toFloat()

    /** 默认开始颜色 */
    private val defaultStartColor = ContextCompat.getColor(context, R.color.orange_start)

    /** 默认结束颜色 */
    private val defaultEndColor = ContextCompat.getColor(context, R.color.orange_end)

    /** 颜色变化 */
    private val colorsRight = intArrayOf(defaultStartColor, defaultEndColor)

    /**
     * 填充色主要参数：
     * colors[]
     * positions[]
     * 即每个position定义一个color值，注意position是一个相对位置，其值应该在0.0到1.0之间。
     * 18.5/24.0/28.0/35.0
     */
    private val colorsRightPositions = floatArrayOf(0f, 1f)

    /** 是否完整的圆 */
    private var isCompleteCircle = true

    /** 默认的动效周期 2s */
    private var duration = ANIM_DURATION

    /** 当前进度 */
    private var currentProgress = 0f

    /** 最大进度 */
    private var maxProgress = 360f

    /** 当前次数 */
    private var currentTimes = 0

    /** 最大的次数 */
    private var maxTimes = 2000

    /** 渐变类型 */
    lateinit var gradientType: GradientType

    /** 动画 */
    private var valueAnimator: ValueAnimator? = null

    init {
        initParams(attrs)
        initPaint()
    }

    /** 初始化参数 */
    private fun initParams(attrs: AttributeSet?) {
        attrs?.run {

        }
    }

    /** 初始化画笔 */
    private fun initPaint() {
        circlePaint.color = Color.BLUE
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = timesPaintWidth
        circlePaint.strokeCap = Paint.Cap.ROUND
        circlePaint.isAntiAlias = true

        timesPaint.color = ContextCompat.getColor(context, R.color.orange_end)
        timesPaint.textSize = timesSize
        timesPaint.style = Paint.Style.STROKE
        timesPaint.textAlign = Paint.Align.CENTER
        timesPaint.isAntiAlias = true

        textPaint.color = Color.GRAY
        textPaint.textSize = textSize
        textPaint.style = Paint.Style.STROKE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isAntiAlias = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimator()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        drawCircle(canvas)
        drawText(canvas)
    }

    /** 画圆 */
    private fun drawCircle(canvas: Canvas) {
        //画一个区域
        //        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        //        canvas.drawRect(rectF, textPaint)
        val linearGradient = LinearGradient(width / 2f, 0f, width / 2f, height.toFloat(), colorsRight, null, Shader.TileMode.REPEAT)

        val circleRectF = RectF(timesPaintWidth / 2, timesPaintWidth / 2, width - timesPaintWidth / 2, height - timesPaintWidth / 2)
        val sweepGradient = SweepGradient(width / 2f, height / 2f, colorsRight, colorsRightPositions)
        /*val matrix = Matrix()
        matrix.setRotate(1f, width / 2f, height / 2f) //加上旋转还是很有必要的，每次最右边总是有一部分多余了,不太美观,也可以不加
        sweepGradient.setLocalMatrix(matrix)*/
        //        circlePaint.shader = sweepGradient
        circlePaint.shader = linearGradient

        //当前进度，从90°（正下），画360°的圆弧
        canvas.drawArc(circleRectF, -90f, currentProgress * 360f, false, circlePaint)
    }

    /** 写字 */
    private fun drawText(canvas: Canvas) {
        val fontMetrics = timesPaint.fontMetrics
        val top = fontMetrics.top //为基线到字体上边框的距离,即上图中的top
        val bottom = fontMetrics.bottom //为基线到字体下边框的距离,即上图中的bottom

        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val centerX = rectF.centerX()
        val centerY = rectF.centerY() - top / 2 - bottom / 2 //基线中间点的y轴计算公式

        val times = currentTimes.toString()
        val timesText = "次"

        val leftLength = timesPaint.measureText(times)
        val rightLength = textPaint.measureText(timesText)

        canvas.drawText(times, centerX - rightLength / 2, centerY, timesPaint)
        canvas.drawText(timesText, centerX + leftLength / 2, centerY, textPaint)

        //        canvas.drawLine(0f, height / 2.toFloat(), width.toFloat(), height / 2.toFloat(), textPaint)
        //        canvas.drawLine(width / 2.toFloat(), 0f, width / 2.toFloat(), height.toFloat(), textPaint)
    }


    /** 设置进度 */
    fun setProgress(maxTimes: Int, maxProgress: Float) {
        this.maxProgress = maxProgress
        this.maxTimes = maxTimes
        currentProgress = 0f
        startAnimator()
    }

    /** 开始动画 */
    private fun startAnimator() {
        if (isInEditMode) {
            return
        }

        valueAnimator?.run {
            if (isRunning) {
                cancel()
            }
        }
        valueAnimator = ValueAnimator.ofFloat(currentProgress, maxProgress)
        valueAnimator?.duration = duration
        valueAnimator?.interpolator = DecelerateInterpolator()
        valueAnimator?.addUpdateListener {
            val value = it.animatedValue as Float
            currentProgress = value / maxProgress
            currentTimes = (currentProgress * maxTimes).toInt()
            invalidate()
        }
        valueAnimator?.start()
    }

    companion object {
        /** 次数字体大小 */
        private val TIMES_TEXT_SIZE = 40

        /** 文本字体大小 */
        private val TEXT_TEXT_SIZE = 16

        /** 默认的动效周期 2s */
        private val ANIM_DURATION: Long = 2000

        /** 圆的画笔宽度 */
        private val CIRCLE_PAINT_WIDTH = 40f
    }
}


enum class GradientType(i: Int) {
    LinearType(0), SweepType(1)
}