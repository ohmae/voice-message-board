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
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.list_item_theme.view.*
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.theme.Theme
import net.mm2d.android.vmb.util.isInActive
import net.mm2d.android.vmb.view.adapter.BaseListAdapter
import java.util.*

/**
 * テーマ選択ダイアログ。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SelectThemeDialog : DialogFragment() {
    private var eventListener: SelectThemeListener? = null

    interface SelectThemeListener {
        fun onSelectTheme(theme: Theme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SelectThemeListener) {
            eventListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val argument = requireArguments()
        val themeList = argument.getParcelableArrayList<Theme>(KEY_THEME_LIST)!!
        return AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.theme_select))
            .setAdapter(ThemeListAdapter(activity, themeList)) { _, which ->
                eventListener?.onSelectTheme(themeList[which])
            }
            .create()
    }

    private class ThemeListAdapter internal constructor(
        context: Context,
        collection: Collection<Theme>
    ) : BaseListAdapter<Theme>(context, collection) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val theme = getItem(position)
            val view = inflateView(R.layout.list_item_theme, convertView, parent)
            val sample = view.textSample
            sample.setBackgroundColor(theme.backgroundColor)
            sample.setTextColor(theme.foregroundColor)
            view.textTitle.text = theme.name
            return view
        }
    }

    companion object {
        private const val TAG = "SelectThemeDialog"
        private const val KEY_THEME_LIST = "KEY_THEME_LIST"

        fun show(activity: FragmentActivity, themes: ArrayList<Theme>) {
            if (activity.isInActive()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            SelectThemeDialog().also { dialog ->
                dialog.arguments = Bundle().also {
                    it.putParcelableArrayList(KEY_THEME_LIST, themes)
                }
            }.show(manager, TAG)
        }
    }
}
