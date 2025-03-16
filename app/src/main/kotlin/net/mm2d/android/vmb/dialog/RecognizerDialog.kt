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
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.databinding.DialogRecognizerBinding
import net.mm2d.android.vmb.util.Toaster
import net.mm2d.android.vmb.util.isInActive

class RecognizerDialog : DialogFragment() {
    private var recognizer: SpeechRecognizer? = null
    private lateinit var binding: DialogRecognizerBinding

    override fun onCreateDialog(
        savedInstanceState: Bundle?,
    ): Dialog {
        val activity = requireActivity()
        startListening()
        val inflater = activity.layoutInflater
        val decorView = activity.window.decorView as ViewGroup
        binding = DialogRecognizerBinding.inflate(inflater, decorView, false)
        binding.beatingView.setOnClickListener { recognizer?.stopListening() }
        return AlertDialog.Builder(activity)
            .setView(binding.root)
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
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            it.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            it.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            it.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context?.packageName)
        }

    private fun createRecognitionListener(): RecognitionListener =
        object : RecognitionListener {
            override fun onReadyForSpeech(
                params: Bundle?,
            ) = Unit

            override fun onBeginningOfSpeech() = Unit
            override fun onBufferReceived(
                buffer: ByteArray?,
            ) = Unit

            override fun onEndOfSpeech() = Unit
            override fun onEvent(
                eventType: Int,
                params: Bundle?,
            ) = Unit

            override fun onRmsChanged(
                rms: Float,
            ) {
                val volume = normalize(rms)
                binding.beatingView.onVolumeChanged(volume)
                binding.waveView.onVolumeChanged(volume)
            }

            override fun onError(
                error: Int,
            ) {
                Toaster.show(context, R.string.toast_voice_input_fail)
                dismissAllowingStateLoss()
            }

            override fun onPartialResults(
                results: Bundle?,
            ) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
                if (list.isNotEmpty() && list[0].isNotEmpty()) {
                    binding.text.text = list[0]
                }
            }

            override fun onResults(
                results: Bundle?,
            ) {
                dismissAllowingStateLoss()
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
                val requestKey = requireArguments().getString(KEY_REQUEST, "")
                parentFragmentManager.setFragmentResult(requestKey, bundleOf(KEY_RESULT to list))
            }
        }

    override fun onDismiss(
        dialog: DialogInterface,
    ) {
        super.onDismiss(dialog)
        runCatching { recognizer?.destroy() }
        recognizer = null
    }

    companion object {
        private const val TAG = "RecognizerDialog"
        private const val KEY_REQUEST = "KEY_REQUEST"
        private const val KEY_RESULT = "KEY_RESULT"

        private const val RMS_DB_MAX = 10.0f
        private const val RMS_DB_MIN = -2.12f

        fun normalize(
            rms: Float,
        ): Float = ((rms - RMS_DB_MIN) / (RMS_DB_MAX - RMS_DB_MIN)).coerceIn(0f, 1f)

        fun registerListener(
            activity: FragmentActivity,
            requestKey: String,
            listener: (List<String>) -> Unit,
        ) {
            val manager = activity.supportFragmentManager
            manager.setFragmentResultListener(requestKey, activity) { _, result ->
                result.getStringArrayList(KEY_RESULT)?.let(listener)
            }
        }

        fun show(
            activity: FragmentActivity,
            requestKey: String,
        ) {
            if (activity.isInActive()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            RecognizerDialog().also {
                it.arguments = bundleOf(
                    KEY_REQUEST to requestKey,
                )
            }.show(manager, TAG)
        }
    }
}
