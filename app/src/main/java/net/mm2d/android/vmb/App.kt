/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.app.Application
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import net.mm2d.android.vmb.settings.Settings
import net.mm2d.log.AndroidLogInitializer
import net.mm2d.log.Log

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.setInitializer(AndroidLogInitializer.get())
        Log.initialize(BuildConfig.DEBUG, true)
        RxJavaPlugins.setErrorHandler { e ->
            when (e) {
                is UndeliverableException -> Log.w(e.cause)
                else -> Log.w(e)
            }
        }
        Settings.initialize(this)
    }
}
