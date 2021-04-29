package com.example.sugaiot.ui.customview

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PathEffect
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

    private val loadingCirclePaint : Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 3f * context.resources.displayMetrics.density
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
                drawLoadingCircle(it)
            }
        }
        super.onDraw(canvas)
    }

    private fun drawLoadingCircle(canvas: Canvas) {

        // TODO, animate the startAngle and sweepAngle in the onSizeChanged.
        canvas.drawArc(
            mWidth * 0.7f,
            mHeight * 0.25f,
            mWidth * 0.9f,
            mHeight * 0.75f,
            360f,
            180f,
            true,
            loadingCirclePaint
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        buttonRect = RectF(left.toFloat(), top.toFloat(), mWidth.toFloat(), mHeight.toFloat())

    }
}