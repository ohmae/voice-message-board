/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.util

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle.State

fun ComponentActivity.isActive(): Boolean =
    !isFinishing && lifecycle.currentState.isAtLeast(State.STARTED)

fun ComponentActivity.isInActive(): Boolean = !isActive()
