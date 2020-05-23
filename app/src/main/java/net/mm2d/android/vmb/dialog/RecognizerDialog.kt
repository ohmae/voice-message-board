/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
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
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_recognizer.view.*
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.util.Toaster
import net.mm2d.android.vmb.util.isInActive
import net.mm2d.android.vmb.view.BeatingView
import net.mm2d.android.vmb.view.WaveView
import java.util.*

class RecognizerDialog : DialogFragment() {
    private var recognizer: SpeechRecognizer? = null
    private lateinit var textView: TextView
    private lateinit var beatingView: BeatingView
    private lateinit var waveView: WaveView

    interface RecognizeListener {
        fun onRecognize(results: ArrayList<String>)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        startListening()
        val inflater = activity.layoutInflater
        val decorView = activity.window.decorView as ViewGroup
        val view = inflater.inflate(R.layout.dialog_recognizer, decorView, false)
        beatingView = view.beating_view
        beatingView.setOnClickListener { recognizer?.stopListening() }
        waveView = view.wave_view
        textView = view.text
        return AlertDialog.Builder(activity)
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

    private fun createRecognizerIntent(): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
            it.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            it.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            it.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context?.packageName)
        }

    private fun createRecognitionListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

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
            val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
            if (list.isNotEmpty() && list[0].isNotEmpty()) {
                textView.text = list[0]
            }
        }

        override fun onResults(results: Bundle?) {
            dismissAllowingStateLoss()
            val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
            (activity as? RecognizeListener)?.onRecognize(list)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        runCatching { recognizer?.destroy() }
        recognizer = null
    }

    companion object {
        private const val TAG = "RecognizerDialog"
        private const val RMS_DB_MAX = 10.0f
        private const val RMS_DB_MIN = -2.12f
        fun normalize(rms: Float): Float =
            ((rms - RMS_DB_MIN) / (RMS_DB_MAX - RMS_DB_MIN)).coerceIn(0f, 1f)

        fun show(activity: FragmentActivity) {
            if (activity.isInActive()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            RecognizerDialog().show(manager, TAG)
        }
    }
}
