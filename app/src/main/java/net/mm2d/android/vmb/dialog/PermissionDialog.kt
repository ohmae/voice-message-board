/*
 * Copyright (c) 2017 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import net.mm2d.android.vmb.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class PermissionDialog : BaseDialogFragment() {
    interface OnCancelListener {
        fun onCancel()
    }

    private var onCancelListener: OnCancelListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnCancelListener) {
            onCancelListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        onCancelListener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = context!!
        return AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_permission_title)
                .setMessage(R.string.dialog_microphone_permission_message)
                .setPositiveButton(R.string.app_info) { _, _ ->
                    startAppInfo(ctx)
                }
                .setNegativeButton(R.string.cancel, { _, _ -> onCancelListener?.onCancel() })
                .create()
    }

    private fun startAppInfo(context: Context) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:" + context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    companion object {
        private const val TITLE = "TITLE"
        fun newInstance(@StringRes message: Int): PermissionDialog = PermissionDialog().apply {
            arguments = Bundle().apply { putInt(TITLE, message) }
        }
    }
}
