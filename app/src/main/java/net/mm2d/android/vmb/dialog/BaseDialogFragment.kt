/*
 * Copyright (c) 2018 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.dialog

import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
open class BaseDialogFragment : DialogFragment() {
    fun showAllowingStateLoss(manager: FragmentManager?, tag: String) {
        manager?.beginTransaction()?.let {
            it.add(this, tag)
            it.commitAllowingStateLoss()
        }
    }
}
