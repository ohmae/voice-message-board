/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt

/**
 * グリッド模様付き背景を描画するDrawable
 */
class GridDrawable(context: Context) : Drawable() {
    private val paint: Paint = Paint()
    private var gridSize: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        GRID_SIZE,
        context.resources.displayMetrics,
    )
    private var gridColor: Int = 0

    @ColorInt
    private var backgroundColor: Int = 0

    fun setColor(background: Int) {
        val hsv = FloatArray(3)
        Color.RGBToHSV(Color.red(background), Color.green(background), Color.blue(background), hsv)
        if (hsv[2] > 0.5f) {
            hsv[2] -= 0.15f
        } else {
            hsv[2] += 0.15f
        }
        backgroundColor = background
        gridColor = Color.HSVToColor(hsv)
        paint.color = gridColor
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        canvas.drawColor(backgroundColor)
        var x = gridSize - 1
        while (x < width) {
            canvas.drawLine(x, 0f, x, height, paint)
            x += gridSize
        }
        var y = gridSize - 1
        while (y < height) {
            canvas.drawLine(0f, y, width, y, paint)
            y += gridSize
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int = PixelFormat.OPAQUE

    companion object {
        private const val GRID_SIZE = 10f
    }
}
