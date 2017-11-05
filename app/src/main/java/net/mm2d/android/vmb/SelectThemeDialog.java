/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * テーマ選択ダイアログ。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class SelectThemeDialog extends DialogFragment {
    /**
     * テーマが選択された時に呼ばれるリスナー。
     *
     * 呼び出し元のActivityに実装して利用する。
     */
    public interface SelectThemeListener {
        /**
         * テーマが選択された。
         *
         * @param theme 選択されたテーマ
         */
        void onSelectTheme(@NonNull Theme theme);
    }

    /**
     * テーマリストのkey
     */
    private static final String KEY_THEME_LIST = "KEY_THEME_LIST";

    /**
     * Dialogのインスタンスを作成。
     *
     * 表示するテーマリストを渡すため、
     * コンストラクタではなく
     * このstaticメソッドを利用する。
     *
     * @param themes テーマリスト
     * @return 新規インスタンス
     */
    @NonNull
    public static SelectThemeDialog newInstance(@NonNull final ArrayList<Theme> themes) {
        final Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_THEME_LIST, themes);
        final SelectThemeDialog instance = new SelectThemeDialog();
        instance.setArguments(args);
        return instance;
    }

    private SelectThemeListener mEventListener;

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        if (context instanceof SelectThemeListener) {
            mEventListener = (SelectThemeListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final ArrayList<Theme> themeList = args.getParcelableArrayList(KEY_THEME_LIST);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.theme_select));
        final ListAdapter adapter = new ThemeListAdapter(getActivity(), themeList);
        builder.setAdapter(adapter, (dialog, which) -> {
            if (mEventListener != null) {
                mEventListener.onSelectTheme(themeList.get(which));
            }
        });
        return builder.create();
    }

    private static class ThemeListAdapter extends BaseListAdapter<Theme> {
        ThemeListAdapter(
                @NonNull final Context context,
                @NonNull final Collection<? extends Theme> collection) {
            super(context, collection);
        }

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent) {
            final View view = inflateView(R.layout.list_item_theme, convertView, parent);
            final TextView sample = view.findViewById(R.id.textSample);
            final TextView title = view.findViewById(R.id.textTitle);
            final Theme theme = getItem(position);
            sample.setBackgroundColor(theme.getBackgroundColor());
            sample.setTextColor(theme.getForegroundColor());
            title.setText(theme.getName());
            return view;
        }
    }
}
