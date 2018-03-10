/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.font

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import net.mm2d.android.vmb.R.*
import java.io.File

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class FileAdapter(context: Context, private val onClick: (file: File) -> Unit)
    : Adapter<FileAdapter.ViewHolder>() {
    private var files: Array<File> = emptyArray()
    private val inflater = LayoutInflater.from(context)

    fun setFiles(files: Array<File>) {
        this.files = files
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position], onClick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(layout.list_item_file_chooser, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.findViewById<TextView>(id.text)!!
        val icon = itemView.findViewById<ImageView>(id.icon)!!

        fun bind(file: File, onClick: (file: File) -> Unit) {
            itemView.setOnClickListener { onClick.invoke(file) }
            icon.setImageResource(if (file.isDirectory) drawable.ic_folder else drawable.ic_file)
            text.text = file.name
        }
    }
}
