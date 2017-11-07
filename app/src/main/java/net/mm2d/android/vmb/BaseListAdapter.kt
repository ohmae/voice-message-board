/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.*

/**
 * 指定要素のリストを内部に持つListAdapter。
 *
 * カスタムリストビューを作成するためのベースクラスとして利用。
 *
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
abstract class BaseListAdapter<T>(context: Context) : BaseAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val list: MutableList<T> = ArrayList()

    /**
     * インスタンス作成。
     *
     * @param context    コンテキスト
     * @param collection 要素
     */
    constructor(context: Context, collection: Collection<T>) : this(context) {
        list.addAll(collection)
    }

    /**
     * Viewのレイアウトを作成する。
     *
     * getViewの中で利用することを想定
     * convertViewが非nullであればconvertViewをそのまま返す。
     * convertViewがnullの場合、指定layoutでinflateしたViewを返す。
     *
     * @param layout      レイアウト
     * @param convertView nullの場合のみレイアウトが作成される
     * @param parent      親要素
     * @return レイアウトが作成されたView
     */
    protected fun inflateView(layout: Int, convertView: View?, parent: ViewGroup): View {
        return convertView ?: inflater.inflate(layout, parent, false)
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): T {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
