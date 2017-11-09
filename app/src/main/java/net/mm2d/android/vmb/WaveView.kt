/*
 * Copyright (c) 2017 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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

    private var waveAnimator: ValueAnimator? = null
    private var offset: Float = 0f
    private var turn = 1
    private var maxDb: Float = 0f

    init {
        paint.isAntiAlias = true
        wave1Color = ContextCompat.getColor(context, R.color.wave1)
        wave2Color = ContextCompat.getColor(context, R.color.wave2)
        val resources = context.resources
        wave1CenterFromBottom = resources.getDimension(R.dimen.wave1_center)
        wave2CenterFromBottom = resources.getDimension(R.dimen.wave2_center)
        val density: Float = context.resources.displayMetrics.density
        wave1Scale = density * 1.3f
        wave2Scale = density * 1.7f
    }

    fun onRmsChanged(rmsdB: Float) {
        if (rmsdB > maxDb) {
            maxDb = rmsdB
        }
    }

    private fun drawWave(canvas: Canvas, cy: Float, offset: Float, scale: Float) {
        val step = (canvas.width / (DIVISION - 3)).toFloat()
        var x = offset
        var py = queue[0] * turn.toFloat() * scale + cy
        path.reset()
        path.moveTo(x, py)
        for (i in 1 until DIVISION) {
            val x1 = x + step / 3
            val x2 = x + step * 2 / 3
            val x3 = x + step
            val flag = (if (i % 2 == 0) 1 else -1) * turn
            val ny = queue[i] * flag.toFloat() * scale + cy
            path.cubicTo(x1, py, x2, ny, x3, ny)
            x += step
            py = ny
        }
        path.lineTo(canvas.width.toFloat(), canvas.height.toFloat())
        path.lineTo(0f, canvas.height.toFloat())
        path.close()
        canvas.drawPath(path, paint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val width = canvas.width
        val height = canvas.height
        val length = (width / (DIVISION - 3)).toFloat()
        paint.color = wave1Color
        drawWave(canvas, height - wave1CenterFromBottom, -length * offset, wave1Scale)
        paint.color = wave2Color
        drawWave(canvas, height - wave2CenterFromBottom, -length * (offset + 0.5f), wave2Scale)
        super.dispatchDraw(canvas)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        queue.clear()
        for (i in 0 until DIVISION) {
            queue.add(0f)
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 200L
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener { animation ->
            offset = animation.animatedValue as Float
            invalidate()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator) {
                turn *= -1
                queue.removeAt(0)
                queue.add(maxDb)
                maxDb = 0f
            }
        })
        animator.start()
        waveAnimator = animator
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator?.cancel()
        waveAnimator = null
    }

    companion object {
        private const val DIVISION = 5
    }
}
