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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.databinding.ListItemThemeBinding
import net.mm2d.android.vmb.theme.Theme
import net.mm2d.android.vmb.util.isInActive
import net.mm2d.android.vmb.view.adapter.BaseListAdapter

class SelectThemeDialog : DialogFragment() {
    private var eventListener: SelectThemeListener? = null

    interface SelectThemeListener {
        fun onSelectTheme(theme: Theme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        eventListener = context as? SelectThemeListener
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

    private class ThemeListAdapter(
        context: Context,
        collection: Collection<Theme>
    ) : BaseListAdapter<Theme>(context, collection) {
        private val inflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            convertView ?: ListItemThemeBinding.inflate(inflater, parent, false).also {
                val theme = getItem(position)
                it.textSample.setBackgroundColor(theme.backgroundColor)
                it.textSample.setTextColor(theme.foregroundColor)
                it.textTitle.text = theme.name
            }.root
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
                dialog.arguments = bundleOf(KEY_THEME_LIST to themes)
            }.show(manager, TAG)
        }
    }
}
