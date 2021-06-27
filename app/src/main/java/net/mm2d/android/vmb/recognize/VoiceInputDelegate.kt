/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.recognize

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.permission.RecordAudioPermission
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.util.Toaster
import java.util.*

class VoiceInputDelegate(
    private val activity: FragmentActivity,
    private val voiceRequestCode: Int,
    private val setText: (text: String) -> Unit
) {
    private val settings = Settings.get()
    private val launcher =
        activity.registerForActivityResult(RecordAudioPermission.RequestContract(), ::onPermissionResult)

    fun start() {
        if (settings.shouldUseSpeechRecognizer()) {
            startDialogWithPermission()
        } else {
            startActivity()
        }
    }

    private fun startDialogWithPermission() {
        if (RecordAudioPermission.hasPermission(activity)) {
            startDialog()
        } else {
            launcher.launch(Unit)
        }
    }

    private fun startDialog() {
        RecognizerDialog.show(activity)
    }

    private fun startActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
            it.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            it.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            it.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
            it.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                activity.getString(R.string.recognizer_title)
            )
        }
        try {
            activity.startActivityForResult(intent, voiceRequestCode)
        } catch (_: ActivityNotFoundException) {
            Toaster.show(activity, R.string.toast_can_not_use_voice_input)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != voiceRequestCode || resultCode != Activity.RESULT_OK) {
            return
        }
        // 音声入力の結果を反映
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
        onRecognize(results)
    }

    fun onRecognize(results: ArrayList<String>) {
        if (results.isEmpty()) {
            return
        }
        if (results.size > 1 && settings.shouldShowCandidateList()) {
            SelectStringDialog.show(activity, R.string.dialog_title_select, results)
        } else {
            setText.invoke(results[0])
        }
    }

    private fun onPermissionResult(granted: Boolean) {
        when {
            granted -> {
                startDialog()
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
