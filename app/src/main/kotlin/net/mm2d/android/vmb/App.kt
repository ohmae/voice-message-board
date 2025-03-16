/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import net.mm2d.android.vmb.customtabs.CustomTabsHelperHolder
import net.mm2d.android.vmb.settings.Settings

@Suppress("unused")
open class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeOverrideWhenDebug()
        Settings.initialize(this)
        CustomTabsHelperHolder.initialize(this)
    }

    protected open fun initializeOverrideWhenDebug() {
        setUpStrictMode()
    }

    private fun setUpStrictMode() {
        StrictMode.setThreadPolicy(ThreadPolicy.LAX)
        StrictMode.setVmPolicy(VmPolicy.LAX)
    }
}
