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
package com.esminis.server.library.activity;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esminis.server.library.EventMessage;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.application.Application;
import com.esminis.server.php.R;
import com.esminis.server.php.service.server.install.InstallToDocumentRoot;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Subscriber;

public class DrawerFragment extends PreferenceFragment {

	@Inject
	protected Preferences preferences;

	@Inject
	protected ServerControl serverControl;

	@Inject
	protected MainActivityHelper activityHelper;

	@Inject
	protected InstallToDocumentRoot installToDocumentRoot;

	@Inject
	protected Bus bus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context context = getActivity();
		((Application)context.getApplicationContext()).getObjectGraph().inject(this);
		PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
		setPreferenceScreen(screen);
		context = new ContextThemeWrapper(getActivity(), R.style.Preference);
		setupPreferences(screen, context);
		setupPreferencesValues(screen, context);
	}

	protected void setupPreferences(PreferenceScreen screen, Context context) {
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
			requestStatusOnChange(
				createPreferenceCheckbox(
					context, Preferences.SHOW_NOTIFICATION_SERVER,
					!preferences.getBoolean(context, Preferences.KEEP_RUNNING),
					R.string.show_notification_server, R.string.show_notification_server_summary
				)
			)
		);
	}

	protected CheckBoxPreference createPreferenceCheckbox(
		Context context, String key, boolean defaultValue, @StringRes int title, @StringRes int summary
	) {
		final CheckBoxPreference preference = new CheckBoxPreference(context);
		preference.setTitle(title);
		preference.setDefaultValue(defaultValue);
		preference.setSummary(summary);
		preference.setKey(key);
		return preference;
	}

	protected Preference createPreferenceInstall(Context context) {
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

	protected void setupPreferencesValues(PreferenceScreen screen, Context context) {
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
			serverControl.requestRestartIfRunning();
		} else if (
			(
				preferences.getBoolean(context, Preferences.KEEP_RUNNING) ||
				(Preferences.KEEP_RUNNING.equals(name) && value)
			) && preferences.getBoolean(context, Preferences.SHOW_NOTIFICATION_SERVER)
		) {
			setPreferenceValue(Preferences.SHOW_NOTIFICATION_SERVER, false);
			serverControl.requestStatus();
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
				serverControl.requestStatus();
				return true;
			}
		});
		return preference;
	}

	protected Preference restartOnChange(Preference preference) {
		if (preference == null) {
			return null;
		}
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				onPreferenceChanged(preference, newValue);
				serverControl.requestRestartIfRunning();
				return true;
			}
		});
		return preference;
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
