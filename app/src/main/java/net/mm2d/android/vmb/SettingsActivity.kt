/*
 * Copyright(C) 2017 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.os.Bundle

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SettingsActivity : AppCompatPreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    override fun onIsMultiPane(): Boolean {
        return false
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return true
    }
}
