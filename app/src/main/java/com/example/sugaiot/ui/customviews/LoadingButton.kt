package com.example.sugaiot.ui.customviews

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnRepeat
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import com.example.sugaiot.R
import com.google.android.material.button.MaterialButton

class LoadingButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
    private var loading = true
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private lateinit var buttonRect: RectF

    private val buttonBackgroundPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.purple_500)
        isAntiAlias = true
        isDither = true
    }
    private val circlePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.white)
        isAntiAlias = true
        isDither = true
    }
    private val circlePaintTwo: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.white)
        isAntiAlias = true
        isDither = true
    }
    private val circlePaintThree: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.white)
        isAntiAlias = true
        isDither = true
    }

    fun startLoading() {
        loading = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        if (loading) {

            canvas?.let {
                drawAnimatedThreeCircles(canvas)
            }
        } else {
            super.onDraw(canvas)
        }
    }

    private fun drawAnimatedThreeCircles(canvas: Canvas) {
        canvas.drawRoundRect(buttonRect, width * 0.01f, height * 0.01f, buttonBackgroundPaint)
        canvas.drawCircle(width * 0.5f, height * 0.5f, width * 0.02f,
                circlePaintTwo)

        canvas.drawCircle(width * 0.4f, height * 0.5f, width * 0.02f, circlePaint)
        canvas.drawCircle(width * 0.6f, height * 0.5f, width * 0.02f, circlePaintThree)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = measuredWidth
        mHeight = measuredHeight
        buttonRect = RectF(left.toFloat(), top.toFloat(), mWidth.toFloat(), mHeight.toFloat())
        val circlePaintOneAlphaAnimator: ValueAnimator = ValueAnimator.ofArgb(
                ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, android.R.color.transparent)).apply {
            duration = 400
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

        circlePaintOneAlphaAnimator.addUpdateListener {
            circlePaint.color = it.animatedValue as Int
            if (circlePaint.color == ContextCompat.getColor(context, R.color.white)) {
                circlePaintOneAlphaAnimator.pause()
                Handler(Looper.getMainLooper()).postDelayed({
                    circlePaintOneAlphaAnimator.resume()
                }, 800)
            }
            invalidate()
        }

        val circlePaintTwoAlphaAnimator: ValueAnimator = ValueAnimator.ofArgb(ContextCompat.getColor(context, R.color.white),
                ContextCompat.getColor(context, android.R.color.transparent)).apply {
            duration = 400
            startDelay = 400
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART

        }
        circlePaintTwoAlphaAnimator.addUpdateListener {
            circlePaintTwo.color = it.animatedValue as Int
            if (circlePaintTwo.color == ContextCompat.getColor(context, R.color.white)) {
                circlePaintTwoAlphaAnimator.pause()
                Handler(Looper.getMainLooper()).postDelayed({
                    circlePaintTwoAlphaAnimator.resume()
                }, 800)
            }
            invalidate()
        }
        val circlePaintThreeAlphaAnimator: ValueAnimator = ValueAnimator.ofArgb(ContextCompat.getColor(context, R.color.white),
                ContextCompat.getColor(context, android.R.color.transparent)).apply {
            duration = 400
            startDelay = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

        circlePaintThreeAlphaAnimator.addUpdateListener {
            circlePaintThree.color = it.animatedValue as Int
            if (circlePaintThree.color == ContextCompat.getColor(context, R.color.white)) {
                circlePaintThreeAlphaAnimator.pause()
                Handler(Looper.getMainLooper()).postDelayed({
                    circlePaintThreeAlphaAnimator.resume()
                }, 800)
            }
            invalidate()
        }

        AnimatorSet().apply {
            playTogether(circlePaintOneAlphaAnimator, circlePaintTwoAlphaAnimator, circlePaintThreeAlphaAnimator)
            start()
        }
    }
}