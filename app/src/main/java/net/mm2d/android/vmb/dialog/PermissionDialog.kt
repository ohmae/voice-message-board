/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.util.isInActive

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class PermissionDialog : DialogFragment() {
    interface OnCancelListener {
        fun onCancel()
    }

    interface OnPositiveClickListener {
        fun onPositiveClick()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return AlertDialog.Builder(context)
            .setTitle(R.string.dialog_permission_title)
            .setMessage(R.string.dialog_microphone_permission_message)
            .setPositiveButton(R.string.app_info) { _, _ ->
                startAppInfo(context)
                (context as? OnPositiveClickListener)?.onPositiveClick()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        (context as? OnCancelListener)?.onCancel()
    }

    private fun startAppInfo(context: Context) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:" + context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "PermissionDialog"
        private const val TITLE = "TITLE"

        fun show(activity: FragmentActivity, @StringRes message: Int) {
            if (activity.isInActive()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            PermissionDialog().also { dialog ->
                dialog.arguments = Bundle().also {
                    it.putInt(TITLE, message)
                }
            }.show(manager, TAG)
        }
    }
}
