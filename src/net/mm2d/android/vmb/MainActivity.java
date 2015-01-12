/**
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke) All Rights Reserved.
 */

package net.mm2d.android.vmb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 起動後から表示されるActivity。
 *
 * @author ryosuke
 */
public class MainActivity extends Activity implements SelectThemeDialog.SelectThemeListener {
    private static final String TAG_FONT_SIZE = "TAG_FONT_SIZE";
    private static final String TAG_TEXT = "TAG_TEXT";
    private static final String KEY_BACKGROUND = "KEY_BACKGROUND";
    private static final String KEY_FOREGROUND = "KEY_FOREGROUND";
    private static final int REQUEST_CODE = 1;
    private float mFontSizeMin;
    private float mFontSizeMax;
    private float mFontSize;
    private View mRoot;
    private TextView mText;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private ArrayList<Theme> mThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mText = (TextView) findViewById(R.id.textView1);
        mRoot = findViewById(R.id.root);
        mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });
        mRoot.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });
        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
        mGestureDetector = new GestureDetector(this, new GestureListener());
        mFontSizeMin = getResources().getDimension(R.dimen.font_size_min);
        mFontSizeMax = getResources().getDimension(R.dimen.font_size_max);
        // 画面幅に初期文字列が収まる大きさに調整
        final int width = getResources().getDisplayMetrics().widthPixels;
        final String initialText = mText.getText().toString();
        if (initialText.charAt(0) <= '\u007e') {
            // 半角
            mFontSize = width / initialText.length() * 2;
        } else {
            // 全角
            mFontSize = width / initialText.length();
        }
        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFontSize);
        restoreTheme();
        makeThemes();
    }

    private void restoreTheme() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final int bg = pref.getInt(KEY_BACKGROUND, Color.WHITE);
        final int fg = pref.getInt(KEY_FOREGROUND, Color.BLACK);
        setTheme(bg, fg);
    }

    private void makeThemes() {
        mThemes = new ArrayList<Theme>();
        mThemes.add(new Theme(getString(R.string.theme_white_black), Color.WHITE, Color.BLACK));
        mThemes.add(new Theme(getString(R.string.theme_black_white), Color.BLACK, Color.WHITE));
        mThemes.add(new Theme(getString(R.string.theme_black_yellow), Color.BLACK, Color.YELLOW));
        mThemes.add(new Theme(getString(R.string.theme_black_green), Color.BLACK, Color.GREEN));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // テキストとフォントサイズを復元
        mFontSize = savedInstanceState.getFloat(TAG_FONT_SIZE);
        final String text = savedInstanceState.getString(TAG_TEXT);
        mText.setText(text);
        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFontSize);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // テキストとフォントサイズを保存
        outState.putFloat(TAG_FONT_SIZE, mFontSize);
        outState.putString(TAG_TEXT, mText.getText().toString());
    }

    /**
     * 値が最小値よりも小さければ最小値、最大値より大きければ最大値にして返す。
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @return 丸められた値
     */
    private float clamp(float value, float min, float max) {
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        return value;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE) && (resultCode == RESULT_OK)) {
            // 音声入力の結果を反映
            final ArrayList<String> results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mText.setText(results.get(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                showThemeDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * テーマ設定のダイアログを起動。
     */
    private void showThemeDialog() {
        SelectThemeDialog
                .newInstance(getString(R.string.theme_select), mThemes)
                .show(getFragmentManager(), "");
    }

    /**
     * テーマ設定。
     *
     * 背景色と文字色を設定するのみ。
     *
     * @param background 背景色
     * @param foreground 文字色
     */
    private void setTheme(int background, int foreground) {
        mRoot.setBackgroundColor(background);
        mText.setTextColor(foreground);
    }

    @Override
    public void onSelectTheme(Theme theme) {
        setTheme(theme.getBackground(), theme.getForeground());
        // 設定を保存する。
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor edit = pref.edit();
        edit.putInt(KEY_BACKGROUND, theme.getBackground());
        edit.putInt(KEY_FOREGROUND, theme.getForeground());
        edit.commit();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mRoot.performClick();
            return true;
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

}
