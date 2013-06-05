package esminis.server.php.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Preferences {

	static public String DOCUMENT_ROOT = "documentRoot";
	
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
	
	public void initialize(Context context) {
		if (!preferences.contains(Preferences.DOCUMENT_ROOT)) {
			File file = new File(
				Environment.getExternalStorageDirectory().getAbsolutePath() + 
					File.separator + "www"
			);
			if (!file.isDirectory()) {
				file.mkdir();
				if (file.isDirectory()) {
					try {
						Install install = new Install();
						install.fromAssetDirectory(file, "www", context);						
						HashMap<String, String> variables = new HashMap<String, String>();
						File tempDirectory = new File(
							context.getExternalFilesDir(null).getAbsolutePath() + File.separator + 
							"tmp"
						);
						if (!tempDirectory.isDirectory()) {
							tempDirectory.mkdir();
						}
						variables.put("tempDirectory", tempDirectory.getAbsolutePath());
						install.preprocessFile(
							new File(file + File.separator + "php.ini"), variables
						);
					} catch (IOException ex) {}
				}
			}
			set(Preferences.DOCUMENT_ROOT, file.getAbsolutePath());
		}
		if (!preferences.contains(Preferences.PORT)) {
			set(Preferences.PORT, "8080");
		}
	}
	
}
