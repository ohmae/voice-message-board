/*
 * Copyright (c) 2025 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */
package net.mm2d.android.vmb.permission

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.PermissionStatus.Granted
import com.google.accompanist.permissions.rememberPermissionState

private const val ENOUGH_DURATION = 300L

@OptIn(ExperimentalPermissionsApi::class)
fun grantedPermissionState(
    permission: String,
): PermissionState =
    object : PermissionState {
        override val permission: String = permission
        override val status: PermissionStatus = Granted
        override fun launchPermissionRequest() = Unit
    }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionStateWithDialogTracking(
    permission: String,
    onPermissionResult: (granted: Boolean, dialogShown: Boolean) -> Unit = { _, _ -> },
): PermissionState {
    if (LocalInspectionMode.current) return grantedPermissionState(permission)
    val activity = LocalActivity.current
        ?: throw IllegalStateException("Permissions should be called in the context of an Activity")

    var shouldShowRationaleBefore = false
    var start: Long = 0

    val permissionState = rememberPermissionState(permission) {
        val elapsedEnoughTime = System.currentTimeMillis() - start > ENOUGH_DURATION
        val shouldShowRationaleAfter = activity.shouldShowRationale(permission)
        val dialogShown = shouldShowRationaleBefore || shouldShowRationaleAfter || elapsedEnoughTime
        onPermissionResult(it, dialogShown)
    }

    val launchPermissionRequest by rememberUpdatedState {
        shouldShowRationaleBefore = activity.shouldShowRationale(permission)
        start = System.currentTimeMillis()
        permissionState.launchPermissionRequest()
    }

    return remember(permissionState) {
        object : PermissionState {
            override val permission: String
                get() = permissionState.permission
            override val status: PermissionStatus
                get() = permissionState.status

            override fun launchPermissionRequest() {
                launchPermissionRequest()
            }
        }
    }
}

private fun Activity.shouldShowRationale(
    permission: String,
): Boolean = ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
