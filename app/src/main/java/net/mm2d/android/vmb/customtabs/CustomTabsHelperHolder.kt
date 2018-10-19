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
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import net.mm2d.android.vmb.R
import net.mm2d.android.vmb.util.AttrUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object CustomTabsHelperHolder {
    private lateinit var customTabsHelper: CustomTabsHelper

    fun initialize(application: Application) {
        customTabsHelper = CustomTabsHelper()
        application.registerActivityLifecycleCallbacks(customTabsHelper.binder)
    }

    fun mayLaunchUrl(url: String) {
        customTabsHelper.mayLaunchUrl(url)
    }

    fun mayLaunchUrl(urls: List<String>) {
        customTabsHelper.mayLaunchUrl(urls)
    }

    fun openUrl(context: Context, url: String) {
        if (OpenUriUtils.hasDefaultAppOtherThanBrowser(context, url)) {
            openByNormalIntent(context, url)
            return
        }
        val builder = customTabsHelper.createCustomTabsIntent()
                .setShowTitle(true)
                .setToolbarColor(AttrUtils.resolveColor(context, R.attr.colorPrimary, Color.BLACK))
        AppCompatResources.getDrawable(context, R.drawable.ic_arrow_back)?.toBitmap()
                ?.let { builder.setCloseButtonIcon(it) }
        if (customTabsHelper.launchUrl(context, builder.build(), url)) {
            return
        }
        openByNormalIntent(context, url)
    }

    private fun openByNormalIntent(context: Context, url: String) {
        try {
            context.startActivity(OpenUriUtils.makeBrowseIntent(url))
        } catch (ignored: ActivityNotFoundException) {
        }
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return bitmap
        return Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888).also {
            val canvas = Canvas(it)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
    }
}
