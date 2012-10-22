/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class MiscSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String SENSE4_RECENT_APPS_PREF = "pref_interface_sense4_recent_apps";

    private static final String KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";

    private static final String KEY_KILL_APP_LONGPRESS_TIMEOUT = "kill_app_longpress_timeout";

    private static final String KEY_HIGH_END_GFX = "high_end_gfx";

    private static final String KEY_HIGH_END_GFX_DISABLED = "disable_high_end_gfx";

    private CheckBoxPreference mSense4RecentApps;

    private CheckBoxPreference mKillAppLongpressBack;

    private CheckBoxPreference mHighEndGfx;

    private CheckBoxPreference mDisableHighEndGfx;

    private ListPreference mKillAppLongpressTimeout;



    private ContentResolver mContentResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.misc_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        mContentResolver = getActivity().getApplicationContext().getContentResolver();


	mSense4RecentApps = (CheckBoxPreference) findPreference(SENSE4_RECENT_APPS_PREF); 
        mSense4RecentApps.setChecked((Settings.System.getInt(getContentResolver(),
                        Settings.System.SENSE4_RECENT_APPS, 0)) == 1);
        mKillAppLongpressBack = (CheckBoxPreference) findPreference(KILL_APP_LONGPRESS_BACK);
        mKillAppLongpressTimeout = (ListPreference) findPreference(KEY_KILL_APP_LONGPRESS_TIMEOUT);
        mKillAppLongpressTimeout.setOnPreferenceChangeListener(this);
        int statusKillAppLongpressTimeout = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                 Settings.System.KILL_APP_LONGPRESS_TIMEOUT, 1500);
        mKillAppLongpressTimeout.setValue(String.valueOf(statusKillAppLongpressTimeout));
        mKillAppLongpressTimeout.setSummary(mKillAppLongpressTimeout.getEntry());
            boolean isHighEndGfx = ActivityManager.isHighEndGfx(getActivity().getWindowManager()
                                                                .getDefaultDisplay());
            mHighEndGfx = (CheckBoxPreference) findPreference(KEY_HIGH_END_GFX);
            mDisableHighEndGfx = (CheckBoxPreference) findPreference(KEY_HIGH_END_GFX_DISABLED);
            if(isHighEndGfx) {
                getPreferenceScreen().removePreference(mHighEndGfx);
                mDisableHighEndGfx.setChecked((Settings.System.getInt(getContentResolver(),
                                                               Settings.System.HIGH_END_GFX_DISABLED, 0) == 1));
            } else {
                getPreferenceScreen().removePreference(mDisableHighEndGfx);
                mHighEndGfx.setChecked((Settings.System.getInt(getContentResolver(),
                                                               Settings.System.HIGH_END_GFX_ENABLED, 0) == 1));
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateKillAppLongpressBackOptions();
    }

    private void writeKillAppLongpressBackOptions() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.KILL_APP_LONGPRESS_BACK,
                mKillAppLongpressBack.isChecked() ? 1 : 0);
    }

    private void updateKillAppLongpressBackOptions() {
        mKillAppLongpressBack.setChecked(Settings.System.getInt(
            getActivity().getContentResolver(), Settings.System.KILL_APP_LONGPRESS_BACK, 0) != 0);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mKillAppLongpressTimeout) {
            int statusKillAppLongpressTimeout = Integer.valueOf((String) objValue);
            int index = mKillAppLongpressTimeout.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.KILL_APP_LONGPRESS_TIMEOUT, statusKillAppLongpressTimeout);
            mKillAppLongpressTimeout.setSummary(mKillAppLongpressTimeout.getEntries()[index]);
            return true;
        }

        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
	if (preference == mSense4RecentApps) {
            value = mSense4RecentApps.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.SENSE4_RECENT_APPS,
                    value ? 1 : 0);
        } else if (preference == mKillAppLongpressBack) {
            writeKillAppLongpressBackOptions();
        } else if (preference == mHighEndGfx) {
            Settings.System.putInt(getContentResolver(),
                                   Settings.System.HIGH_END_GFX_ENABLED, mHighEndGfx.isChecked() ? 1 : 0);
        } else if (preference == mDisableHighEndGfx) {
            Settings.System.putInt(getContentResolver(),
                                   Settings.System.HIGH_END_GFX_DISABLED, mDisableHighEndGfx.isChecked() ? 1 : 0);
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }
}
