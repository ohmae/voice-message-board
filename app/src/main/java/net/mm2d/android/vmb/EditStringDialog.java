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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * 文字列編集を行うダイアログ。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class EditStringDialog extends DialogFragment {
    /**
     * 文字列を確定した時に呼ばれるリスナー
     *
     * 呼び出し元のActivityに実装して利用する。
     */
    public interface ConfirmStringListener {
        /**
         * 文字列が確定された。
         *
         * @param string 確定された文字列。
         */
        void onConfirmString(@NonNull String string);
    }

    /**
     * 選択文字列のkey
     */
    private static final String KEY_STRING = "KEY_STRING";

    /**
     * Dialogのインスタンスを作成。
     *
     * 表示する情報を引数で渡すため
     * コンストラクタではなく、
     * このstaticメソッドを利用する。
     *
     * @param editString 編集する元の文字列
     * @return 新規インスタンス
     */
    public static EditStringDialog newInstance(@NonNull final String editString) {
        final EditStringDialog instance = new EditStringDialog();
        final Bundle args = new Bundle();
        args.putString(KEY_STRING, editString);
        instance.setArguments(args);
        return instance;
    }

    private EditText mEditText;
    private ConfirmStringListener mEventListener;

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        if (context instanceof ConfirmStringListener) {
            mEventListener = (ConfirmStringListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final String string = args.getString(KEY_STRING);
        if (string == null) {
            dismiss();
            return new AlertDialog.Builder(getActivity())
                    .setTitle(getActivity().getString(R.string.string_edit))
                    .create();
        }
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_edit, null, false);
        mEditText = view.findViewById(R.id.editText);
        mEditText.setText(string);
        mEditText.setSelection(string.length());
        mEditText.setOnEditorActionListener((v, actionId, event) -> {
            final int keyCode = event == null ? -1 : event.getKeyCode();
            if (actionId == EditorInfo.IME_ACTION_DONE || keyCode == KeyEvent.KEYCODE_ENTER) {
                inputText();
                dismiss();
                return true;
            }
            return false;
        });
        return new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.string_edit))
                .setView(mEditText)
                .setPositiveButton(R.string.ok, (dialog, which) -> inputText())
                .create();
    }

    private void inputText() {
        if (mEventListener != null) {
            mEventListener.onConfirmString(mEditText.getText().toString());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        // 編集中の文字列を保存
        final Bundle args = getArguments();
        args.putString(KEY_STRING, mEditText.getText().toString());
    }
}
