/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.font

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_file_chooser.*
import net.mm2d.android.vmb.R.layout
import net.mm2d.android.vmb.R.string
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.PermissionDialog.OnCancelListener
import net.mm2d.android.vmb.dialog.PermissionDialog.OnPositiveClickListener
import net.mm2d.android.vmb.permission.PermissionHelper
import net.mm2d.android.vmb.util.Toaster
import java.io.File

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class FontFileChooserActivity : AppCompatActivity(), OnCancelListener, OnPositiveClickListener {
    private val defaultPath = Environment.getExternalStorageDirectory()
    private var currentPath = defaultPath
    private val compositeDisposable = CompositeDisposable()
    private lateinit var fileAdapter: FileAdapter
    private lateinit var permissionHelper: PermissionHelper
    private val comparator = Comparator<File> { o1, o2 ->
        if (o1.isDirectory != o2.isDirectory) {
            if (o1.isDirectory) -1 else 1
        } else {
            o1.compareTo(o2)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_file_chooser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(string.title_file_chooser)
        permissionHelper = PermissionHelper(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            PERMISSION_REQUEST_CODE
        )
        setInitialPath()
        initRecyclerView()
        if (savedInstanceState == null) {
            checkPermission()
        } else {
            savedInstanceState.getString(CURRENT_PATH_KEY)?.let {
                currentPath = File(it)
            }
            setUpDirectory(currentPath)
        }
    }

    private fun setInitialPath() {
        val path = intent?.getStringExtra(EXTRA_INITIAL_PATH) ?: return
        File(path).let {
            if (it.exists() && it.canRead()) currentPath = if (it.isFile) it.parentFile else it
        }
        intent = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_PATH_KEY, currentPath.absolutePath)
    }

    private fun initRecyclerView() {
        fileAdapter = FileAdapter(this) {
            if (!it.canRead()) {
                Toaster.show(this, string.toast_can_not_read_file_or_directory)
            } else if (it.isDirectory) {
                setUpDirectory(it)
            } else if (FontUtils.isValidFontFile(it)) {
                setResult(Activity.RESULT_OK, Intent().setData(Uri.fromFile(it)))
                finish()
            } else {
                Toaster.showShort(this, string.toast_not_a_valid_font)
            }
        }
        recyclerView.adapter = fileAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun setUpDirectory(file: File) {
        currentPath = file
        supportActionBar?.subtitle = currentPath.absolutePath
        fileAdapter.setFiles(emptyArray())
        progressBar.visibility = View.VISIBLE
        Single.fromCallable { file.listFiles().apply { sortWith(comparator) } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                fileAdapter.setFiles(it)
                fileAdapter.notifyDataSetChanged()
                progressBar.visibility = View.INVISIBLE
            })
            .addTo(compositeDisposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (currentPath != defaultPath) {
            setUpDirectory(currentPath.parentFile)
            return
        }
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    private fun checkPermission() {
        if (permissionHelper.requestPermissionIfNeed()) {
            return
        }
        setUpDirectory(currentPath)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            PermissionHelper.Result.OTHER -> return
            PermissionHelper.Result.GRANTED -> setUpDirectory(currentPath)
            PermissionHelper.Result.DENIED -> {
                Toaster.show(this, string.toast_should_allow_storage_permission)
                finish()
            }
            PermissionHelper.Result.DENIED_ALWAYS ->
                PermissionDialog.show(this, string.dialog_storage_permission_message)
        }
    }

    override fun onCancel() {
        Toaster.show(this, string.toast_should_allow_storage_permission)
        finish()
    }

    override fun onPositiveClick() {
        finish()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val CURRENT_PATH_KEY = "CURRENT_PATH_KEY"
        private const val EXTRA_INITIAL_PATH = "EXTRA_INITIAL_PATH"

        fun makeIntent(context: Context, path: String): Intent {
            return Intent(context, FontFileChooserActivity::class.java).apply {
                putExtra(EXTRA_INITIAL_PATH, path)
            }
        }
    }
}
