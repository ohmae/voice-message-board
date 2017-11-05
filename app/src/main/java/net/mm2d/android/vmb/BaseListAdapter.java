/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
abstract class BaseListAdapter<T> extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final List<T> mList;

    /**
     * インスタンス作成。
     *
     * @param context コンテキスト
     */
    BaseListAdapter(@NonNull final Context context) {
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
    }

    /**
     * インスタンス作成。
     *
     * @param context    コンテキスト
     * @param collection 要素
     */
    BaseListAdapter(
            @NonNull final Context context,
            @NonNull final Collection<? extends T> collection) {
        this(context);
        mList.addAll(collection);
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
    @NonNull
    protected View inflateView(
            final int layout,
            @Nullable final View convertView,
            @NonNull final ViewGroup parent) {
        return convertView != null ? convertView : mInflater.inflate(layout, parent, false);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(final int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }
}
