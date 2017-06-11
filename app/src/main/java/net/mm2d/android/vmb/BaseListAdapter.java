/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 指定要素のリストを内部に持つListAdapter。
 *
 * カスタムリストビューを作成するためのベースクラスとして利用。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public abstract class BaseListAdapter<T> extends BaseAdapter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<T> mList;

    /**
     * インスタンス作成。
     *
     * @param context コンテキスト
     */
    public BaseListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
    }

    /**
     * インスタンス作成。
     *
     * @param context コンテキスト
     * @param collection 要素
     */
    public BaseListAdapter(Context context, Collection<? extends T> collection) {
        this(context);
        mList.addAll(collection);
    }

    /**
     * コンテキストを返す。
     *
     * @return コンテキスト
     */
    protected Context getContext() {
        return mContext;
    }

    /**
     * 要素を追加。
     *
     * @param item 要素
     */
    public void add(T item) {
        mList.add(item);
    }

    /**
     * 位置を指定して要素を追加。
     *
     * @param location 位置
     * @param item 要素
     */
    public void add(int location, T item) {
        mList.add(location, item);
    }

    /**
     * 要素をまとめて追加。
     *
     * @param collection 要素のコレクション
     */
    public void addAll(Collection<? extends T> collection) {
        mList.addAll(collection);
    }

    /**
     * 指定場所の要素を削除。
     *
     * @param location 場所
     * @return 削除できた場合その要素
     */
    public T remove(int location) {
        return mList.remove(location);
    }

    /**
     * 要素を削除。
     *
     * @param object 削除する要素
     * @return 削除できた場合true、指定要素がなかった場合false
     */
    public boolean remove(T object) {
        return mList.remove(object);
    }

    /**
     * 要素をクリア。
     */
    public void clear() {
        mList.clear();
    }

    /**
     * Viewのレイアウトを作成する。
     *
     * getViewの中で利用することを想定
     * convertViewが非nullであればconvertViewをそのまま返す。
     * convertViewがnullの場合、指定layoutでinflateしたViewを返す。
     *
     * @param layout レイアウト
     * @param convertView nullの場合のみレイアウトが作成される
     * @param parent 親要素
     * @return レイアウトが作成されたView
     */
    protected View inflateView(int layout, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(layout, parent, false);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
