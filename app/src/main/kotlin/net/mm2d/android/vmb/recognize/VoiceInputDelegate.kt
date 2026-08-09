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
import net.mm2d.android.vmb.MainActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.permission.RecordAudioPermission
import net.mm2d.android.vmb.util.Toaster
import net.mm2d.android.vmb.util.registerForActivityResultWrapper

class VoiceInputDelegate(
    private val activity: FragmentActivity,
    private val onShowRecognizer: () -> Unit,
    private val setText: (text: String) -> Unit,
) {
    private var shouldUseSpeechRecognizer: Boolean = false
    private var shouldShowCandidateList: Boolean = false

    private val permissionLauncher =
        activity.registerForActivityResultWrapper(
            RequestPermission(),
            Manifest.permission.RECORD_AUDIO,
            ::onPermissionResult,
        )
    private val speechLauncher =
        activity.registerForActivityResultWrapper(
            RecognizeSpeechContract(),
            activity.getString(R.string.recognizer_title),
            ::onRecognize,
        )

    fun updateSettings(
        shouldUseSpeechRecognizer: Boolean,
        shouldShowCandidateList: Boolean,
    ) {
        this.shouldUseSpeechRecognizer = shouldUseSpeechRecognizer
        this.shouldShowCandidateList = shouldShowCandidateList
    }

    fun start() {
        if (shouldUseSpeechRecognizer) {
            startDialogWithPermission()
        } else {
            speechLauncher.launch()
        }
    }

    private fun startDialogWithPermission() {
        if (RecordAudioPermission.hasPermission(activity)) {
            onShowRecognizer()
        } else {
            permissionLauncher.launch()
        }
    }

    fun onRecognize(
        results: List<String>,
    ) {
        if (results.isEmpty()) {
            return
        }
        if (results.size > 1 && shouldShowCandidateList) {
            SelectStringDialog.show(
                activity,
                MainActivity.REQUEST_SELECT,
                R.string.dialog_title_select,
                ArrayList(results),
            )
        } else {
            setText.invoke(results[0])
        }
    }

    private fun onPermissionResult(
        granted: Boolean,
    ) {
        when {
            granted -> {
                onShowRecognizer()
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
