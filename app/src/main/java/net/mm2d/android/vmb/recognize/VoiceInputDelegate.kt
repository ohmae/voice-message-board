/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.recognize

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.permission.RecordAudioPermission
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.util.Toaster
import net.mm2d.android.vmb.util.registerForActivityResultWrapper

class VoiceInputDelegate(
    private val activity: FragmentActivity,
    private val setText: (text: String) -> Unit
) {
    private val settings = Settings.get()
    private val permissionLauncher =
        activity.registerForActivityResultWrapper(
            RequestPermission(),
            Manifest.permission.RECORD_AUDIO,
            ::onPermissionResult
        )
    private val speechLauncher =
        activity.registerForActivityResultWrapper(
            RecognizeSpeechContract(),
            activity.getString(R.string.recognizer_title),
            ::onRecognize
        )

    fun start() {
        if (settings.shouldUseSpeechRecognizer()) {
            startDialogWithPermission()
        } else {
            speechLauncher.launch()
        }
    }

    private fun startDialogWithPermission() {
        if (RecordAudioPermission.hasPermission(activity)) {
            RecognizerDialog.show(activity)
        } else {
            permissionLauncher.launch()
        }
    }

    fun onRecognize(results: List<String>) {
        if (results.isEmpty()) {
            return
        }
        if (results.size > 1 && settings.shouldShowCandidateList()) {
            SelectStringDialog.show(activity, R.string.dialog_title_select, ArrayList(results))
        } else {
            setText.invoke(results[0])
        }
    }

    private fun onPermissionResult(granted: Boolean) {
        when {
            granted -> {
                RecognizerDialog.show(activity)
            }
            RecordAudioPermission.deniedWithoutShowDialog(activity) -> {
                PermissionDialog.show(activity)
            }
            else -> {
                Toaster.show(activity, R.string.toast_should_allow_microphone_permission)
            }
        }
    }
}
