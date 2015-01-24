/**
 * Copyright(C) 2014 大前良介(OHMAE Ryosuke)
 */

package net.mm2d.android.vmb;

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
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * @author 大前良介(OHMAE Ryosuke)
 */
public class SettingsActivity extends PreferenceActivity {
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
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
        Preference p;
        p = findPreference(Settings.PLAYSTORE.name());
        p.setOnPreferenceClickListener(sPlaystoreClickListener);
        p = findPreference(Settings.VERSION_NUMBER.name());
        p.setSummary(getVersionName(this));
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    private static boolean isXLargeTablet(Context context) {
        final Configuration config = context.getResources().getConfiguration();
        final int layout = config.screenLayout;
        final int size = layout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return size >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || !isXLargeTablet(context);
    }

    private static String getVersionName(Context context) {
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

    private static OnPreferenceChangeListener sSummaryListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            final String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                final ListPreference listPreference = (ListPreference) preference;
                final int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static OnPreferenceClickListener sPlaystoreClickListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            final Context context = preference.getContext();
            final Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
            return true;
        }
    };

    private static void bindPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(sSummaryListener);
        final Context context = preference.getContext();
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final String value = sp.getString(preference.getKey(), "");
        sSummaryListener.onPreferenceChange(preference, value);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            bindPreference(findPreference(Settings.SCREEN_ORIENTATION.name()));
        }
    }

    public static class InformationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_information);
            Preference p;
            p = findPreference(Settings.PLAYSTORE.name());
            p.setOnPreferenceClickListener(sPlaystoreClickListener);
            p = findPreference(Settings.VERSION_NUMBER.name());
            p.setSummary(getVersionName(getActivity()));
        }
    }
}
