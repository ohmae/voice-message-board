/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object RecordAudioPermission {
    private const val PERMISSION = Manifest.permission.RECORD_AUDIO

    fun deniedWithoutShowDialog(
        activity: Activity,
    ): Boolean = !ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION)

    fun hasPermission(
        context: Context,
    ) = ContextCompat.checkSelfPermission(context, PERMISSION) ==
        PackageManager.PERMISSION_GRANTED
}
