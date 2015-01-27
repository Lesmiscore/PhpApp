package com.esminis.server.php;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;

import javax.inject.Inject;

public class DrawerFragment extends PreferenceFragment {

	@Inject
	protected Preferences preferences;

	@Inject
	protected Php php;

	@Inject
	protected ActivityHelper activityHelper;

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
				initializeModulesDialog();
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

	private void initializeModulesDialog() {
		PreferenceScreen screen = (PreferenceScreen)findPreference("modules");
		if (screen == null) {
			return;
		}
		final Dialog dialog = screen.getDialog();
		if (dialog == null) {
			return;
		}
		ListView list = (ListView)dialog.findViewById(android.R.id.list);
		if (list == null || list.getParent() == null) {
			return;
		}
		((ViewGroup)list.getParent()).removeView(list);
		list.setPadding(0, 0, 0, 0);
		dialog.setContentView(R.layout.preference_modules);
		Toolbar toolbar = activityHelper.createToolbar(dialog);
		toolbar.setTitle(screen.getTitle());
		((ViewGroup)dialog.findViewById(R.id.content)).addView(
			list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((Application)getActivity().getApplication()).getObjectGraph().inject(this);
		if (savedInstanceState != null) {
			initializeModulesDialog();
		}
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
