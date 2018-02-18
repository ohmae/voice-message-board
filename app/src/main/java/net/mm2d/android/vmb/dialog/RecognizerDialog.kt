/*
 * Copyright (c) 2017 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.TextView
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.util.Toaster
import net.mm2d.android.vmb.view.BeatingView
import net.mm2d.android.vmb.view.WaveView
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class RecognizerDialog : BaseDialogFragment() {
    private var recognizer: SpeechRecognizer? = null
    private lateinit var textView: TextView
    private lateinit var beatingView: BeatingView
    private lateinit var waveView: WaveView

    interface RecognizeListener {
        fun onRecognize(results: ArrayList<String>)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val act = activity!!
        startListening()
        val inflater = act.layoutInflater
        val decorView = act.window.decorView as ViewGroup
        val view = inflater.inflate(R.layout.dialog_recognizer, decorView, false)
        beatingView = view.findViewById(R.id.beating_view)
        beatingView.setOnClickListener { recognizer?.stopListening() }
        waveView = view.findViewById(R.id.wave_view)
        textView = view.findViewById(R.id.text)
        return AlertDialog.Builder(act)
                .setView(view)
                .create()
    }

    private fun startListening() {
        val applicationContext = context?.applicationContext ?: return
        try {
            recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)?.apply {
                setRecognitionListener(createRecognitionListener())
                startListening(createRecognizerIntent())
            }
        } catch (_: RuntimeException) {
            recognizer = null
            Toaster.show(context, R.string.toast_fail_to_start_voice_input)
            dismiss()
        }
    }

    private fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context?.packageName)
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
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

            override fun onRmsChanged(rms: Float) {
                val volume = normalize(rms)
                beatingView.onVolumeChanged(volume)
                waveView.onVolumeChanged(volume)
            }

            override fun onError(error: Int) {
                Toaster.show(context, R.string.toast_voice_input_fail)
                dismissAllowingStateLoss()
            }

            override fun onPartialResults(results: Bundle?) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?: return
                if (list.size > 0 && !list[0].isEmpty()) {
                    textView.text = list[0]
                }
            }

            override fun onResults(results: Bundle?) {
                dismissAllowingStateLoss()
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?: return
                (activity as? RecognizeListener)?.onRecognize(list)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        try {
            recognizer?.destroy()
        } catch (_: RuntimeException) {
        }
        recognizer = null
    }

    companion object {
        private const val RMS_DB_MAX = 10.0f
        private const val RMS_DB_MIN = -2.12f
        fun normalize(rms: Float): Float = Math.min(Math.max((rms - RMS_DB_MIN) / (RMS_DB_MAX - RMS_DB_MIN), 0f), 1f)
        fun newInstance(): RecognizerDialog = RecognizerDialog()
    }
}
