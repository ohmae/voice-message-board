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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.databinding.ListItemStringBinding
import net.mm2d.android.vmb.util.isInActive
import net.mm2d.android.vmb.view.adapter.BaseListAdapter

class SelectStringDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val argument = requireArguments()
        val stringList = argument.getStringArrayList(KEY_STRING_LIST)!!
        return AlertDialog.Builder(activity)
            .setTitle(argument.getInt(KEY_TITLE))
            .setAdapter(StringListAdapter(activity, stringList)) { _, which ->
                val requestKey = requireArguments().getString(KEY_REQUEST, "")
                parentFragmentManager.setFragmentResult(requestKey, bundleOf(KEY_RESULT to stringList[which]))
            }
            .create()
    }

    class StringListAdapter(
        context: Context,
        collection: Collection<String>
    ) : BaseListAdapter<String>(context, collection) {
        private val inflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            convertView ?: ListItemStringBinding.inflate(inflater, parent, false).also {
                it.textView.text = getItem(position)
            }.root
    }

    companion object {
        private const val TAG = "SelectStringDialog"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_STRING_LIST = "KEY_STRING_LIST"
        private const val KEY_REQUEST = "KEY_REQUEST"
        private const val KEY_RESULT = "KEY_RESULT"

        fun registerListener(activity: FragmentActivity, requestKey: String, listener: (String) -> Unit) {
            val manager = activity.supportFragmentManager
            manager.setFragmentResultListener(requestKey, activity) { _, result ->
                Log.e("XXXX", "$result")
                result.getString(KEY_RESULT)?.let(listener)
            }
        }

        fun show(activity: FragmentActivity, requestKey: String, @StringRes title: Int, strings: ArrayList<String>) {
            if (activity.isInActive()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            SelectStringDialog().also { dialog ->
                dialog.arguments = bundleOf(
                    KEY_REQUEST to requestKey,
                    KEY_TITLE to title,
                    KEY_STRING_LIST to strings,
                )
            }.show(manager, TAG)
        }
    }
}
