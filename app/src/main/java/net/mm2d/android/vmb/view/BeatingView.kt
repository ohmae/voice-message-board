/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import net.mm2d.android.vmb.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class BeatingView
@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    @Dimension
    private val radiusMin: Float
    @Dimension
    private val radiusMax: Float
    private val paint = Paint()
    private val radiusAnimators: Array<ValueAnimator> = arrayOf(
            ValueAnimator.ofFloat(0f),
            ValueAnimator.ofFloat(0f),
            ValueAnimator.ofFloat(0f)
    )
    private val radius: Array<Float> = arrayOf(0f, 0f, 0f)

    init {
        paint.color = ContextCompat.getColor(context, R.color.beating)
        paint.isAntiAlias = true
        val resources = context.resources
        radiusMin = resources.getDimension(R.dimen.recognizer_icon_circle_size) / 2f
        radiusMax = resources.getDimension(R.dimen.recognizer_icon_area_size) / 2f
    }

    fun onVolumeChanged(volume: Float) {
        for (i in 0 until 3) {
            startAnimation(i, volume)
        }
    }

    private fun startAnimation(index: Int, volume: Float) {
        if (radius[index] < volume) {
            radius[index] = volume
        }
        if (radiusAnimators[index].isRunning) {
            return
        }
        val target = (radiusMax - radiusMin) * radius[index] / 6f * (1 + index)
        radius[index] = 0f
        val animator = ValueAnimator.ofFloat(0f, target).apply {
            duration = 20 + 200L * (index + 1)
            setInterpolator { 1f - (it * 2f - 1f).square() }
            addUpdateListener { invalidate() }
            start()
        }
        radiusAnimators[index] = animator
    }

    private fun Float.square(): Float = this * this

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        radiusAnimators.forEach {
            if (it.isRunning) {
                it.cancel()
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        val cx = canvas.width / 2f
        val cy = canvas.height / 2f
        var radius = radiusMin
        for (a in radiusAnimators) {
            radius += a.animatedValue as Float
            canvas.drawCircle(cx, cy, radius, paint)
        }
        super.dispatchDraw(canvas)
    }
}
