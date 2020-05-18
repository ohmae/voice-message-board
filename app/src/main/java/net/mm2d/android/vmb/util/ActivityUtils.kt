package net.mm2d.android.vmb.util

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle.State

fun ComponentActivity.isActive(): Boolean =
    !isFinishing && lifecycle.currentState.isAtLeast(State.STARTED)

fun ComponentActivity.isInActive(): Boolean = !isActive()
