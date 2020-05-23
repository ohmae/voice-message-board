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
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.list_item_string.view.*
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.util.isInActive
import net.mm2d.android.vmb.view.adapter.BaseListAdapter
import java.util.*

class SelectStringDialog : DialogFragment() {
    private var eventListener: SelectStringListener? = null

    interface SelectStringListener {
        fun onSelectString(string: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SelectStringListener) {
            eventListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val argument = requireArguments()
        val stringList = argument.getStringArrayList(KEY_STRING_LIST)!!
        return AlertDialog.Builder(activity)
            .setTitle(argument.getInt(KEY_TITLE))
            .setAdapter(StringListAdapter(activity, stringList)) { _, which ->
                eventListener?.onSelectString(stringList[which])
            }
            .create()
    }

    class StringListAdapter(
        context: Context,
        collection: Collection<String>
    ) : BaseListAdapter<String>(context, collection) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            inflateView(R.layout.list_item_string, convertView, parent).also {
                it.textView.text = getItem(position)
            }
    }

    companion object {
        private const val TAG = "SelectStringDialog"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_STRING_LIST = "KEY_STRING_LIST"

        fun show(activity: FragmentActivity, @StringRes title: Int, strings: ArrayList<String>) {
            if (activity.isInActive()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            SelectStringDialog().also { dialog ->
                dialog.arguments = Bundle().also {
                    it.putInt(KEY_TITLE, title)
                    it.putStringArrayList(KEY_STRING_LIST, strings)
                }
            }.show(manager, TAG)
        }
    }
}
