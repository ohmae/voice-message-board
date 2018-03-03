/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.recognize

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.support.v4.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.RecognizerDialog
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.permission.PermissionHelper
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.util.Toaster
import java.util.*

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
class VoiceInputDelegate(
        private val activity: FragmentActivity,
        private val voiceRequestCode: Int,
        permissionRequestCode: Int,
        private val setText: (text: String) -> Unit) {
    private val settings = Settings(activity)
    private val permissionHelper = PermissionHelper(activity, Manifest.permission.RECORD_AUDIO, permissionRequestCode)

    fun start() {
        if (settings.shouldUseSpeechRecognizer()) {
            startDialogWithPermission()
        } else {
            startActivity()
        }
    }

    private fun startDialogWithPermission() {
        if (!permissionHelper.requestPermissionIfNeed()) {
            startDialog()
        }
    }

    private fun startDialog() {
        RecognizerDialog.show(activity)
    }

    private fun startActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.recognizer_title))
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

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            PermissionHelper.Result.OTHER -> return
            PermissionHelper.Result.GRANTED ->
                startDialog()
            PermissionHelper.Result.DENIED ->
                Toaster.show(activity, R.string.toast_should_allow_microphone_permission)
            PermissionHelper.Result.DENIED_ALWAYS ->
                PermissionDialog.show(activity, R.string.dialog_microphone_permission_message)
        }
    }
}
