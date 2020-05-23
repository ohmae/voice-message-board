/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.permission

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class PermissionHelper(
    private val activity: FragmentActivity,
    private val permission: String,
    private val requestCode: Int
) {
    enum class Result {
        OTHER,
        GRANTED,
        DENIED,
        DENIED_ALWAYS
    }

    private fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    fun requestPermissionIfNeed(): Boolean {
        if (isGranted()) return false
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        return true
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Result {
        if (requestCode != requestCode || permissions.isEmpty()) {
            return Result.OTHER
        }
        val index = permissions.indexOf(permission)
        if (index < 0 || index > grantResults.size) {
            return Result.OTHER
        }
        if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
            return Result.GRANTED
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            return Result.DENIED
        }
        return Result.DENIED_ALWAYS
    }
}
