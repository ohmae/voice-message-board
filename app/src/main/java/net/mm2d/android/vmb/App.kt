/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.multidex.MultiDexApplication
import net.mm2d.android.vmb.customtabs.CustomTabsHelperHolder
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.log.Logger
import net.mm2d.log.android.AndroidSenders

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@Suppress("unused")
class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        setUpLogger()
        setStrictMode()
        Settings.initialize(this)
        CustomTabsHelperHolder.initialize(this)
    }

    private fun setUpLogger() {
        if (!BuildConfig.DEBUG) {
            return
        }
        Logger.setSender(AndroidSenders.create())
        Logger.setLogLevel(Logger.VERBOSE)
        AndroidSenders.appendCaller(true)
        AndroidSenders.appendThread(true)
    }

    private fun setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults()
        } else {
            StrictMode.setThreadPolicy(ThreadPolicy.LAX)
            StrictMode.setVmPolicy(VmPolicy.LAX)
        }
    }
}
