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

    /** 默认的动效周期 2s */
    private var duration = ANIM_DURATION

    /** 起始角度 */
    private var startAngle = DEFAULT_START_ANGLE

    /** 圆的百分比 */
    private var circlePercent = 1f

    /** 当前百分比 */
    private var currentAnglePercent = 0f

    /** 最大角度 */
    private var maxAngle = circlePercent * 360

    /** 当前次数 */
    private var currentNum = 0

    /** 最大的次数 */
    private var maxNum = 2000

    /** 单位 */
    private var unit = "次"

    /** 渐变类型 */
    var gradientType: GradientType? = null

    /** 动画 */
    private var valueAnimator: ValueAnimator? = null

    init {
        initParams(attrs)
        initData()
        initPaint()

    }

    /** 初始化参数 */
    private fun initParams(attrs: AttributeSet?) {
        attrs?.run {

        }
    }

    /** 初始化数据 */
    private fun initData() {
        gradientType = GradientType.LinearType
        when (gradientType) {
            GradientType.LinearType -> {
            }
            GradientType.SweepType -> {
            }
            else -> {
            }
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
        canvas.drawArc(circleRectF, startAngle, currentAnglePercent * maxAngle, false, circlePaint)
    }

    /** 写字 */
    private fun drawText(canvas: Canvas) {
        val fontMetrics = timesPaint.fontMetrics
        //为基线到字体上边框的距离,即上图中的top
        val top = fontMetrics.top
        //为基线到字体下边框的距离,即上图中的bottom
        val bottom = fontMetrics.bottom

        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val centerX = rectF.centerX()
        //基线中间点的y轴计算公式
        val centerY = rectF.centerY() - top / 2 - bottom / 2

        val times = currentNum.toString()


        val leftLength = timesPaint.measureText(times)
        val rightLength = textPaint.measureText(unit)

        canvas.drawText(times, centerX - rightLength / 2, centerY, timesPaint)
        canvas.drawText(unit, centerX + leftLength / 2, centerY, textPaint)
    }


    /** 设置进度 */
    fun setProgress(maxTimes: Int, percent: Float = 1f) {
        when {
            percent < 0 -> this.circlePercent = 0f
            percent > 1 -> this.circlePercent = 1f
            else -> this.circlePercent = percent
        }
        this.maxAngle = circlePercent * 360
        this.maxNum = maxTimes
        currentAnglePercent = 0f
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
        valueAnimator = ValueAnimator.ofFloat(currentAnglePercent, maxAngle)
        valueAnimator?.duration = duration
        valueAnimator?.interpolator = DecelerateInterpolator()
        valueAnimator?.addUpdateListener {
            val value = it.animatedValue as Float
            currentAnglePercent = value / maxAngle
            currentNum = (currentAnglePercent * maxNum).toInt()
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

        /** 默认起始角度，-90表示12点钟方向 */
        private val DEFAULT_START_ANGLE = -90f
    }
}

/** 渐变类型 */
enum class GradientType() {
    LinearType, SweepType
}