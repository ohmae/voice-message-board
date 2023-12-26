/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.customtabs

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import net.mm2d.android.vmb.util.queryIntentServicesCompat

class CustomTabsHelper(context: Context) : CustomTabsServiceConnection(), LifecycleEventObserver {
    private val appContext: Context = context.applicationContext
    private var bound: Boolean = false
    private var session: CustomTabsSession? = null

    init {
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                bind()
            }
            Lifecycle.Event.ON_STOP -> {
                unbind()
            }
            else -> Unit
        }
    }

    private fun bind() {
        if (bound) return
        val packageName = findPackageNameToUse(appContext) ?: return
        bound = CustomTabsClient.bindCustomTabsService(
            appContext,
            packageName,
            this,
        )
    }

    private fun unbind() {
        if (!bound) return
        appContext.unbindService(this)
        bound = false
        session = null
    }

    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        client.warmup(0)
        session = client.newSession(null)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        session = null
    }

    fun mayLaunchUrl(url: String) {
        session?.mayLaunchUrl(Uri.parse(url), null, null)
    }

    fun mayLaunchUrl(urls: List<String>) {
        if (urls.isEmpty()) return
        session?.mayLaunchUrl(Uri.parse(urls[0]), null, makeOtherLikelyBundles(urls))
    }

    private fun makeOtherLikelyBundles(urls: List<String>): List<Bundle>? =
        if (urls.size == 1) {
            null
        } else {
            urls.subList(1, urls.size)
                .map { bundleOf(CustomTabsService.KEY_URL to Uri.parse(it)) }
        }

    fun createCustomTabsIntent(): CustomTabsIntent.Builder = CustomTabsIntent.Builder(session)

    fun launchUrl(context: Context, customTabsIntent: CustomTabsIntent, url: String): Boolean {
        if (session == null) {
            customTabsIntent.intent.setPackage(findPackageNameToUse(context))
        }
        return runCatching { customTabsIntent.launchUrl(context, Uri.parse(url)) }.isSuccess
    }

    companion object {
        private val PREFERRED_PACKAGES = listOf(
            // Chrome
            "com.android.chrome",
            // Chrome Beta
            "com.chrome.beta",
            // Chrome Dev
            "com.chrome.dev",
            // Chrome Canary
            "com.chrome.canary",
            // Chrome Local
            "com.google.android.apps.chrome",
        )

        private fun findPackageNameToUse(context: Context): String? {
            val browsers = OpenUriUtils.getBrowserPackages(context)
            val candidate = context.packageManager
                .queryIntentServicesCompat(Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION), 0)
                .mapNotNull { it.serviceInfo?.packageName }
                .filter { browsers.contains(it) }
            if (candidate.isEmpty()) return null
            if (candidate.size == 1) return candidate[0]
            OpenUriUtils.getDefaultBrowserPackage(context)?.let {
                if (candidate.contains(it)) {
                    return it
                }
            }
            return PREFERRED_PACKAGES.find { candidate.contains(it) }
        }
    }
}
