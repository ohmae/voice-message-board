/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt

/**
 * グリッド模様付き背景を描画するDrawable
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class GridDrawable(context: Context) : Drawable() {
    private val paint: Paint = Paint()
    private var gridSize: Float = 0f
    private var gridColor: Int = 0
    @ColorInt
    private var backgroundColor: Int = 0

    init {
        gridSize = GRID_SIZE * context.resources.displayMetrics.density
    }

    /**
     * 描画色を設定する
     *
     * @param background 背景
     * @param grid       グリッド
     */
    fun setColor(
            background: Int,
            grid: Int) {
        backgroundColor = background
        gridColor = grid
        paint.color = gridColor
    }

    /**
     * 描画色を設定する。
     *
     * グリッド色は背景色から設定する。
     *
     * @param background 背景
     */
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
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
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

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    companion object {
        private val GRID_SIZE = 10
    }
}
