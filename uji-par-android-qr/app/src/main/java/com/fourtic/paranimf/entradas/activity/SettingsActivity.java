package com.fourtic.paranimf.entradas.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import com.fourtic.paranimf.entradas.R;
import com.fourtic.paranimf.entradas.activity.base.BasePreferenceActivity;
import com.fourtic.paranimf.entradas.rest.RestService;
import com.google.inject.Inject;

public class SettingsActivity extends BasePreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static final String PREF_HOST = "pref_host";
	public static final String PREF_PORT = "pref_port";
	public static final String PREF_APIKEY = "pref_apikey";
	public static final String PREF_EXT_SCAN = "pref_ext_scan";

	@Inject
	private RestService rest;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		setTitle(R.string.action_settings);

		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		rest.setURLFromPreferences(this);
		rest.setAPIKeyFromPreferences(this);
		rest.setLectorExternoFromPreferences(this);

		updatePrefSummary(findPreference(key));
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			p.setSummary(listPref.getEntry());
		}
		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			String text = editTextPref.getText();
			if (text != null && text.length() > 0)
				p.setSummary(editTextPref.getText());
			else
			{
				int resourceId = getResources().getIdentifier("@string/" + p.getKey() + "_summ", "strings", this.getPackageName());
				if (resourceId > 0)
					p.setSummary(getResources().getString(resourceId));
			}
		}
	}
}
