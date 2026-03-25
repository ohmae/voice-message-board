/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.customtabs

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import net.mm2d.android.vmb.util.queryIntentActivitiesCompat
import net.mm2d.android.vmb.util.resolveActivityCompat

internal object OpenUriUtils {
    fun getBrowserPackages(
        context: Context,
    ): Set<String> =
        context.packageManager
            .queryIntentActivitiesCompat(makeBrowserTestIntent(), PackageManager.MATCH_ALL)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()

    fun getDefaultBrowserPackage(
        context: Context,
    ): String? {
        val packageName = context.packageManager
            .resolveActivityCompat(makeBrowserTestIntent(), 0)
            ?.activityInfo
            ?.packageName
            ?: return null
        return if (getBrowserPackages(context).contains(packageName)) packageName else null
    }

    fun makeBrowseIntent(
        uri: String,
    ): Intent =
        Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

    private fun makeBrowserTestIntent(): Intent = makeBrowseIntent("http://www.example.com/")

    fun hasDefaultAppOtherThanBrowser(
        context: Context,
        uri: String,
    ): Boolean {
        val packageManager = context.packageManager
        val intent = makeBrowseIntent(uri)
        val packageName = packageManager.resolveActivityCompat(intent, 0)
            ?.activityInfo?.packageName ?: return false
        if (getBrowserPackages(context).contains(packageName)) {
            return false
        }
        return packageManager.queryIntentActivitiesCompat(intent, 0)
            .any { it.activityInfo?.packageName == packageName }
    }
}
