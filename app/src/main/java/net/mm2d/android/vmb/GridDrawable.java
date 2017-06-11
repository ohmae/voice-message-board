/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * グリッド模様付き背景を描画するDrawable
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class GridDrawable extends Drawable {
    private static final int GRID_SIZE = 10;
    private final Paint mPaint;
    private int mGridSize;
    private int mGridColor;
    private int mBackgroundColor;

    /**
     * インスタンス作成
     *
     * @param context コンテキスト
     */
    public GridDrawable(Context context) {
        mPaint = new Paint();
        final float density = context.getResources().getDisplayMetrics().density;
        mGridSize = (int) (GRID_SIZE * density + 0.5f);
    }

    /**
     * グリッドの大きさを指定
     *
     * @param size くりっどの大きさ(単位:ピクセル)
     */
    public void setGridSize(int size) {
        mGridSize = size;
    }

    /**
     * 描画色を設定する
     *
     * @param background 背景
     * @param grid       グリッド
     */
    public void setColor(int background, int grid) {
        mBackgroundColor = background;
        mGridColor = grid;
        mPaint.setColor(mGridColor);
    }

    /**
     * 描画色を設定する。
     *
     * グリッド色は背景色から設定する。
     *
     * @param background 背景
     */
    public void setColor(int background) {
        final float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(background), Color.green(background), Color.blue(background), hsv);
        if (hsv[2] > 0.5f) {
            hsv[2] -= 0.15f;
        } else {
            hsv[2] += 0.15f;
        }
        mBackgroundColor = background;
        mGridColor = Color.HSVToColor(hsv);
        mPaint.setColor(mGridColor);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        canvas.drawColor(mBackgroundColor);
        for (int x = mGridSize - 1; x < width; x += mGridSize) {
            canvas.drawLine(x, 0, x, height, mPaint);
        }
        for (int y = mGridSize - 1; y < height; y += mGridSize) {
            canvas.drawLine(0, y, width, y, mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
