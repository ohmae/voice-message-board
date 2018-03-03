/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.Dimension
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.FrameLayout
import net.mm2d.android.vmb.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class BeatingView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    @Dimension
    private val radiusMin: Float
    @Dimension
    private val radiusMax: Float
    private val paint = Paint()
    @Dimension
    private var radius = 0f
    @Dimension
    private var startRadius = 0f
    @Dimension
    private var targetRadius = 0f
    private var radiusAnimator: Animator? = null

    init {
        paint.color = ContextCompat.getColor(context, R.color.beating)
        paint.isAntiAlias = true
        val resources = context.resources
        radiusMin = resources.getDimension(R.dimen.recognizer_icon_circle_size) / 2f
        radiusMax = resources.getDimension(R.dimen.recognizer_icon_area_size) / 2f
        radius = radiusMin
    }

    fun onVolumeChanged(volume: Float) {
        val target = radiusMin + (radiusMax - radiusMin) * volume
        if (target == targetRadius) {
            return
        }
        if (radiusAnimator?.isRunning == true) {
            radiusAnimator?.cancel()
            radius = targetRadius
        }
        startRadius = radius
        targetRadius = target
        radiusAnimator = ValueAnimator.ofFloat(startRadius, targetRadius).apply {
            duration = 150L
            setInterpolator { Math.pow(it.toDouble(), 0.33).toFloat() }
            addUpdateListener {
                radius = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    radiusAnimator = null
                }
            })
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (radiusAnimator?.isRunning == true) {
            radiusAnimator?.cancel()
        }
        radiusAnimator = null
    }

    override fun dispatchDraw(canvas: Canvas) {
        val cx = canvas.width / 2f
        val cy = canvas.height / 2f
        canvas.drawCircle(cx, cy, radius, paint)
        super.dispatchDraw(canvas)
    }
}
