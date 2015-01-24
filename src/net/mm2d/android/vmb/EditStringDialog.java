/**
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 */

package net.mm2d.android.vmb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final String string = args.getString(KEY_STRING);
        mEditText = new EditText(getActivity());
        mEditText.setText(string);
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getActivity().getResources().getDimension(R.dimen.edit_font_size));
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.string_edit));
        builder.setView(mEditText);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mEventListener != null) {
                    mEventListener.onConfirmString(mEditText.getText().toString());
                }
            }
        });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 編集中の文字列を保存
        final Bundle args = getArguments();
        args.putString(KEY_STRING, mEditText.getText().toString());
    }
}
