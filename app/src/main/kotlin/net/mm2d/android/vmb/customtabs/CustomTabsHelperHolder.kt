/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.customtabs

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.core.graphics.createBitmap
import net.mm2d.android.vmb.R

object CustomTabsHelperHolder {
    private lateinit var customTabsHelper: CustomTabsHelper

    fun initialize(
        application: Application,
    ) {
        customTabsHelper = CustomTabsHelper(application)
    }

    @Suppress("unused")
    fun mayLaunchUrl(
        url: String,
    ) {
        customTabsHelper.mayLaunchUrl(url)
    }

    fun mayLaunchUrl(
        urls: List<String>,
    ) {
        customTabsHelper.mayLaunchUrl(urls)
    }

    fun openUrl(
        context: Context,
        url: String,
    ) {
        if (OpenUriUtils.hasDefaultAppOtherThanBrowser(context, url)) {
            openByNormalIntent(context, url)
            return
        }
        val builder = customTabsHelper.createCustomTabsIntent()
            .setShowTitle(true)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder().build(),
            )
        AppCompatResources.getDrawable(context, R.drawable.ic_arrow_back)
            ?.toBitmap()
            ?.let { builder.setCloseButtonIcon(it) }
        if (customTabsHelper.launchUrl(context, builder.build(), url)) {
            return
        }
        openByNormalIntent(context, url)
    }

    private fun openByNormalIntent(
        context: Context,
        url: String,
    ) {
        try {
            context.startActivity(OpenUriUtils.makeBrowseIntent(url))
        } catch (_: ActivityNotFoundException) {
        }
    }

    private fun Drawable.toBitmap(): Bitmap =
        if (this is BitmapDrawable) {
            bitmap
        } else {
            createBitmap(intrinsicWidth, intrinsicHeight)
                .also { bitmap ->
                    Canvas(bitmap).also {
                        setBounds(0, 0, it.width, it.height)
                        draw(it)
                    }
                }
        }
}
