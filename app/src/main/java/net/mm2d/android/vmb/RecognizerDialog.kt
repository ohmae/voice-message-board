/*
 * Copyright (c) 2017 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Toast
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class RecognizerDialog : DialogFragment() {
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var beatingView: BeatingView

    interface RecognizeListener {
        fun onRecognize(results: ArrayList<String>)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
        recognizer.setRecognitionListener(createRecognitionListener())
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        recognizer.startListening(intent)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_recognizer, null, false);
        beatingView = view.findViewById(R.id.beating_view);
        return AlertDialog.Builder(context)
                .setView(view)
                .create()
    }

    fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
            }

            override fun onRmsChanged(rmsdB: Float) {
                beatingView.onRmsChanged(rmsdB)
            }

            override fun onError(error: Int) {
                Toast.makeText(context, R.string.toast_voice_input_fail, Toast.LENGTH_LONG).show()
                dismiss()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?: Collections.emptyList<String>()
                if (list.sumBy { it.length } > 0) {
                    recognizer.stopListening()
                }
            }

            override fun onResults(results: Bundle?) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (list != null) {
                    (targetFragment as? RecognizeListener)?.onRecognize(list)
                }
                dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        recognizer.destroy()
    }

    fun showAllowingStateLoss(manager: FragmentManager, tag: String) {
        val ft = manager.beginTransaction();
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    companion object {
        fun newInstance(): RecognizerDialog {
            return RecognizerDialog()
        }
    }
}