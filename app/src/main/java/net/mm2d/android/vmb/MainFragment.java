/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * テキストを大きく表示する画面。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class MainFragment extends Fragment {
    private static final String TAG_FONT_SIZE = "TAG_FONT_SIZE";
    private static final String TAG_TEXT = "TAG_TEXT";
    private static final int REQUEST_CODE = 1;
    private float mFontSizeMin;
    private float mFontSizeMax;
    private float mFontSize;
    private View mRoot;
    private TextView mText;
    private Toolbar mToolbar;
    private GridDrawable mGridDrawable;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        view.findViewById(R.id.fab).setOnClickListener(v -> {
            startEdit();
        });
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mText = (TextView) view.findViewById(R.id.textView);
        mRoot = view.findViewById(R.id.root);
        mRoot.setOnClickListener(v -> startVoiceInput());
        mRoot.setOnLongClickListener(v -> {
            startEdit();
            return true;
        });
        mRoot.setOnTouchListener((v, event) -> {
            mGestureDetector.onTouchEvent(event);
            mScaleDetector.onTouchEvent(event);
            return true;
        });
        mGridDrawable = new GridDrawable(getActivity());
        mScaleDetector = new ScaleGestureDetector(getActivity(), new ScaleListener());
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        mFontSizeMin = getResources().getDimension(R.dimen.font_size_min);
        mFontSizeMax = getResources().getDimension(R.dimen.font_size_max);
        applyTheme();
        onRestoreInstanceState(savedInstanceState);
        return view;
    }

    /**
     * Bundleがあればそこから、なければ初期値をViewに設定する。
     *
     * @param savedInstanceState State
     */
    private void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // 画面幅に初期文字列が収まる大きさに調整
            final int width = getResources().getDisplayMetrics().widthPixels;
            final String initialText = mText.getText().toString();
            if (initialText.charAt(0) <= '\u007e') {
                // 半角
                mFontSize = (float) width / initialText.length() * 2;
            } else {
                // 全角
                mFontSize = (float) width / initialText.length();
            }
            mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFontSize);
        } else {
            // テキストとフォントサイズを復元
            mFontSize = savedInstanceState.getFloat(TAG_FONT_SIZE);
            final String text = savedInstanceState.getString(TAG_TEXT);
            mText.setText(text);
            mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFontSize);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // テキストとフォントサイズを保存
        outState.putFloat(TAG_FONT_SIZE, mFontSize);
        outState.putString(TAG_TEXT, mText.getText().toString());
    }

    /**
     * 音声入力開始。
     */
    private void startVoiceInput() {
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == MainFragment.REQUEST_CODE) && (resultCode == Activity.RESULT_OK)) {
            // 音声入力の結果を反映
            final ArrayList<String> results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results.size() > 1 &&
                    getDefaultSharedPreferences().getBoolean(Settings.CANDIDATE_LIST.name(), false)) {
                SelectStringDialog.newInstance(results).show(getFragmentManager(), "");
            } else {
                setText(results.get(0));
            }
        }
    }

    /**
     * テキストの編集を開始する。
     */
    public void startEdit() {
        final String string = mText.getText().toString();
        EditStringDialog.newInstance(string).show(getFragmentManager(), "");
    }

    /**
     * 文字列を設定する。
     *
     * Activityからも設定できるようにpublic
     * onSaveInstanceStateでここで設定した文字列は保持される。
     *
     * @param string 表示する文字列
     */
    public void setText(String string) {
        mText.setText(string);
    }

    /**
     * DefaultSharedPreferencesを返す。
     *
     * @return DefaultSharedPreferences
     */
    private SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    /**
     * Preferenceを読みだして、テーマを設定する。
     */
    public void applyTheme() {
        final SharedPreferences pref = getDefaultSharedPreferences();
        final int bg = pref.getInt(Settings.KEY_BACKGROUND.name(), Color.WHITE);
        final int fg = pref.getInt(Settings.KEY_FOREGROUND.name(), Color.BLACK);
        setTheme(bg, fg);
    }

    /**
     * テーマ設定。
     *
     * 背景色と文字色を設定するのみ。
     *
     * @param background 背景色
     * @param foreground 文字色
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setTheme(int background, int foreground) {
        mGridDrawable.setColor(background);
        ViewCompat.setBackground(mRoot, mGridDrawable);
        mRoot.invalidate();
        mText.setTextColor(foreground);
        final Drawable icon = mToolbar.getOverflowIcon();
        if (icon != null) {
            DrawableCompat.setTint(icon, getIconColor(background));
        }
    }

    @ColorInt
    private int getIconColor(@ColorInt int background) {
        if (getBrightness(background) < 128) {
            return Color.WHITE;
        }
        return Color.BLACK;
    }

    private int getBrightness(@ColorInt int color) {
        return getBrightness(Color.red(color), Color.green(color), Color.blue(color));
    }
    private int getBrightness(int r, int g, int b) {
        return clamp((int) (r * 0.299 + g * 0.587 + b * 0.114 + 0.5f), 0, 255);
    }

    /**
     * タッチイベントをClickとLongClickに振り分ける。
     *
     * 直接OnClickを使うとピンチ時に反応するため、
     * GestureDetectorを利用する。
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mRoot.performClick();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (getDefaultSharedPreferences().getBoolean(Settings.LONG_TAP_EDIT.name(), false)) {
                mRoot.performLongClick();
            }
        }
    }

    /**
     * ピンチ操作でフォントサイズを調整する。
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final float factor = detector.getScaleFactor();
            mFontSize = clamp(mFontSize * factor, mFontSizeMin, mFontSizeMax);
            mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFontSize);
            return true;
        }
    }

    /**
     * min以下はmin、max以上はmaxに飽和させる
     *
     * @param value 値
     * @param min   最小値
     * @param max   最大値
     * @return 飽和させた値
     */
    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * min以下はmin、max以上はmaxに飽和させる
     *
     * @param value 値
     * @param min   最小値
     * @param max   最大値
     * @return 飽和させた値
     */
    private static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }
}
