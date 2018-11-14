/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.customtabs

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsHelper : CustomTabsServiceConnection() {
    private var bound: Boolean = false
    private var session: CustomTabsSession? = null

    val binder: ActivityLifecycleCallbacks
        get() = CustomTabsBinder()

    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        client.warmup(0)
        session = client.newSession(null)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        session = null
    }

    private fun bind(context: Context) {
        if (!bound) {
            val packageName = findPackageNameToUse(context) ?: return
            bound = CustomTabsClient.bindCustomTabsService(
                context.applicationContext,
                packageName,
                this
            )
        }
    }

    private fun unbind(context: Context) {
        if (bound) {
            context.applicationContext.unbindService(this)
            bound = false
            session = null
        }
    }

    fun mayLaunchUrl(url: String) {
        session?.mayLaunchUrl(Uri.parse(url), null, null)
    }

    fun mayLaunchUrl(urls: List<String>) {
        if (urls.isEmpty()) return
        session?.mayLaunchUrl(Uri.parse(urls[0]), null, makeOtherLikelyBundles(urls))
    }

    private fun makeOtherLikelyBundles(urls: List<String>): List<Bundle>? {
        if (urls.size == 1) return null
        return urls.subList(1, urls.size)
            .map { Bundle().apply { putParcelable(CustomTabsService.KEY_URL, Uri.parse(it)) } }
    }

    fun createCustomTabsIntent(): CustomTabsIntent.Builder {
        return CustomTabsIntent.Builder(session)
    }

    fun launchUrl(context: Context, customTabsIntent: CustomTabsIntent, url: String): Boolean {
        customTabsIntent.intent.putExtra(
            EXTRA_CUSTOM_TABS_KEEP_ALIVE,
            Intent(context, KeepAliveService::class.java)
        )
        if (session == null) {
            customTabsIntent.intent.setPackage(findPackageNameToUse(context))
        }
        return try {
            customTabsIntent.launchUrl(context, Uri.parse(url))
            true
        } catch (ignored: ActivityNotFoundException) {
            false
        }
    }

    private inner class CustomTabsBinder : ActivityLifecycleCallbacks {
        private var createdCount: Int = 0

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            createdCount++
            bind(activity)
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            createdCount--
            if (createdCount == 0 && activity.isFinishing) {
                unbind(activity)
            }
        }
    }

    companion object {
        private val PREFERRED_PACKAGES = listOf(
            "com.android.chrome", // Chrome
            "com.chrome.beta", // Chrome Beta
            "com.chrome.dev", // Chrome Dev
            "com.chrome.canary", // Chrome Canary
            "com.google.android.apps.chrome" // Chrome Local
        )
        private const val EXTRA_CUSTOM_TABS_KEEP_ALIVE =
            "android.support.customtabs.extra.KEEP_ALIVE"

        private fun findPackageNameToUse(context: Context): String? {
            val browsers = OpenUriUtils.getBrowserPackages(context)
            val candidate = context.packageManager
                .queryIntentServices(Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION), 0)
                .mapNotNull { it?.serviceInfo?.packageName }
                .filter { browsers.contains(it) }
            if (candidate.isEmpty()) return null
            if (candidate.size == 1) return candidate[0]
            val defaultBrowser = OpenUriUtils.getDefaultBrowserPackage(context)
            if (defaultBrowser != null && candidate.contains(defaultBrowser)) {
                return defaultBrowser
            }
            return PREFERRED_PACKAGES.find { candidate.contains(it) }
        }
    }
}
