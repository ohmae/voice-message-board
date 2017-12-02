/*
 * Copyright (c) 2017 大前良介(OHMAE Ryosuke)
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
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import net.mm2d.android.vmb.R
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class WaveView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    @ColorInt
    private val wave1Color: Int
    @ColorInt
    private val wave2Color: Int
    private val paint = Paint()
    private val path = Path()
    private val queue = LinkedList<Float>()
    private val wave1CenterFromBottom: Float
    private val wave2CenterFromBottom: Float
    private val wave1Scale: Float
    private val wave2Scale: Float

    private var waveAnimator: Animator? = null
    private var offset: Float = 0f
    private var sign = 1
    private var amplitude: Float = 0f

    init {
        paint.isAntiAlias = true
        wave1Color = ContextCompat.getColor(context, R.color.wave1)
        wave2Color = ContextCompat.getColor(context, R.color.wave2)
        val resources = context.resources
        wave1CenterFromBottom = resources.getDimension(R.dimen.wave1_center)
        wave2CenterFromBottom = resources.getDimension(R.dimen.wave2_center)
        val density: Float = context.resources.displayMetrics.density
        wave1Scale = density * 13f
        wave2Scale = density * 17f
    }

    fun onVolumeChanged(volume: Float) {
        if (volume > amplitude) {
            amplitude = volume
        }
    }

    private fun drawWave(canvas: Canvas, cy: Float, offset: Float, scale: Float) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val waveLength = width / (DIVISION - 3)
        val handleLength = waveLength / 3
        var xp = -offset * waveLength
        var yp = queue[0] * scale + cy
        path.reset()
        path.moveTo(xp, yp)
        for (i in 1 until DIVISION) {
            val xn = xp + waveLength
            val x1 = xp + handleLength
            val x2 = xn - handleLength
            val yn = queue[i] * scale + cy
            path.cubicTo(x1, yp, x2, yn, xn, yn)
            if (xn > width) {
                break
            }
            xp = xn
            yp = yn
        }
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()
        canvas.drawPath(path, paint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val height = canvas.height
        paint.color = wave1Color
        drawWave(canvas, height - wave1CenterFromBottom, offset, wave1Scale)
        paint.color = wave2Color
        drawWave(canvas, height - wave2CenterFromBottom, offset + 0.5f, wave2Scale)
        super.dispatchDraw(canvas)
    }

    private fun startAnimation() {
        stopAnimation()
        queue.clear()
        for (i in 0 until DIVISION) {
            queue.addLast(0f)
        }
        sign = 1
        offset = 0f
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 200L
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            if (value < offset) {
                sign *= -1
                queue.removeFirst()
                queue.addLast(amplitude * sign)
                amplitude = 0f
            }
            offset = value
            invalidate()
        }
        animator.start()
        waveAnimator = animator
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
        private const val DIVISION = 5
    }
}
