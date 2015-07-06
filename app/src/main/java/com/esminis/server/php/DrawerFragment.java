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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.install.InstallToDocumentRoot;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.view.CheckboxRight;
import com.squareup.otto.Bus;

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
	protected ActivityHelper activityHelper;

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
		preference = new CheckBoxPreference(context);
		preference.setTitle(R.string.show_notification_server);
		preference.setDefaultValue(!preferences.getBoolean(context, Preferences.KEEP_RUNNING));
		preference.setSummary(R.string.show_notification_server_summary);
		preference.setKey(Preferences.SHOW_NOTIFICATION_SERVER);
		screen.addPreference(preference);
		requestStatusOnChange(preference);
		Preference preferenceInstall = new Preference(context);
		preferenceInstall.setTitle(R.string.reinstall_files);
		preferenceInstall.setSummary(R.string.reinstall_files_summary);
		preferenceInstall.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
						public void onNext(Void aVoid) {}
					}
				);
				return false;
			}
		});
		screen.addPreference(preferenceInstall);
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
		for (int i = 0; i < preferencesModules.getPreferenceCount(); i++) {
			CheckBoxPreference preference = (CheckBoxPreference)preferencesModules.getPreference(i);
			if (!getIsForBuiltIn(preference)) {
				preference.setChecked(selected);
			}
		}
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
		if (
			Preferences.SHOW_NOTIFICATION_SERVER.equals(name) && value &&
			preferences.getBoolean(context, Preferences.KEEP_RUNNING)
		) {
			((CheckBoxPreference)findPreference(Preferences.KEEP_RUNNING)).setChecked(false);
			((CheckBoxPreference)findPreference(Preferences.START_ON_BOOT)).setChecked(false);
			php.requestRestartIfRunning();
		} else if (
			(
				preferences.getBoolean(context, Preferences.KEEP_RUNNING) ||
				(Preferences.KEEP_RUNNING.equals(name) && value)
			) && preferences.getBoolean(context, Preferences.SHOW_NOTIFICATION_SERVER)
		) {
			((CheckBoxPreference)findPreference(Preferences.SHOW_NOTIFICATION_SERVER)).setChecked(false);
			php.requestStatus();
		}
	}

	private void requestStatusOnChange(Preference preference) {
		if (preference == null) {
			return;
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
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				resetSelectAll();
				return false;
			}
		});
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
