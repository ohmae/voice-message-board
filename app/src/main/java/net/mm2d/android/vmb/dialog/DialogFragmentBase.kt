package net.mm2d.android.vmb.dialog

import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
open class DialogFragmentBase : DialogFragment() {
    fun showAllowingStateLoss(manager: FragmentManager?, tag: String) {
        manager?.beginTransaction()?.let {
            it.add(this, tag)
            it.commitAllowingStateLoss()
        }
    }
}
