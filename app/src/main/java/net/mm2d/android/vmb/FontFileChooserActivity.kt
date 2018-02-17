/*
 * Copyright (c) 2018 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_file_chooser.*
import net.mm2d.android.vmb.FontFileChooserActivity.FileAdapter.ViewHolder
import net.mm2d.android.vmb.dialog.PermissionDialog
import net.mm2d.android.vmb.dialog.PermissionDialog.OnCancelListener
import net.mm2d.android.vmb.util.Toaster
import net.mm2d.log.Log
import java.io.File

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class FontFileChooserActivity : AppCompatActivity(), OnCancelListener {
    private val defaultPath = Environment.getExternalStorageDirectory()
    private var currentPath = defaultPath
    private lateinit var adapter: FileAdapter
    private val comparator = Comparator<File> { o1, o2 ->
        if (o1.isDirectory != o2.isDirectory) {
            if (o1.isDirectory) -1 else 1
        } else {
            o1.compareTo(o2)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_chooser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.title_file_chooser)
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
        adapter = FileAdapter(this) {
            if (!it.canRead()) {
                Toaster.show(this, R.string.toast_can_not_read_file_or_directory)
            } else if (it.isDirectory) {
                setUpDirectory(it)
            } else if (isValidFontFile(it)) {
                setResult(Activity.RESULT_OK, Intent().setData(Uri.fromFile(it)))
                finish()
            } else {
                Toaster.showShort(this, R.string.toast_not_a_valid_font)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun isValidFontFile(file: File): Boolean {
        try {
            Typeface.createFromFile(file)
            return true
        } catch (e: Exception) {
            Log.w(e)
        }
        return false
    }

    private fun setUpDirectory(file: File) {
        currentPath = file
        supportActionBar?.subtitle = currentPath.absolutePath
        adapter.setFiles(emptyArray())
        progressBar.visibility = View.VISIBLE
        Single.fromCallable { file.listFiles().apply { sortWith(comparator) } }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    adapter.setFiles(it)
                    adapter.notifyDataSetChanged()
                    progressBar.visibility = View.INVISIBLE
                })
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

    @SuppressLint("NewApi")
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            recyclerView.visibility = View.INVISIBLE
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            return
        }
        recyclerView.visibility = View.VISIBLE
        setUpDirectory(currentPath)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST_CODE || permissions.isEmpty()) {
            return
        }
        val index = permissions.indexOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (index < 0 || index > grantResults.size) {
            return
        }
        if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
            recyclerView.visibility = View.VISIBLE
            setUpDirectory(currentPath)
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toaster.show(this, R.string.toast_should_allow_storage_permission)
            finish()
        } else {
            supportFragmentManager?.let {
                PermissionDialog.newInstance(R.string.dialog_storage_permission_message)
                        .showAllowingStateLoss(it, "")
            }
        }
    }

    override fun onCancel() {
        Toaster.show(this, R.string.toast_should_allow_storage_permission)
        finish()
    }

    private class FileAdapter(context: Context, private val onClick: (file: File) -> Unit) : RecyclerView.Adapter<ViewHolder>() {
        private var files: Array<File> = emptyArray()
        private val inflater = LayoutInflater.from(context)

        fun setFiles(files: Array<File>) {
            this.files = files
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return files.size
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            holder?.bind(files[position], onClick)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val view = inflater.inflate(R.layout.list_item_file_chooser, parent, false)
            return ViewHolder(view)
        }

        private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val text = itemView.findViewById<TextView>(R.id.text)!!
            val icon = itemView.findViewById<ImageView>(R.id.icon)!!

            fun bind(file: File, onClick: (file: File) -> Unit) {
                itemView.setOnClickListener { onClick.invoke(file) }
                icon.setImageResource(if (file.isDirectory) R.drawable.ic_folder else R.drawable.ic_file)
                text.text = file.name
            }
        }
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
