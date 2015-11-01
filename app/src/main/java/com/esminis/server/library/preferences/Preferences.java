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
package com.esminis.server.library.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.esminis.server.php.BuildConfig;
import com.esminis.server.php.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class Preferences {

	final static public String DOCUMENT_ROOT = "documentRoot";
	final static public String ADDRESS = "address";
	final static public String PORT = "port";
	final static public String START_ON_BOOT = "startOnBoot";
	final static public String KEEP_RUNNING = "keepRunning";
	final static public String SHOW_NOTIFICATION_SERVER = "showNotificationServer";
	final static public String SERVER_STARTED = "serverStarted";
	final static public String BUILD = "installedPhpBuild";
	final static public String INDEX_PHP_ROUTER = "indexPhpRouter";

	private PreferencesBackend backend = null;
	private final Object lock = new Object();

	@Inject
	public Preferences() {}

	public void set(Context context, String name, boolean value) {
		set(context, name, value ? "1" : "");
	}

	public void setBooleans(Context context, Map<String, Boolean> values) {
		final Map<String, String> valuesNew = new HashMap<>();
		final Set<String> keys = values.keySet();
		for (String key : keys) {
			valuesNew.put(key, values.get(key).equals(true) ? "1" : "");
		}
		setStrings(context, valuesNew);
	}

	public void set(Context context, String name, String value) {
		Map<String, String> values = new HashMap<>();
		values.put(name, value);
		setStrings(context, values);
	}

	private void setStrings(final Context context, final Map<String, String> values) {
		if (!values.isEmpty()) {
			getPreferences(context).put(values);
		}
	}

	public String getString(Context context, String name) {
		return getPreferences(context).get(name, "");
	}

	public boolean getBoolean(Context context, String name) {
		return "1".equals(getString(context, name));
	}

	public Map<String, String> getStrings(Context context) {
		return getPreferences(context).get();
	}

	public Map<String, Boolean> getBooleans(Context context) {
		final Map<String, String> values = getStrings(context);
		final Map<String, Boolean> result = new HashMap<>();
		final Set<String> keys = values.keySet();
		for (String key : keys) {
			result.put(key, "1".equals(values.get(key)));
		}
		return result;
	}

	public boolean contains(Context context, String name) {
		return getPreferences(context).contains(name);
	}

	private PreferencesBackend getPreferences(Context context) {
		synchronized (lock) {
			if (backend == null) {
				migrate(backend = new PreferencesBackendContentProvider(context), context);
			}
		}
		return backend;
	}

	private void migrate(PreferencesBackend preferences, Context context) {
		final SharedPreferences sharedPreferences = PreferenceManager
			.getDefaultSharedPreferences(context);
		final String keyMigrate = "__migrated__";
		if (sharedPreferences.contains(keyMigrate)) {
			return;
		}
		final Map<String, ?> map = sharedPreferences.getAll();
		final Set<String> keys = map.keySet();
		final Map<String, String> mapNew = new HashMap<>();
		for (String key : keys) {
			Object object = map.get(key);
			if (object instanceof Boolean) {
				mapNew.put(key, object.equals(true) ? "1" : "");
			} else {
				mapNew.put(key, (String)object);
			}
		}
		preferences.put(mapNew);
		sharedPreferences.edit().putInt(keyMigrate, BuildConfig.VERSION_CODE).apply();
	}

	public boolean getIsSameBuild(Context context) {
		return getString(context, Preferences.BUILD).equals(getBuild(context));
	}

	public String getBuild(Context context) {
		String build = context.getString(R.string.build);
		return context.getString(R.string.version) +
			(build.isEmpty() || build.equals("0") ? "" : "_" + build);
	}

}
