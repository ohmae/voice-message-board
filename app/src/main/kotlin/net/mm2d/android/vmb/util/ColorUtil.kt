/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.util

import androidx.compose.ui.graphics.Color

fun Color.toHsv(
    outHsv: FloatArray? = null,
): FloatArray {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val hsv = outHsv ?: FloatArray(3)
    hsv[0] = hue(r, g, b, max, min)
    hsv[1] = saturation(max, min)
    hsv[2] = max
    return hsv
}

private fun hue(
    r: Float,
    g: Float,
    b: Float,
    max: Float,
    min: Float,
): Float {
    val range = max - min
    if (range == 0f) return 0f
    var hue = when (max) {
        r -> (g - b) / range
        g -> (b - r) / range + 2f
        else -> (r - g) / range + 4f
    }
    if (hue < 0f) hue += 6f
    return hue * 60f
}

private fun saturation(
    max: Float,
    min: Float,
): Float = if (max != 0f) (max - min) / max else 0f
