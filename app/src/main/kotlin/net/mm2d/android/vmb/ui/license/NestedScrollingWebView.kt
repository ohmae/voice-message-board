/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.ui.license

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import kotlin.math.abs

class NestedScrollingWebView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr),
    NestedScrollingChild {
    private val helper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private val buffer = IntArray(2)
    private var startY: Float = 0f
    private var prevY: Float = 0f
    private var scrolling: Boolean = false

    init {
        isNestedScrollingEnabled = true
        setBackgroundColor(0)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(
        event: MotionEvent,
    ): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.rawY
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }

            MotionEvent.ACTION_MOVE -> onTouchMove(event)

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
                -> onTouchEnd()
        }
        prevY = event.rawY
        return super.onTouchEvent(event)
    }

    private fun onTouchMove(
        event: MotionEvent,
    ) {
        if (event.pointerCount != 1) {
            return
        }
        if (scrolling) {
            val dy = (prevY - event.rawY).toInt()
            val consumed: IntArray = buffer
            dispatchNestedPreScroll(0, dy, consumed, null)
            val unconsumedDy = dy - consumed[1]
            dispatchNestedScroll(0, consumed[1], 0, unconsumedDy, null)
            return
        }
        if (abs(startY - event.rawY) > touchSlop) {
            scrolling = true
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
        }
    }

    private fun onTouchEnd() {
        if (scrolling) {
            stopNestedScroll()
            scrolling = false
        }
    }

    override fun setNestedScrollingEnabled(
        enabled: Boolean,
    ) {
        helper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean = helper.isNestedScrollingEnabled

    override fun startNestedScroll(
        axes: Int,
    ): Boolean = helper.startNestedScroll(axes)

    override fun stopNestedScroll() {
        helper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean = helper.hasNestedScrollingParent()

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
    ): Boolean =
        helper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
        )

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
    ): Boolean = helper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean,
    ): Boolean = helper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun dispatchNestedPreFling(
        velocityX: Float,
        velocityY: Float,
    ): Boolean = helper.dispatchNestedPreFling(velocityX, velocityY)
}
