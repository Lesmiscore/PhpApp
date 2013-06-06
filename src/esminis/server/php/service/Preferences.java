package esminis.server.php.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

	static public String DOCUMENT_ROOT = "documentRoot";
	
	static public String ADDRESS = "address";
	
	static public String PORT = "port";
	
	static public String START_ON_BOOT = "startOnBoot";
	
	private SharedPreferences preferences = null;
	
	public Preferences(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public void set(String name, boolean value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}
	
	public void set(String name, String value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}
	
	public String getString(String name) {
		return preferences.getString(name, "");
	}
	
	public boolean getBoolean(String name) {
		return preferences.getBoolean(name, false);
	}
	
	public boolean contains(String name) {
		return preferences.contains(name);
	}
	
}
