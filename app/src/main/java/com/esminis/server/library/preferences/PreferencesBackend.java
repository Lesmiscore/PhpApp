package com.esminis.server.library.preferences;

import java.util.Map;

interface PreferencesBackend {

	String get(String name, String defaultVale);

	Map<String, String> get();

	void put(Map<String, String> values);

	boolean contains(String name);

}
