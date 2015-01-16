/**
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 */

package net.mm2d.android.vmb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
        void onSelectTheme(Theme theme);
    }

    /**
     * タイトルのkey
     */
    private static final String KEY_TITLE = "KEY_TITLE";
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
     * @param title タイトル
     * @param themes テーマリスト
     * @return 新規インスタンス
     */
    public static SelectThemeDialog newInstance(String title, ArrayList<Theme> themes) {
        final Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putParcelableArrayList(KEY_THEME_LIST, themes);
        final SelectThemeDialog instance = new SelectThemeDialog();
        instance.setArguments(args);
        return instance;
    }

    private SelectThemeListener mEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof SelectThemeListener) {
            mEventListener = (SelectThemeListener) activity;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final String title = args.getString(KEY_TITLE);
        final ArrayList<Theme> themeList = args.getParcelableArrayList(KEY_THEME_LIST);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        final ListAdapter adapter = new ThemeListAdapter(getActivity(), themeList);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mEventListener != null) {
                    mEventListener.onSelectTheme(themeList.get(which));
                }
            }
        });
        return builder.create();
    }

    private static class ThemeListAdapter extends BaseListAdapter<Theme> {
        public ThemeListAdapter(Context context) {
            super(context);
        }

        public ThemeListAdapter(Context context, Collection<? extends Theme> collection) {
            super(context, collection);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = inflateView(R.layout.list_item_theme, convertView, parent);
            final TextView sample = (TextView) view.findViewById(R.id.textSample);
            final TextView title = (TextView) view.findViewById(R.id.textTitle);
            final Theme theme = getItem(position);
            sample.setBackgroundColor(theme.getBackground());
            sample.setTextColor(theme.getForeground());
            title.setText(theme.getName());
            return view;
        }
    }

}
