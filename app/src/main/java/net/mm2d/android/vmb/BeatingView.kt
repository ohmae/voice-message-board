/*
 * Copyright (c) 2017 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class BeatingView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private val radiusMin: Float
    private val radiusMax: Float
    private val paint: Paint = Paint()
    private var radius: Float = 0f

    init {
        paint.color = ContextCompat.getColor(context, R.color.beating)
        paint.isAntiAlias = true
        val resources = context.resources
        radiusMin = resources.getDimension(R.dimen.recognizer_icon_circle_size) / 2f
        radiusMax = resources.getDimension(R.dimen.recognizer_icon_area_size) / 2f
        radius = radiusMin
    }

    fun onRmsChanged(rmsdB: Float) {
        radius = radiusMin + (radiusMax - radiusMin) * (rmsdB - RMS_DB_MIN) / (RMS_DB_MAX - RMS_DB_MIN)
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        val cx = canvas.width / 2f
        val cy = canvas.height / 2f
        canvas.drawCircle(cx, cy, radius, paint)
        super.dispatchDraw(canvas)
    }

    companion object {
        private val RMS_DB_MAX = 10.0f
        private val RMS_DB_MIN = -2.12f
    }
}
