/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
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

class RecognizeSpeechContract : ActivityResultContract<String, List<String>>() {
    override fun createIntent(context: Context, input: String): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
            it.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            it.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            it.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            it.putExtra(RecognizerIntent.EXTRA_PROMPT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): List<String> =
        if (resultCode != Activity.RESULT_OK) {
            emptyList()
        } else {
            intent?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: emptyList()
        }
}
