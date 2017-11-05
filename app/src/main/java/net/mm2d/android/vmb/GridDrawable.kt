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

/**
 * グリッド模様付き背景を描画するDrawable
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
class GridDrawable
/**
 * インスタンス作成
 *
 * @param context コンテキスト
 */
(context: Context) : Drawable() {
    private val paint: Paint = Paint()
    private var gridSize: Int = 0
    private var gridColor: Int = 0
    private var backgroundColor: Int = 0

    init {
        val density = context.resources.displayMetrics.density
        gridSize = (GRID_SIZE * density + 0.5f).toInt()
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
        val width = canvas.width
        val height = canvas.height
        canvas.drawColor(backgroundColor)
        var x = gridSize - 1
        while (x < width) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
            x += gridSize
        }
        var y = gridSize - 1
        while (y < height) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
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
