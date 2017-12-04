package com.app.simon.viewlib

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp


/**
 * desc: 渐变色的圆
 * date: 2017/11/30
 *
 * @author xw
 */
class SweepGradientCircleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    /** 默认数量字体颜色 */
    private val defaultNumColor = ContextCompat.getColor(context, R.color.orange_text)
    /** 单位字体颜色 */
    private val defaultUnitColor = Color.GRAY
    /** 默认开始颜色 */
    private val defaultStartColor = ContextCompat.getColor(context, R.color.orange_start)
    /** 默认结束颜色 */
    private val defaultEndColor = ContextCompat.getColor(context, R.color.orange_end)

    /** 圆弧画笔 */
    private val circlePaint = Paint()
    /** 数量画笔 */
    private val numPaint = Paint()
    /** 单位画笔 */
    private val unitPaint = Paint()

    /** 圆弧画笔宽度 */
    private var circlePaintWidth = CIRCLE_PAINT_WIDTH

    /** 数量字体大小 */
    private var numTextSize = sp(NUM_TEXT_SIZE).toFloat()
    /** 数量字体颜色 */
    private var numTextColor = defaultNumColor

    /** 单位字体大小 */
    private var unitTextSize = sp(UNIT_TEXT_SIZE).toFloat()
    /** 单位字体颜色 */
    private var unitTextColor = defaultUnitColor

    /** 颜色变化 */
    private val colorsArray = intArrayOf(defaultStartColor, defaultEndColor)

    /**
     * 填充色主要参数：
     * colors[]
     * positions[]
     * 即每个position定义一个color值，注意position是一个相对位置，其值应该在0.0到1.0之间。
     * 18.5/24.0/28.0/35.0
     */
    private val colorsPositions = floatArrayOf(0f, 1f)

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

    /** 当前数量 */
    private var currentNum = 0

    /** 最大的数量 */
    private var maxNum = 0

    /** 单位 */
    private var unit = "次"

    /** 渐变类型 */
    private var gradientType = GradientType.LinearType

    /** 动画 */
    private var valueAnimator: ValueAnimator? = null

    init {
        initParams(attrs)
        initPaint()
    }

    /** 初始化参数 */
    private fun initParams(attrs: AttributeSet?) {
        attrs?.run {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SweepGradientCircleView)
            typedArray.run {
                //起止的颜色
                val start = getColor(R.styleable.SweepGradientCircleView_circleColorStart, defaultStartColor)
                val end = getColor(R.styleable.SweepGradientCircleView_circleColorEnd, defaultEndColor)
                colorsArray[0] = start
                colorsArray[1] = end

                circlePaintWidth = getDimension(R.styleable.SweepGradientCircleView_circleWidth, dip(CIRCLE_PAINT_WIDTH).toFloat())

                numTextSize = getDimension(R.styleable.SweepGradientCircleView_numTextSize, sp(NUM_TEXT_SIZE).toFloat())
                numTextColor = getColor(R.styleable.SweepGradientCircleView_numTextColor, defaultNumColor)

                unitTextSize = getDimension(R.styleable.SweepGradientCircleView_unitTextSize, sp(UNIT_TEXT_SIZE).toFloat())
                unitTextColor = getColor(R.styleable.SweepGradientCircleView_unitTextColor, defaultUnitColor)

                duration = getInt(R.styleable.SweepGradientCircleView_animDuration, ANIM_DURATION)
                startAngle = getFloat(R.styleable.SweepGradientCircleView_startAngle, DEFAULT_START_ANGLE)

                val type = getInt(R.styleable.SweepGradientCircleView_gradientType, 0)
                gradientType = when (type) {
                    1 -> {
                        GradientType.SweepType
                    }
                    else -> {
                        GradientType.LinearType
                    }
                }
            }
            typedArray.recycle()
        }
    }

    /** 初始化画笔 */
    private fun initPaint() {
        circlePaint.color = Color.BLUE
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = circlePaintWidth
        circlePaint.strokeCap = Paint.Cap.ROUND
        circlePaint.isAntiAlias = true

        val numColor = ContextCompat.getColor(context, R.color.orange_end)
        numPaint.color = numColor
        numPaint.textSize = numTextSize
        numPaint.style = Paint.Style.STROKE
        numPaint.textAlign = Paint.Align.CENTER
        numPaint.isAntiAlias = true

        unitPaint.color = Color.GRAY
        unitPaint.textSize = unitTextSize
        unitPaint.style = Paint.Style.STROKE
        unitPaint.textAlign = Paint.Align.CENTER
        unitPaint.isAntiAlias = true
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
        //        canvas.drawRect(rectF, unitPaint)

        val circleRectF = RectF(circlePaintWidth / 2, circlePaintWidth / 2, width - circlePaintWidth / 2, height - circlePaintWidth / 2)

        when (gradientType) {
            GradientType.LinearType -> {
                val start = width / 2f
                val top = 0f
                val end = width / 2f
                val bottom = height.toFloat()
                val linearGradient = LinearGradient(start, top, end, bottom, colorsArray, null, Shader.TileMode.REPEAT)
                circlePaint.shader = linearGradient
            }
            GradientType.SweepType -> {
                val sweepGradient = SweepGradient(width / 2f, height / 2f, colorsArray, colorsPositions)
                /*val matrix = Matrix()
                matrix.setRotate(1f, width / 2f, height / 2f) //加上旋转还是很有必要的，每次最右边总是有一部分多余了,不太美观,也可以不加
                sweepGradient.setLocalMatrix(matrix)*/
                circlePaint.shader = sweepGradient
            }
        }

        //当前进度，从90°（正下），画360°的圆弧
        canvas.drawArc(circleRectF, startAngle, currentAnglePercent * maxAngle, false, circlePaint)
    }

    /** 写字 */
    private fun drawText(canvas: Canvas) {
        val fontMetrics = numPaint.fontMetrics
        //为基线到字体上边框的距离,即上图中的top
        val top = fontMetrics.top
        //为基线到字体下边框的距离,即上图中的bottom
        val bottom = fontMetrics.bottom

        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val centerX = rectF.centerX()
        //基线中间点的y轴计算公式
        val centerY = rectF.centerY() - top / 2 - bottom / 2

        val times = currentNum.toString()


        val leftLength = numPaint.measureText(times)
        val rightLength = unitPaint.measureText(unit)

        canvas.drawText(times, centerX - rightLength / 2, centerY, numPaint)
        canvas.drawText(unit, centerX + leftLength / 2, centerY, unitPaint)
    }


    /** 设置进度 */
    fun setProgress(maxNum: Int, percent: Float = 1f) {
        when {
            percent < 0 -> this.circlePercent = 0f
            percent > 1 -> this.circlePercent = 1f
            else -> this.circlePercent = percent
        }
        this.maxAngle = circlePercent * 360
        this.maxNum = maxNum
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
        valueAnimator?.duration = duration.toLong()
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
        /** 数量字体大小 */
        private val NUM_TEXT_SIZE = 40f

        /** 单位字体大小 */
        private val UNIT_TEXT_SIZE = 16f

        /** 默认的动效周期 2s */
        private val ANIM_DURATION = 2000

        /** 圆的画笔宽度 */
        private val CIRCLE_PAINT_WIDTH = 8f

        /** 默认起始角度，-90表示12点钟方向 */
        private val DEFAULT_START_ANGLE = -90f
    }
}

/** 渐变类型 */
enum class GradientType {
    LinearType, SweepType
}