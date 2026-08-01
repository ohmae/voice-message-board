/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import net.mm2d.android.vmb.ui.license.LicenseScreen
import net.mm2d.android.vmb.ui.theme.AppTheme

class LicenseActivity : ComponentActivity() {
    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                LicenseScreen(
                    onBackClick = { finish() },
                )
            }
        }
    }

    companion object {
        fun start(
            context: Context,
        ) {
            context.startActivity(Intent(context, LicenseActivity::class.java))
        }
    }
}
