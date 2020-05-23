/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.*

abstract class BaseListAdapter<T>(context: Context) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    private val list: MutableList<T> = ArrayList()

    constructor(context: Context, collection: Collection<T>) : this(context) {
        list.addAll(collection)
    }

    protected fun inflateView(layout: Int, convertView: View?, parent: ViewGroup): View =
        convertView ?: inflater.inflate(layout, parent, false)

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): T = list[position]

    override fun getItemId(position: Int): Long = position.toLong()
}
