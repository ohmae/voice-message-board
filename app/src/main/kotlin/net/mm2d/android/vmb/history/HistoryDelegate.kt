/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.history

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.vmb.MainActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.SelectStringDialog
import java.util.LinkedList

class HistoryDelegate(
    private val activity: FragmentActivity,
) {
    private val history = LinkedList<String>()

    fun updateHistory(
        values: Set<String>,
    ) {
        history.clear()
        history.addAll(values)
    }

    fun showSelectDialog() {
        if (history.isEmpty()) return
        SelectStringDialog.show(
            activity,
            MainActivity.REQUEST_SELECT,
            R.string.dialog_title_history,
            ArrayList(history),
        )
    }

    fun showClearDialog(
        onClear: () -> Unit,
    ) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_clear_history)
            .setMessage(R.string.dialog_message_clear_history)
            .setPositiveButton(R.string.ok) { _, _ ->
                onClear()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
