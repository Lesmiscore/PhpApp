package com.esminis.server.php.model.manager;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.esminis.server.php.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Preferences extends com.esminis.model.manager.Preferences {

	final static public String DOCUMENT_ROOT = "documentRoot";
	final static public String ADDRESS = "address";
	final static public String PORT = "port";
	final static public String START_ON_BOOT = "startOnBoot";
	final static public String KEEP_RUNNING = "keepRunning";
	final static public String SHOW_NOTIFICATION_SERVER = "showNotificationServer";
	final static public String PHP_BUILD = "installedPhpBuild";

	public String getPhpBuild(Context context) {
		String build = context.getString(R.string.php_build);
		return context.getString(R.string.php_version) +
			(build.isEmpty() || build.equals("0") ? "" : "_" + build);
	}

	public boolean getIsSameBuild(Context context) {
		return getString(context, Preferences.PHP_BUILD).equals(getPhpBuild(context));
	}

	public String[] getEnabledModules(Context context) {
		List<String> modules = new ArrayList<>();
		String[] list = context.getResources().getStringArray(R.array.modules);
		for (int i = 0; i < list.length; i += 3) {
			if (getBoolean(context, "module_" + list[i])) {
				modules.add(list[i]);
			}
		}
		return modules.toArray(new String[modules.size()]);
	}

	private String[] getInstallModules(Context context) {
		List<String> modules = new ArrayList<>();
		String[] list = context.getResources().getStringArray(R.array.modules);
		for (int i = 0; i < list.length; i += 3) {
			String module = list[i];
			modules.add((module.startsWith("zend_") ? module.substring(5) : module) + ".so");
		}
		return modules.toArray(new String[modules.size()]);
	}

	public String[] getInstallPaths(Context context) {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, context.getResources().getStringArray(R.array.install_binaries));
		Collections.addAll(list, getInstallModules(context));
		String pathBinaries = "bin/" + (Build.CPU_ABI.toLowerCase().startsWith("x86") ? "x86" : "arm")
			+ "/";
		for (int i = 0; i < list.size(); i++) {
			list.set(i, pathBinaries + list.get(i));
		}
		Collections.addAll(list, context.getResources().getStringArray(R.array.install_files));
		return list.toArray(new String[list.size()]);
	}

	public File getDefaultDocumentRoot() {
		return new File(Environment.getExternalStorageDirectory(), "www");
	}

}
