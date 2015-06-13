package com.esminis.server.php;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;

import javax.inject.Inject;

public class DrawerFragment extends PreferenceFragment {

	static private final String KEY_MODULES = "modules";

	@Inject
	protected Preferences preferences;

	@Inject
	protected Php php;

	@Inject
	protected ActivityHelper activityHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context context = getActivity();
		PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
		setPreferenceScreen(screen);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			context = new ContextThemeWrapper(getActivity(), R.style.Preference);
		}
		setupPreferences(screen, context);
	}

	private void setupPreferences(PreferenceScreen screen, Context context) {
		PreferenceScreen modules = getPreferenceManager().createPreferenceScreen(context);
		modules.setTitle(R.string.modules_title);
		modules.setSummary(R.string.modules_summary);
		modules.setKey(KEY_MODULES);
		screen.addPreference(modules);
		setupPreferencesModules(modules, context);
		CheckBoxPreference preference = new CheckBoxPreference(context);
		preference.setTitle(R.string.server_start_on_boot_title);
		preference.setDefaultValue(false);
		preference.setSummary(R.string.server_start_on_boot_summary);
		preference.setKey(Preferences.START_ON_BOOT);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				onPreferenceChanged(preference, newValue);
				return true;
			}
		});
		screen.addPreference(preference);
		preference = new CheckBoxPreference(context);
		preference.setTitle(R.string.server_keep_running_title);
		preference.setDefaultValue(false);
		preference.setSummary(R.string.server_keep_running);
		preference.setKey(Preferences.KEEP_RUNNING);
		screen.addPreference(preference);
		restartOnChange(preference);
	}

	private void setupPreferencesModules(PreferenceScreen screen, Context context) {
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
			addPreference(list[i], list[i + 1], list[i + 2], screen, context, true);
		}
		list = resources.getStringArray(R.array.modules_builtin);
		for (int i = 0; i < list.length; i += 2) {
			addPreference(
				"builtin_" + i, resources.getString(R.string.modules_title_builtin, list[i]), list[i + 1],
				screen, context, false
			);
		}
	}

	private void initializeModulesDialog() {
		PreferenceScreen screen = (PreferenceScreen)findPreference(KEY_MODULES);
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
		if (getView() == null) {
			return;
		}
		View view = getView().findViewById(android.R.id.list);
		if (view == null) {
			return;
		}
		TypedValue attribute = new TypedValue();
		getActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, attribute, true);
		if (attribute.resourceId > 0) {
			view.setBackgroundColor(getResources().getColor(attribute.resourceId));
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
		String name, String title, String summary, PreferenceScreen screen, Context context,
		boolean enabled
	) {
		CheckBoxPreference preference = new CheckBoxPreference(context);
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
			View viewList = view.findViewById(android.R.id.list);
			if (viewList != null) {
				viewList.setPadding(0, 0, 0, 0);
			}
		}
		return view;
	}

}
