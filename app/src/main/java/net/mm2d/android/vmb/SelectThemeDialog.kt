/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*

/**
 * テーマ選択ダイアログ。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SelectThemeDialog : DialogFragment() {

    private var eventListener: SelectThemeListener? = null

    /**
     * テーマが選択された時に呼ばれるリスナー。
     *
     * 呼び出し元のActivityに実装して利用する。
     */
    interface SelectThemeListener {
        /**
         * テーマが選択された。
         *
         * @param theme 選択されたテーマ
         */
        fun onSelectTheme(theme: Theme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SelectThemeListener) {
            eventListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val themeList = arguments.getParcelableArrayList<Theme>(KEY_THEME_LIST)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.theme_select))
        val adapter = ThemeListAdapter(activity, themeList!!)
        builder.setAdapter(adapter) { dialog, which ->
            eventListener?.onSelectTheme(themeList[which])
        }
        return builder.create()
    }

    private class ThemeListAdapter internal constructor(context: Context, collection: Collection<Theme>)
        : BaseListAdapter<Theme>(context, collection) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val theme = getItem(position)
            val view = inflateView(R.layout.list_item_theme, convertView, parent)
            val sample = view.findViewById<TextView>(R.id.textSample)
            sample.setBackgroundColor(theme.backgroundColor)
            sample.setTextColor(theme.foregroundColor)
            view.findViewById<TextView>(R.id.textTitle).text = theme.name
            return view
        }
    }

    companion object {

        /**
         * テーマリストのkey
         */
        private val KEY_THEME_LIST = "KEY_THEME_LIST"

        /**
         * Dialogのインスタンスを作成。
         *
         * 表示するテーマリストを渡すため、
         * コンストラクタではなく
         * このstaticメソッドを利用する。
         *
         * @param themes テーマリスト
         * @return 新規インスタンス
         */
        fun newInstance(themes: ArrayList<Theme>): SelectThemeDialog {
            val args = Bundle()
            args.putParcelableArrayList(KEY_THEME_LIST, themes)
            val instance = SelectThemeDialog()
            instance.arguments = args
            return instance
        }
    }
}
