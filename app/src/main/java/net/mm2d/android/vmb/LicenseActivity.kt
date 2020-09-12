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
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_license.*

class LicenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setTitle(R.string.pref_title_license)
            setDisplayHomeAsUpEnabled(true)
        }
        webView.settings.setSupportZoom(false)
        webView.settings.displayZoomControls = false
        webView.loadUrl("file:///android_asset/license.html")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return true
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LicenseActivity::class.java))
        }
    }
}
