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
 * 音声認識の結果複数の候補が出た時に表示するダイアログ。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class SelectStringDialog extends DialogFragment {
    /**
     * 文字列を選択した時に呼ばれるリスナー
     *
     * 呼び出し元のActivityに実装して利用する。
     */
    public interface SelectStringListener {
        /**
         * 文字列が選択された。
         *
         * @param string 選択された文字列。
         */
        void onSelectString(@NonNull String string);
    }

    /**
     * 選択文字列のkey
     */
    private static final String KEY_STRING_LIST = "KEY_STRING_LIST";

    /**
     * Dialogのインスタンスを作成。
     *
     * 表示する情報を引数で渡すため
     * コンストラクタではなく、
     * このstaticメソッドを利用する。
     *
     * @param strings 選択肢
     * @return 新規インスタンス
     */
    @NonNull
    public static SelectStringDialog newInstance(@NonNull final ArrayList<String> strings) {
        final SelectStringDialog instance = new SelectStringDialog();
        final Bundle args = new Bundle();
        args.putStringArrayList(KEY_STRING_LIST, strings);
        instance.setArguments(args);
        return instance;
    }

    private SelectStringListener mEventListener;

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        if (context instanceof SelectStringListener) {
            mEventListener = (SelectStringListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.string_select));
        final Bundle args = getArguments();
        final ArrayList<String> stringList = args.getStringArrayList(KEY_STRING_LIST);
        if (stringList == null) {
            dismiss();
            return builder.create();
        }
        final ListAdapter adapter = new StringListAdapter(getActivity(), stringList);
        builder.setAdapter(adapter, (dialog, which) -> {
            if (mEventListener != null) {
                mEventListener.onSelectString(stringList.get(which));
            }
        });
        return builder.create();
    }

    private static class StringListAdapter extends BaseListAdapter<String> {
        public StringListAdapter(
                @NonNull final Context context,
                @NonNull final Collection<? extends String> collection) {
            super(context, collection);
        }

        @Override
        public View getView(
                final int position,
                @NonNull final View convertView,
                @NonNull final ViewGroup parent) {
            final View view = inflateView(R.layout.list_item_string, convertView, parent);
            final TextView text = view.findViewById(R.id.textView);
            text.setText(getItem(position));
            return text;
        }
    }
}
