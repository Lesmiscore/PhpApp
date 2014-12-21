package com.esminis.server.php;

import android.app.*;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;

import javax.inject.Inject;

public class DrawerFragment extends PreferenceFragment {

	@Inject
	protected Preferences preferences;

	@Inject
	protected Php php;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		restartOnChange(findPreference(Preferences.KEEP_RUNNING));
		Preference preference = findPreference(Preferences.START_ON_BOOT);
		if (preference != null) {
			preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					onPreferenceChanged(preference, newValue);
					return true;
				}
			});
		}
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((Application)getActivity().getApplication()).getObjectGraph().inject(this);
	}

	private void onPreferenceChanged(Preference preference, Object newValueObject) {
		if (preference == null || !(preference instanceof CheckBoxPreference)) {
			return;
		}
		Context context = getActivity();
		if (context == null) {
			return;
		}
		String name = preference.getKey();
		boolean value = (Boolean)newValueObject;
		if (
			Preferences.KEEP_RUNNING.equals(name) && !value &&
			preferences.getBoolean(context, Preferences.START_ON_BOOT)
		) {
			preferences.set(context, Preferences.START_ON_BOOT, false);
			((CheckBoxPreference)findPreference(Preferences.START_ON_BOOT)).setChecked(false);
		} else if (
			Preferences.START_ON_BOOT.equals(name) && value &&
			!preferences.getBoolean(context, Preferences.KEEP_RUNNING)
		) {
			preferences.set(context, Preferences.KEEP_RUNNING, true);
			((CheckBoxPreference)findPreference(Preferences.KEEP_RUNNING)).setChecked(true);
		}
	}

	private void restartOnChange(Preference preference) {
		if (preference == null) {
			return;
		}
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				onPreferenceChanged(preference, newValue);
				php.requestRestartIfRunning();
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
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
		View view = super.onCreateView(inflater, container, state);
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
