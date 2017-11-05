/*
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.vmb;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * 設定画面。
 *
 * @author 大前良介(OHMAE Ryosuke)
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }
        addPreferencesFromResource(R.xml.pref_general);
        final PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_information);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_information);
        bindPreference(findPreference(Settings.SCREEN_ORIENTATION.name()));
        findPreference(Settings.PLAY_STORE.name())
                .setOnPreferenceClickListener(sPlayStoreClickListener);
        findPreference(Settings.PRIVACY_POLICY.name())
                .setOnPreferenceClickListener(sPrivacyPolicyClickListener);
        findPreference(Settings.VERSION_NUMBER.name())
                .setSummary(getVersionName(this));
    }

    @Override
    protected boolean isValidFragment(@NonNull final String fragmentName) {
        return true;
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    @Override
    public void onBuildHeaders(@NonNull final List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * 画面サイズからXLARGE以上かを判定
     *
     * @param context コンテキスト
     * @return XLARGE以上ならtrue
     */
    private static boolean isXLargeTablet(@NonNull final Context context) {
        final Configuration config = context.getResources().getConfiguration();
        final int layout = config.screenLayout;
        final int size = layout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return size >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * マルチペイン判定
     *
     * @param context コンテキスト
     * @return マルチペインにしない場合true
     */
    private static boolean isSimplePreferences(@NonNull final Context context) {
        return ALWAYS_SIMPLE_PREFS
                || !isXLargeTablet(context);
    }

    /**
     * バージョン情報を取得。
     *
     * @param context コンテキスト
     * @return バージョン文字列
     */
    @NonNull
    private static String getVersionName(@NonNull final Context context) {
        final PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            final PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (final NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 設定変更を検出してSummaryに反映するリスナー。
     */
    private static final OnPreferenceChangeListener sSummaryListener = (preference, value) -> {
        final String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) preference;
            final int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    };

    /**
     * Playストアへ飛ばすOnPreferenceClickListener。
     */
    private static final OnPreferenceClickListener sPlayStoreClickListener = preference -> {
        final Uri uri = Uri.parse("market://details?id=net.mm2d.android.vmb");
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final Context context = preference.getContext();
        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException ignored) {
        }
        return true;
    };

    /**
     * プライバシーポリシーへ飛ばすOnPreferenceClickListener。
     */
    private static final OnPreferenceClickListener sPrivacyPolicyClickListener = preference -> {
        final Uri uri = Uri.parse("https://github.com/ohmae/VoiceMessageBoard/blob/develop/PRIVACY-POLICY.md");
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final Context context = preference.getContext();
        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException ignored) {
        }
        return true;
    };

    /**
     * 設定結果を反映させるListenerとの接続。
     *
     * @param preference Preference
     */
    private static void bindPreference(@NonNull final Preference preference) {
        preference.setOnPreferenceChangeListener(sSummaryListener);
        final Context context = preference.getContext();
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final String value = sp.getString(preference.getKey(), "");
        sSummaryListener.onPreferenceChange(preference, value);
    }

    /**
     * 一般設定のFragment。
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            bindPreference(findPreference(Settings.SCREEN_ORIENTATION.name()));
        }
    }

    /**
     * 情報表示のFragment。
     */
    public static class InformationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_information);
            findPreference(Settings.PLAY_STORE.name())
                    .setOnPreferenceClickListener(sPlayStoreClickListener);
            findPreference(Settings.PRIVACY_POLICY.name())
                    .setOnPreferenceClickListener(sPrivacyPolicyClickListener);
            findPreference(Settings.VERSION_NUMBER.name())
                    .setSummary(getVersionName(getActivity()));
        }
    }
}
