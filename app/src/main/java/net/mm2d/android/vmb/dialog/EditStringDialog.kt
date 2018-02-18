/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import net.mm2d.android.vmb.R

/**
 * 文字列編集を行うダイアログ。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class EditStringDialog : BaseDialogFragment() {

    private lateinit var editText: EditText
    private var eventListener: ConfirmStringListener? = null

    /**
     * 文字列を確定した時に呼ばれるリスナー
     *
     * 呼び出し元のActivityに実装して利用する。
     */
    interface ConfirmStringListener {
        /**
         * 文字列が確定された。
         *
         * @param string 確定された文字列。
         */
        fun onConfirmString(string: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConfirmStringListener) {
            eventListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val act = activity!!
        val string = arguments?.getString(KEY_STRING) ?: return act.let {
            dismiss()
            AlertDialog.Builder(it).create()
        }
        val inflater = act.layoutInflater
        val decorView = act.window.decorView as ViewGroup
        val view = inflater.inflate(R.layout.dialog_edit, decorView, false)
        editText = view.findViewById<EditText>(R.id.editText).apply {
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
        return AlertDialog.Builder(act)
                .setTitle(act.getString(R.string.dialog_title_edit))
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

        /**
         * 選択文字列のkey
         */
        private const val KEY_STRING = "KEY_STRING"

        /**
         * Dialogのインスタンスを作成。
         *
         * 表示する情報を引数で渡すため
         * コンストラクタではなく、
         * このstaticメソッドを利用する。
         *
         * @param editString 編集する元の文字列
         * @return 新規インスタンス
         */
        private fun newInstance(editString: String): EditStringDialog {
            return EditStringDialog().apply {
                arguments = Bundle().apply {
                    putString(KEY_STRING, editString)
                }
            }
        }

        fun show(activity: FragmentActivity, editString: String) {
            newInstance(editString).showAllowingStateLoss(activity.supportFragmentManager, "")
        }
    }
}
