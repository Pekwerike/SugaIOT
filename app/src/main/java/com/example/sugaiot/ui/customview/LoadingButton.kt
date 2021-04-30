package com.example.sugaiot.ui.customview

import android.animation.Animator
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
    private var loading: Boolean = true
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private lateinit var buttonRect: RectF
    private var startAngle: Float = 360f
    private var sweepAngle: Float = 30f
    private var rotationAngle: Float = 0f


    private val loadingCirclePaint: Paint = Paint().apply {
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
        canvas.save()
        canvas.rotate(rotationAngle)
        // TODO, animate the startAngle and sweepAngle in the onSizeChanged.
        canvas.drawArc(
            mWidth * 0.7f,
            mHeight * 0.25f,
            mWidth * 0.9f,
            mHeight * 0.75f,
            startAngle,
            sweepAngle,
            true,
            loadingCirclePaint
        )
        canvas.restore()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        buttonRect = RectF(left.toFloat(), top.toFloat(), mWidth.toFloat(), mHeight.toFloat())

        // animate the start angle
        val startAngleAnimator = ValueAnimator.ofFloat(startAngle, 180f).apply {
            duration = 400
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            start()
        }
        startAngleAnimator.addUpdateListener {
            startAngle = it.animatedValue as Float
        //    invalidate()
        }

        val sweepAngleAnimator = ValueAnimator.ofFloat(sweepAngle, 300f).apply {
            duration = 500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            start()
        }
        sweepAngleAnimator.addUpdateListener {
            sweepAngle = it.animatedValue as Float
            invalidate()
        }

        val rotationAngleAnimator = ValueAnimator.ofFloat(rotationAngle, 360f).apply {
            duration = 350
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            start()
        }
        rotationAngleAnimator.addUpdateListener {
            rotationAngle = it.animatedValue as Float
          //  invalidate()
        }

    }
}