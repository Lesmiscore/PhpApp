package com.esminis.server.php.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.esminis.server.library.EventMessage;
import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.application.Application;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.widget.CheckboxRight;
import com.esminis.server.php.R;
import com.esminis.server.php.server.install.InstallToDocumentRoot;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Subscriber;

public class DrawerPhpFragment extends DrawerFragment {

	static private final String KEY_MODULES = "modules";
	static private final String PREFIX_MODULE = "module_";
	static private final String PREFIX_BUILT_IN = "builtin_";

	private CheckboxRight checkboxSelectAll = null;

	@Inject
	protected InstallToDocumentRoot installToDocumentRoot;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((Application)getActivity().getApplicationContext()).getObjectGraph().inject(this);
		if (savedInstanceState != null) {
			initializeModulesDialog();
		}
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
			list, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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
		serverControl.requestRestartIfRunning();
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

	private boolean getIsForBuiltIn(Preference preference) {
		return preference.getKey().startsWith(PREFIX_MODULE + PREFIX_BUILT_IN);
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
	protected Preference restartOnChange(Preference preference) {
		super.restartOnChange(preference);
		if (preference != null) {
			preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					resetSelectAll();
					return false;
				}
			});
		}
		return preference;
	}

	@Override
	protected void setupPreferences(PreferenceScreen screen, Context context) {
		final PreferenceScreen modules = getPreferenceManager().createPreferenceScreen(context);
		modules.setTitle(R.string.modules_title);
		modules.setSummary(R.string.modules_summary);
		modules.setKey(KEY_MODULES);
		screen.addPreference(modules);
		super.setupPreferences(screen, context);
		setupPreferencesModules(modules, context);
		screen.addPreference(
			restartOnChange(
				createPreferenceCheckbox(
					context, Preferences.INDEX_PHP_ROUTER, false,
					R.string.server_index_as_router_title, R.string.server_index_as_router
				)
			)
		);
		screen.addPreference(createPreferenceInstall(context));
	}
}
