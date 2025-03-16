/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import net.mm2d.android.vmb.R
import java.util.LinkedList

class WaveView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    @ColorInt
    private val wave1Color: Int

    @ColorInt
    private val wave2Color: Int

    @ColorInt
    private val wave3Color: Int
    private val paint = Paint()
    private val path = Path()
    private val queue = LinkedList<Float>()
    private val wave1CenterFromBottom: Float
    private val wave2CenterFromBottom: Float
    private val wave3CenterFromBottom: Float
    private val wave1Scale: Float
    private val wave2Scale: Float
    private val wave3Scale: Float

    private var waveAnimator: Animator? = null
    private var phase: Float = 0f
    private var sign = 1
    private var amplitude: Float = 0f

    init {
        paint.isAntiAlias = true
        wave1Color = ContextCompat.getColor(context, R.color.wave1)
        wave2Color = ContextCompat.getColor(context, R.color.wave2)
        wave3Color = ContextCompat.getColor(context, R.color.wave3)
        val resources = context.resources
        wave1CenterFromBottom = resources.getDimension(R.dimen.wave1_center)
        wave2CenterFromBottom = resources.getDimension(R.dimen.wave2_center)
        wave3CenterFromBottom = resources.getDimension(R.dimen.wave3_center)
        val density: Float = context.resources.displayMetrics.density
        wave1Scale = 20.0f * density
        wave2Scale = 17.5f * density
        wave3Scale = 15.0f * density
    }

    fun onVolumeChanged(
        volume: Float,
    ) {
        if (volume > amplitude) {
            amplitude = volume
        }
    }

    private fun drawWave(
        canvas: Canvas,
        cy: Float,
        offset: Float,
        scale: Float,
    ) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val waveLength = width / (2f + offset * 2f)
        val handleLength = waveLength / 3
        var xp = width + (1.0f - phase + offset) * waveLength
        var yp = queue[0] * scale + cy
        path.reset()
        path.moveTo(xp, yp)
        for (i in 1 until HISTORY_SIZE) {
            val xn = xp - waveLength
            val x1 = xp - handleLength
            val x2 = xn + handleLength
            val yn = queue[i] * scale + cy
            path.cubicTo(x1, yp, x2, yn, xn, yn)
            if (xn < 0f) {
                break
            }
            xp = xn
            yp = yn
        }
        path.lineTo(0f, height)
        path.lineTo(width, height)
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawWaveEffect(
        canvas: Canvas,
    ) {
        val height = canvas.height
        paint.color = wave3Color
        drawWave(canvas, height - wave3CenterFromBottom, 1.0f, wave3Scale)
        paint.color = wave2Color
        drawWave(canvas, height - wave2CenterFromBottom, 0.5f, wave2Scale)
        paint.color = wave1Color
        drawWave(canvas, height - wave1CenterFromBottom, 0.0f, wave1Scale)
    }

    override fun dispatchDraw(
        canvas: Canvas,
    ) {
        drawWaveEffect(canvas)
        super.dispatchDraw(canvas)
    }

    private fun startAnimation() {
        stopAnimation()
        queue.clear()
        for (i in 0 until HISTORY_SIZE) {
            queue.addLast(0f)
        }
        sign = 1
        phase = 0f
        waveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200L
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                if (value < phase) {
                    sign *= -1
                    queue.removeLast()
                    queue.addFirst(amplitude * sign)
                    amplitude = 0f
                }
                phase = value
                invalidate()
            }
            start()
        }
    }

    private fun stopAnimation() {
        waveAnimator?.cancel()
        waveAnimator = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    companion object {
        private const val HISTORY_SIZE = 10
    }
}
