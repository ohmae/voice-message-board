/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.history

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.settings.Settings
import java.util.*

class HistoryDelegate(
    private val activity: FragmentActivity,
    private val historyFab: FloatingActionButton
) {
    private val settings = Settings.get()
    private val history = LinkedList(settings.history)

    init {
        if (history.isEmpty()) {
            historyFab.hide()
        }
        historyFab.setOnClickListener { showSelectDialog() }
    }

    fun exist(): Boolean = !history.isEmpty()

    fun showSelectDialog() {
        if (history.isEmpty()) return
        SelectStringDialog.show(activity, R.string.dialog_title_history, ArrayList(history))
    }

    fun showClearDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_clear_history)
            .setMessage(R.string.dialog_message_clear_history)
            .setPositiveButton(R.string.ok) { _, _ -> clear() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun put(string: String) {
        history.remove(string)
        history.addFirst(string)
        while (history.size > MAX_HISTORY) {
            history.removeLast()
        }
        settings.history = HashSet(history)
        historyFab.show()
    }

    private fun clear() {
        history.clear()
        settings.history = HashSet(history)
        historyFab.hide()
    }

    companion object {
        private const val MAX_HISTORY = 30
    }
}
