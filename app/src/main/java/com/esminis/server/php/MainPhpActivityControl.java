package com.esminis.server.php;

import android.content.Context;

import com.esminis.server.php.model.manager.Preferences;

import javax.inject.Inject;

public class MainPhpActivityControl {

	@Inject
	protected Preferences preferences;

	public String getPort(Context context) {
		return preferences.getString(context, Preferences.PORT);
	}

	public void setPort(Context context, String port) {
		preferences.set(context, Preferences.PORT, port);
	}

	public String getAddress(Context context) {
		return preferences.getString(context, Preferences.ADDRESS);
	}

	public void setAddress(Context context, String address) {
		preferences.set(context, Preferences.ADDRESS, address);
	}

	public String getRootDirectory(Context context) {
		return preferences.getString(context, Preferences.DOCUMENT_ROOT);
	}

	public void setRootDirectory(Context context, String root) {
		preferences.set(context, Preferences.DOCUMENT_ROOT, root);
	}

	public String getMessageNewVersion(Context context) {
		return context.getString(
			R.string.server_install_new_version_question, preferences.getPhpBuild(context)
		);
	}

}
