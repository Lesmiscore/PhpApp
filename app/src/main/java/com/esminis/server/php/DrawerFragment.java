/**
 * Copyright 2015 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esminis.server.php;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.esminis.server.php.helper.MainActivityHelper;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.background.install.InstallToDocumentRoot;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.view.CheckboxRight;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Subscriber;

public class DrawerFragment extends PreferenceFragment {

	static private final String KEY_MODULES = "modules";
	static private final String PREFIX_MODULE = "module_";
	static private final String PREFIX_BUILT_IN = "builtin_";

	@Inject
	protected Preferences preferences;

	@Inject
	protected Php php;

	@Inject
	protected MainActivityHelper activityHelper;

	@Inject
	protected InstallToDocumentRoot installToDocumentRoot;

	@Inject
	protected Bus bus;

	private CheckboxRight checkboxSelectAll = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context context = getActivity();
		((Application)context.getApplicationContext()).getObjectGraph().inject(this);
		PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
		setPreferenceScreen(screen);
		context = new ContextThemeWrapper(getActivity(), R.style.Preference);
		setupPreferences(screen, context);
	}

	private void setupPreferences(PreferenceScreen screen, Context context) {
		final PreferenceScreen modules = getPreferenceManager().createPreferenceScreen(context);
		modules.setTitle(R.string.modules_title);
		modules.setSummary(R.string.modules_summary);
		modules.setKey(KEY_MODULES);
		screen.addPreference(modules);
		setupPreferencesModules(modules, context);
		final CheckBoxPreference preference = createPreferenceCheckbox(
			context, Preferences.START_ON_BOOT, false,
			R.string.server_start_on_boot_title, R.string.server_start_on_boot_summary
		);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				onPreferenceChanged(preference, newValue);
				return true;
			}
		});
		screen.addPreference(preference);
		screen.addPreference(
			restartOnChange(
				createPreferenceCheckbox(
					context, Preferences.KEEP_RUNNING, false,
					R.string.server_keep_running_title, R.string.server_keep_running
				)
			)
		);
		screen.addPreference(
			restartOnChange(
				createPreferenceCheckbox(
					context, Preferences.INDEX_PHP_ROUTER, false,
					R.string.server_index_as_router_title, R.string.server_index_as_router
				)
			)
		);
		screen.addPreference(
			requestStatusOnChange(
				createPreferenceCheckbox(
					context, Preferences.SHOW_NOTIFICATION_SERVER,
					!preferences.getBoolean(context, Preferences.KEEP_RUNNING),
					R.string.show_notification_server, R.string.show_notification_server_summary
				)
			)
		);
		screen.addPreference(createPreferenceInstall(context));
		setupPreferencesValues(screen, context);
	}

	private CheckBoxPreference createPreferenceCheckbox(
		Context context, String key, boolean defaultValue, @StringRes int title, @StringRes int summary
	) {
		final CheckBoxPreference preference = new CheckBoxPreference(context);
		preference.setTitle(title);
		preference.setDefaultValue(defaultValue);
		preference.setSummary(summary);
		preference.setKey(key);
		return preference;
	}

	private Preference createPreferenceInstall(Context context) {
		final Preference preference = new Preference(context);
		preference.setTitle(R.string.reinstall_files);
		preference.setSummary(R.string.reinstall_files_summary);
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				installToDocumentRoot.installOnBackground(preference.getContext()).subscribe(
					new Subscriber<Void>() {
						@Override
						public void onCompleted() {
							EventMessage.post(bus, R.string.reinstall_files_complete);
						}

						@Override
						public void onError(Throwable e) {
							EventMessage.post(bus, e);
						}

						@Override
						public void onNext(Void aVoid) {
						}
					}
				);
				return false;
			}
		});
		return preference;
	}

	private void setupPreferencesValues(PreferenceScreen screen, Context context) {
		final Map<String, Boolean> values = preferences.getBooleans(context);
		final Map<String, Boolean> valuesSave = new HashMap<>();
		for (int i = 0; i < screen.getPreferenceCount(); i++) {
			Preference preference = screen.getPreference(i);
			if (preference instanceof CheckBoxPreference) {
				if (values.containsKey(preference.getKey())) {
					((CheckBoxPreference)preference).setChecked(values.get(preference.getKey()));
				} else {
					valuesSave.put(preference.getKey(), ((CheckBoxPreference)preference).isChecked());
				}
			}
		}
		preferences.setBooleans(context, valuesSave);
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
				PREFIX_BUILT_IN + i, resources.getString(R.string.modules_title_builtin, list[i]),
				list[i + 1], screen, context, false
			);
		}
		setupPreferencesValues(screen, context);
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
		final Toolbar toolbar = activityHelper.createToolbar(dialog, getActivity());
		toolbar.setPadding(
			toolbar.getPaddingLeft(), toolbar.getPaddingTop(), 0, toolbar.getPaddingBottom()
		);
		toolbar.setLogo(null);
		toolbar.setTitle(screen.getTitle());
		initializeToolbarMenu(toolbar);
		((ViewGroup)dialog.findViewById(R.id.content)).addView(
			list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		);
	}

	private void setModulesSelected(boolean selected) {
		PreferenceScreen preferencesModules =
			(PreferenceScreen)getPreferenceManager().findPreference(KEY_MODULES);
		final Map<String, Boolean> values = new HashMap<>();
		for (int i = 0; i < preferencesModules.getPreferenceCount(); i++) {
			CheckBoxPreference preference = (CheckBoxPreference)preferencesModules.getPreference(i);
			if (!getIsForBuiltIn(preference)) {
				preference.setChecked(selected);
				values.put(preference.getKey(), selected);
			}
		}
		preferences.setBooleans(getActivity(), values);
		php.requestRestartIfRunning();
		resetSelectAll();
	}

	private void initializeToolbarMenu(Toolbar toolbar) {
		checkboxSelectAll = new CheckboxRight(toolbar.getContext());
		toolbar.getMenu().add("").setActionView(checkboxSelectAll)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		resetSelectAll();
	}

	private void resetSelectAll() {
		if (checkboxSelectAll == null) {
			return;
		}
		PreferenceScreen preferencesModules =
			(PreferenceScreen)getPreferenceManager().findPreference(KEY_MODULES);
		boolean allChecked = true;
		for (int i = 0; i < preferencesModules.getPreferenceCount(); i++) {
			CheckBoxPreference preference = (CheckBoxPreference)preferencesModules.getPreference(i);
			if (!getIsForBuiltIn(preference) && !preference.isChecked()) {
				allChecked = false;
				break;
			}
		}
		checkboxSelectAll.setOnCheckedChangeListener(null);
		checkboxSelectAll.setTitle(allChecked ? R.string.disable_all : R.string.enable_all);
		checkboxSelectAll.setChecked(allChecked);
		checkboxSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
				setModulesSelected(checked);
			}
		});
	}

	private boolean getIsForBuiltIn(Preference preference) {
		return preference.getKey().startsWith(PREFIX_MODULE + PREFIX_BUILT_IN);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
			view.setBackgroundColor(ContextCompat.getColor(getActivity(), attribute.resourceId));
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
		final String name = preference.getKey();
		final boolean value = (Boolean)newValueObject;
		preferences.set(context, name, value);
		if (
			Preferences.KEEP_RUNNING.equals(name) && !value &&
			preferences.getBoolean(context, Preferences.START_ON_BOOT)
		) {
			setPreferenceValue(Preferences.START_ON_BOOT, false);
		} else if (
			Preferences.START_ON_BOOT.equals(name) && value &&
			!preferences.getBoolean(context, Preferences.KEEP_RUNNING)
		) {
			setPreferenceValue(Preferences.KEEP_RUNNING, true);
		}
		if (
			Preferences.SHOW_NOTIFICATION_SERVER.equals(name) && value &&
			preferences.getBoolean(context, Preferences.KEEP_RUNNING)
		) {
			setPreferenceValue(Preferences.START_ON_BOOT, false);
			setPreferenceValue(Preferences.KEEP_RUNNING, false);
			php.requestRestartIfRunning();
		} else if (
			(
				preferences.getBoolean(context, Preferences.KEEP_RUNNING) ||
				(Preferences.KEEP_RUNNING.equals(name) && value)
			) && preferences.getBoolean(context, Preferences.SHOW_NOTIFICATION_SERVER)
		) {
			setPreferenceValue(Preferences.SHOW_NOTIFICATION_SERVER, false);
			php.requestStatus();
		}
	}

	private void setPreferenceValue(String name, boolean checked) {
		preferences.set(getActivity(), name, checked);
		((CheckBoxPreference)findPreference(name)).setChecked(checked);
	}

	private Preference requestStatusOnChange(Preference preference) {
		if (preference == null) {
			return null;
		}
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				onPreferenceChanged(preference, newValue);
				return true;
			}
		});
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				php.requestStatus();
				return true;
			}
		});
		return preference;
	}

	private Preference restartOnChange(Preference preference) {
		if (preference == null) {
			return null;
		}
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				onPreferenceChanged(preference, newValue);
				php.requestRestartIfRunning();
				return true;
			}
		});
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				resetSelectAll();
				return false;
			}
		});
		return preference;
	}

	private void addPreference(
		String name, String title, String summary, PreferenceScreen screen, Context context,
		boolean enabled
	) {
		CheckBoxPreference preference = new CheckBoxPreference(context);
		preference.setKey(PREFIX_MODULE + name);
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
