package com.esminis.server.php;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
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
		restartOnChange(findPreference(Preferences.KEEP_RUNNING));
		PreferenceScreen screen = (PreferenceScreen)findPreference("modules");
		Resources resources = getResources();
		String[] list = resources.getStringArray(R.array.modules);
		for (int i = 0; i < list.length; i += 3) {
			addPreference(list[i], list[i + 1], list[i + 2], screen, true);
		}
		list = resources.getStringArray(R.array.modules_builtin);
		for (int i = 0; i < list.length; i += 2) {
			addPreference(
				"builtin_" + i, resources.getString(R.string.modules_title_builtin, list[i]), list[i + 1],
				screen, false
			);
		}
	}

	private void restartOnChange(Preference preference) {
		if (preference == null) {
			return;
		}
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
									extras != null && !extras.containsKey("errorLine") && extras.getBoolean("running")
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

	private void addPreference(
		String name, String title, String summary, PreferenceScreen screen, boolean enabled
	) {
		CheckBoxPreference preference = new CheckBoxPreference(screen.getContext());
		preference.setKey("module_" + name);
		preference.setTitle(title);
		preference.setSummary(summary);
		preference.setDefaultValue(true);
		screen.addPreference(preference);
		if (enabled) {
			restartOnChange(preference);
		} else {
			preference.setEnabled(false);
			preference.setChecked(true);
			preference.setSelectable(false);
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
