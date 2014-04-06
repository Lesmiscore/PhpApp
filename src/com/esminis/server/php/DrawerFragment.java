package com.esminis.server.php;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;

public class DrawerFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		Preference preference = findPreference(Preferences.KEEP_RUNNING);
		if (preference != null) {
			preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Context context = getActivity();
					if (context != null) {
						context.registerReceiver(new BroadcastReceiver() {
							@Override
							public void onReceive(Context context, Intent intent) {
								if (intent.getAction() != null && intent.getAction().equals(Php.INTENT_ACTION)) {
									Bundle extras = intent.getExtras();
									if (
										extras != null && !extras.containsKey("errorLine") &&
										extras.getBoolean("running")
									) {
										Php.getInstance(context).requestRestart();
									}
									getActivity().unregisterReceiver(this);
								}
							}
						}, new IntentFilter(Php.INTENT_ACTION));
					}
					Php.getInstance(getActivity()).requestStatus();
					return true;
				}
			});
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		if(view != null) {
			view.setBackgroundColor(Color.WHITE);
			View viewList = view.findViewById(android.R.id.list);
			if (viewList != null) {
				viewList.setPadding(0, 0, 0, 0);
			}
		}
		return view;
	}
}
