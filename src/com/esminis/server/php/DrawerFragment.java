package com.esminis.server.php;

import android.app.Dialog;
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
		if (screen == null) {
			return;
		}
		screen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Dialog dialog = ((PreferenceScreen) preference).getDialog();
				if (dialog != null) {
					dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
					dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
					View view = dialog.findViewById(android.R.id.home);
					if (view.getParent() instanceof View) {
						view = (View)view.getParent();
						if (view != null && view.getParent() instanceof View) {
							view = (View)view.getParent();
						}
					}
					if (view != null) {
						view.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
					}
				}
				return false;
			}
		});
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
				Php.getInstance(getActivity()).requestRestartIfRunning();
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
