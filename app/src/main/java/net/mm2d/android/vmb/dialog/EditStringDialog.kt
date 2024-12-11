/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.databinding.DialogEditBinding
import net.mm2d.android.vmb.util.isInActive

class EditStringDialog : DialogFragment() {
    private lateinit var binding: DialogEditBinding

    override fun onCreateDialog(
        savedInstanceState: Bundle?,
    ): Dialog {
        val activity = requireActivity()
        val string = requireArguments().getString(KEY_STRING, "")

        val inflater = activity.layoutInflater
        val decorView = activity.window.decorView as ViewGroup
        binding = DialogEditBinding.inflate(inflater, decorView, false)
        binding.editText.apply {
            setText(string)
            setSelection(string.length)
            setOnEditorActionListener { _, actionId, event ->
                val keyCode = event?.keyCode ?: -1
                if (actionId == EditorInfo.IME_ACTION_DONE || keyCode == KeyEvent.KEYCODE_ENTER) {
                    inputText()
                    dismiss()
                    true
                } else {
                    false
                }
            }
        }
        return AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.dialog_title_edit))
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { _, _ -> inputText() }
            .create()
    }

    private fun inputText() {
        val requestKey = requireArguments().getString(KEY_REQUEST, "")
        val result = binding.editText.text.toString()
        parentFragmentManager.setFragmentResult(requestKey, bundleOf(KEY_RESULT to result))
    }

    override fun onSaveInstanceState(
        outState: Bundle,
    ) {
        super.onSaveInstanceState(outState)
        // 編集中の文字列を保存
        arguments?.putString(KEY_STRING, binding.editText.text.toString())
    }

    companion object {
        private const val TAG = "EditStringDialog"
        private const val KEY_STRING = "KEY_STRING"
        private const val KEY_REQUEST = "KEY_REQUEST"
        private const val KEY_RESULT = "KEY_RESULT"

        fun registerListener(
            activity: FragmentActivity,
            requestKey: String,
            listener: (String) -> Unit,
        ) {
            val manager = activity.supportFragmentManager
            manager.setFragmentResultListener(requestKey, activity) { _, result ->
                result.getString(KEY_RESULT)?.let(listener)
            }
        }

        fun show(
            activity: FragmentActivity,
            requestKey: String,
            editString: String,
        ) {
            if (activity.isInActive()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            EditStringDialog().also { dialog ->
                dialog.arguments = bundleOf(
                    KEY_REQUEST to requestKey,
                    KEY_STRING to editString,
                )
            }.show(manager, TAG)
        }
    }
}
