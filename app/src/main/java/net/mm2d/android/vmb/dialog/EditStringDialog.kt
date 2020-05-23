/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_edit.view.*
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.util.isInActive

class EditStringDialog : DialogFragment() {
    private lateinit var editText: EditText
    private var eventListener: ConfirmStringListener? = null

    interface ConfirmStringListener {
        fun onConfirmString(string: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConfirmStringListener) {
            eventListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val string = arguments?.getString(KEY_STRING) ?: return activity.let {
            dismiss()
            AlertDialog.Builder(it).create()
        }
        val inflater = activity.layoutInflater
        val decorView = activity.window.decorView as ViewGroup
        val view = inflater.inflate(R.layout.dialog_edit, decorView, false)
        editText = view.editText.apply {
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
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ -> inputText() }
            .create()
    }

    private fun inputText() {
        eventListener?.onConfirmString(editText.text.toString())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 編集中の文字列を保存
        arguments?.putString(KEY_STRING, editText.text.toString())
    }

    companion object {
        private const val TAG = "EditStringDialog"
        private const val KEY_STRING = "KEY_STRING"

        fun show(activity: FragmentActivity, editString: String) {
            if (activity.isInActive()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            EditStringDialog().also { dialog ->
                dialog.arguments = Bundle().also {
                    it.putString(KEY_STRING, editString)
                }
            }.show(manager, TAG)
        }
    }
}
