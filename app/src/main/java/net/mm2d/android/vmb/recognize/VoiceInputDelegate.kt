/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.recognize

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.permission.RecordAudioPermission
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.util.Toaster

class VoiceInputDelegate(
    private val activity: FragmentActivity,
    private val setText: (text: String) -> Unit
) {
    private val settings = Settings.get()
    private val permissionLauncher =
        activity.registerForActivityResult(RecordAudioPermission.RequestContract(), ::onPermissionResult)
    private val speechLauncher =
        activity.registerForActivityResult(RecognizeSpeechContract(), ::onRecognize)

    fun start() {
        if (settings.shouldUseSpeechRecognizer()) {
            startDialogWithPermission()
        } else {
            speechLauncher.launch(Unit)
        }
    }

    private fun startDialogWithPermission() {
        if (RecordAudioPermission.hasPermission(activity)) {
            RecognizerDialog.show(activity)
        } else {
            permissionLauncher.launch(Unit)
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

    private class RecognizeSpeechContract : ActivityResultContract<Unit, List<String>>() {
        override fun createIntent(context: Context, input: Unit?): Intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
                it.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                it.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                it.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                val packageName = context.packageName
                it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                val title = context.getString(R.string.recognizer_title)
                it.putExtra(RecognizerIntent.EXTRA_PROMPT, title)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): List<String> =
            if (resultCode != Activity.RESULT_OK) {
                emptyList()
            } else {
                intent?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: emptyList()
            }
    }
}
