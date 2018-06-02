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
import android.support.annotation.StringRes
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.view.adapter.BaseListAdapter
import java.util.*

/**
 * 音声認識の結果複数の候補が出た時に表示するダイアログ。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SelectStringDialog : BaseDialogFragment() {

    private var eventListener: SelectStringListener? = null

    /**
     * 文字列を選択した時に呼ばれるリスナー
     *
     * 呼び出し元のActivityに実装して利用する。
     */
    interface SelectStringListener {
        /**
         * 文字列が選択された。
         *
         * @param string 選択された文字列。
         */
        fun onSelectString(string: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SelectStringListener) {
            eventListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val act = activity!!
        val arg = arguments!!
        val stringList = arg.getStringArrayList(KEY_STRING_LIST) ?: return act.let {
            dismiss()
            AlertDialog.Builder(it).create()
        }
        return AlertDialog.Builder(act)
                .setTitle(arg.getInt(KEY_TITLE))
                .setAdapter(StringListAdapter(act, stringList)) { _, which ->
                    eventListener?.onSelectString(stringList[which])
                }
                .create()
    }

    class StringListAdapter(context: Context, collection: Collection<String>)
        : BaseListAdapter<String>(context, collection) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = inflateView(R.layout.list_item_string, convertView, parent)
            view.findViewById<TextView>(R.id.textView).text = getItem(position)
            return view
        }
    }

    companion object {
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_STRING_LIST = "KEY_STRING_LIST"

        /**
         * Dialogのインスタンスを作成。
         *
         * 表示する情報を引数で渡すため
         * コンストラクタではなく、
         * このstaticメソッドを利用する。
         *
         * @param strings 選択肢
         * @return 新規インスタンス
         */
        private fun newInstance(@StringRes title: Int, strings: ArrayList<String>): SelectStringDialog {
            val instance = SelectStringDialog()
            val args = Bundle()
            args.putInt(KEY_TITLE, title)
            args.putStringArrayList(KEY_STRING_LIST, strings)
            instance.arguments = args
            return instance
        }

        fun show(activity: FragmentActivity, @StringRes title: Int, strings: ArrayList<String>) {
            newInstance(title, strings).showAllowingStateLoss(activity.supportFragmentManager, "")
        }
    }
}
