/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.history

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.mm2d.android.vmb.MainActivity
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.dialog.SelectStringDialog
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.android.vmb.settings.SettingsData
import java.util.LinkedList

class HistoryDelegate(
    private val activity: FragmentActivity,
    private val settings: Settings,
) {
    private val history = LinkedList<String>()

    fun updateHistory(
        settingsData: SettingsData,
    ) {
        history.clear()
        history.addAll(settingsData.history)
    }

    fun exist(): Boolean = history.isNotEmpty()

    fun showSelectDialog() {
        if (history.isEmpty()) return
        SelectStringDialog.show(
            activity,
            MainActivity.REQUEST_SELECT,
            R.string.dialog_title_history,
            ArrayList(history),
        )
    }

    fun showClearDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_clear_history)
            .setMessage(R.string.dialog_message_clear_history)
            .setPositiveButton(R.string.ok) { _, _ ->
                activity.lifecycleScope.launch {
                    clear()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    suspend fun put(
        string: String,
    ) {
        history.remove(string)
        history.addFirst(string)
        while (history.size > MAX_HISTORY) {
            history.removeLast()
        }
        settings.updateHistory(history.toSet())
    }

    suspend fun clear() {
        history.clear()
        settings.updateHistory(emptySet())
    }

    companion object {
        private const val MAX_HISTORY = 30
    }
}
