/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.util

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun <I, O> Fragment.registerForActivityResultWrapper(
    contract: ActivityResultContract<I, O>,
    input: I,
    callback: ActivityResultCallback<O>,
): ActivityResultLauncherWrapper<I> =
    ActivityResultLauncherWrapper(registerForActivityResult(contract, callback), input)

fun <I, O> FragmentActivity.registerForActivityResultWrapper(
    contract: ActivityResultContract<I, O>,
    input: I,
    callback: ActivityResultCallback<O>,
): ActivityResultLauncherWrapper<I> =
    ActivityResultLauncherWrapper(registerForActivityResult(contract, callback), input)

class ActivityResultLauncherWrapper<I>(
    private val launcher: ActivityResultLauncher<I>,
    private val input: I,
) {
    fun launch() = launcher.launch(input)
}
