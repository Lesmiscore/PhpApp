package com.esminis.server.php.model.manager;

import android.content.Context;

import com.esminis.server.php.R;

import java.util.ArrayList;
import java.util.List;

public class Preferences extends com.esminis.model.manager.Preferences {

	final static public String DOCUMENT_ROOT = "documentRoot";
	final static public String ADDRESS = "address";
	final static public String PORT = "port";
	final static public String START_ON_BOOT = "startOnBoot";
	final static public String KEEP_RUNNING = "keepRunning";
	final static public String PHP_BUILD = "installedPhpBuild";

	public String getPhpBuild(Context context) {
		String build = context.getString(R.string.php_build);
		return context.getString(R.string.php_version) +
			(build.isEmpty() || build.equals("0") ? "" : build);
	}

	public boolean getIsSameBuild(Context context) {
		return getString(context, Preferences.PHP_BUILD).equals(getPhpBuild(context));
	}

	public String[] getEnabledModules(Context context) {
		List<String> modules = new ArrayList<String>();
		String[] list = context.getResources().getStringArray(R.array.modules);
		for (int i = 0; i < list.length; i += 3) {
			if (getBoolean(context, "module_" + list[i])) {
				modules.add(list[i]);
			}
		}
		return modules.toArray(new String[modules.size()]);
	}

}
