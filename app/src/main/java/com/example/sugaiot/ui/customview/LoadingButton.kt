package com.example.sugaiot.ui.customview

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
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



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        buttonRect = RectF(left.toFloat(), top.toFloat(), mWidth.toFloat(), mHeight.toFloat())
        val circlePaintOneAlphaAnimator: ValueAnimator = ValueAnimator.ofArgb(
                ContextCompat.getColor(context, R.color.white), ContextCompat.getColor(context, android.R.color.transparent)).apply {
            duration = 400
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
    }
}