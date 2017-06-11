/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

/**
 * 起動後から表示されるActivity。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class MainActivity extends AppCompatActivity
        implements SelectThemeDialog.SelectThemeListener, SelectStringDialog.SelectStringListener,
        EditStringDialog.ConfirmStringListener {
    private ArrayList<Theme> mThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);
        initPreferences();
        makeThemes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 画面の向き設定を読みだして設定
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final String value = pref.getString(Settings.SCREEN_ORIENTATION.name(), null);
        int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if (value != null) {
            try {
                orientation = Integer.parseInt(value);
            } catch (final NumberFormatException e) {
                e.printStackTrace();
            }
        }
        //noinspection WrongConstant
        setRequestedOrientation(orientation);
    }

    /**
     * テーマの選択肢を作成。
     */
    private void makeThemes() {
        mThemes = new ArrayList<>();
        mThemes.add(new Theme(getString(R.string.theme_white_black), Color.WHITE, Color.BLACK));
        mThemes.add(new Theme(getString(R.string.theme_black_white), Color.BLACK, Color.WHITE));
        mThemes.add(new Theme(getString(R.string.theme_black_yellow), Color.BLACK, Color.YELLOW));
        mThemes.add(new Theme(getString(R.string.theme_black_green), Color.BLACK, Color.GREEN));
    }

    /**
     * アプリ設定の初期化。
     */
    private void initPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
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
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_theme:
                showThemeDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * テーマ設定のダイアログを起動。
     */
    private void showThemeDialog() {
        SelectThemeDialog.newInstance(mThemes).show(getSupportFragmentManager(), "");
    }

    /**
     * DefaultSharedPreferencesを返す。
     *
     * @return DefaultSharedPreferences
     */
    private SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onSelectTheme(Theme theme) {
        // 設定を保存する。
        getDefaultSharedPreferences().edit()
                .putInt(Settings.KEY_BACKGROUND.name(), theme.getBackgroundColor())
                .putInt(Settings.KEY_FOREGROUND.name(), theme.getForegroundColor())
                .apply();
        getMainFragment().applyTheme();
    }

    @Override
    public void onSelectString(String string) {
        getMainFragment().setText(string);
        // 継続して
        if (getDefaultSharedPreferences().getBoolean(Settings.LIST_EDIT.name(), false)) {
            getMainFragment().startEdit();
        }
    }

    @Override
    public void onConfirmString(String string) {
        getMainFragment().setText(string);
    }

    /**
     * MainFragmentを返す。
     *
     * @return MainFragment
     */
    private MainFragment getMainFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment instanceof MainFragment) {
            return (MainFragment) fragment;
        }
        return null;
    }
}
