/**
 * Copyright 2013 Tautvydas Andrikys
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
package com.esminis.server.php.service.install;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import com.esminis.server.php.service.Network;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.service.Preferences;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class InstallServer extends AsyncTask<Context, Void, Boolean> {

	public interface OnInstallListener {

		public void OnInstallNewVersionRequest(InstallServer installer);

		public void OnInstallEnd(boolean success);
		
	}
	
	private OnInstallListener listener = null;

	private boolean canStartInstall = false;

	static final private String PATH_ASSET_PHP = "php";
	
	public InstallServer(OnInstallListener listener) {
		this.listener = listener;
	}
	
	public void installIfNeeded(Context context) {
		File file = Php.getInstance(context).getPhp();
		if (file.isFile()) {
			try {
				if (file.length() != context.getAssets().open(PATH_ASSET_PHP).available()) {
					if (listener != null) {
						listener.OnInstallNewVersionRequest(this);
					}
				} else {
					installEnd(true);
				}
			} catch (IOException e) {
				installEnd(false);
			}
		} else {
			startInstall(context);
		}
	}

	public void continueInstall(Context context, boolean confirm) {
		if (confirm) {
			startInstall(context);
		} else {
			installEnd(true);
		}
	}

	private void startInstall(Context context) {
		executeOnExecutor(THREAD_POOL_EXECUTOR, context);
	}

	private void installEnd(boolean success) {
		if (listener != null) {
			listener.OnInstallEnd(success);
			listener = null;
		}
	}

	@Override
	protected Boolean doInBackground(Context... arguments) {
		Context context = arguments[0];
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() == null || !intent.getAction().equals(Php.INTENT_ACTION)) {
					return;
				}
				Bundle extras = intent.getExtras();
				if (extras == null || extras.containsKey("errorLine") || extras.getBoolean("running")) {
					return;
				}
				canStartInstall = true;
			}
		};
		context.registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		Php.getInstance(context).sendAction("stop");
		while (canStartInstall) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ignored) {}
		}
		context.unregisterReceiver(receiver);
		Preferences preferences = new Preferences(context);
		if (!preferences.contains(Preferences.DOCUMENT_ROOT)) {
			File file = new File(Environment.getExternalStorageDirectory(), "www");
			if (!file.isDirectory()) {
				if (file.mkdir() && file.isDirectory()) {
					try {
						Install install = new Install();
						install.fromAssetDirectory(file, "www", context);
						HashMap<String, String> variables = new HashMap<String, String>();
						File tempDirectory = new File(context.getExternalFilesDir(null), "tmp");
						if (!tempDirectory.isDirectory() && !tempDirectory.mkdir()) {
							tempDirectory = file;
						}
						variables.put("tempDirectory", tempDirectory.getAbsolutePath());
						variables.put("wwwDirectory", file.getAbsolutePath());
						install.preprocessFile(new File(file, "php.ini"), variables);
						install.preprocessFile(new File(file, "extensions.ini"), variables);
					} catch (IOException ignored) {}
				}
			}
			preferences.set(Preferences.DOCUMENT_ROOT, file.getAbsolutePath());
		}
		if (!preferences.contains(Preferences.PORT)) {
			preferences.set(Preferences.PORT, "8080");
		}
		if (!preferences.contains(Preferences.ADDRESS)) {			
			preferences.set(Preferences.ADDRESS, new Network().getNames().get(0));
		}
		File php = Php.getInstance(context).getPhp();
		try {
			if (!php.isFile() || php.delete()) {
				new Install().fromAssetFile(php, PATH_ASSET_PHP, context);
				if (!php.isFile() || (!php.canExecute() && !php.setExecutable(true))) {
					return false;
				}
			}	else {
				return false;
			}
		} catch (IOException ignored) {
			return false;
		}
		return true;
	}

	@Override
	protected void onCancelled() {
		installEnd(false);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		installEnd(result);
	}
	
}
