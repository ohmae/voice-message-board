/**
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 */

package net.mm2d.android.vmb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

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
        void onConfirmString(String string);
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
    public static EditStringDialog newInstance(String editString) {
        final EditStringDialog instance = new EditStringDialog();
        final Bundle args = new Bundle();
        args.putString(KEY_STRING, editString);
        instance.setArguments(args);
        return instance;
    }

    private EditText mEditText;
    private ConfirmStringListener mEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ConfirmStringListener) {
            mEventListener = (ConfirmStringListener) activity;
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final String string = args.getString(KEY_STRING);
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_edit, null, false);
        mEditText = (EditText) view.findViewById(R.id.editText);
        mEditText.setText(string);
        mEditText.setSelection(string.length());
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final int keyCode = event == null ? -1 : event.getKeyCode();
                if (actionId == EditorInfo.IME_ACTION_DONE || keyCode == KeyEvent.KEYCODE_ENTER) {
                    inputText();
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.string_edit));
        builder.setView(mEditText);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputText();
            }
        });
        return builder.create();
    }

    private void inputText() {
        if (mEventListener != null) {
            mEventListener.onConfirmString(mEditText.getText().toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 編集中の文字列を保存
        final Bundle args = getArguments();
        args.putString(KEY_STRING, mEditText.getText().toString());
    }
}
