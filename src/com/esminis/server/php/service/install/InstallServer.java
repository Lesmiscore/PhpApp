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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import com.esminis.server.php.service.Network;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.service.Preferences;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class InstallServer extends AsyncTask<Context, Void, Void> {

	public interface OnInstallListener {
		
		public void OnInstallStart();
		
		public void OnInstallEnd(boolean success);
		
	}
	
	private OnInstallListener listener = null;
	
	public InstallServer(OnInstallListener listener) {
		this.listener = listener;
	}
	
	public void installIfNeeded(Context context) {
		if (listener != null) {
			listener.OnInstallStart();
		}
		if (Php.getInstance(context).getPhp().isFile()) {
			if (listener != null) {
				listener.OnInstallEnd(true);
				listener = null;
			}
		} else {
			execute(context);
		}
	}
	
	@Override
	protected Void doInBackground(Context... arguments) {
		Context context = arguments[0];		
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
			new Install().fromAssetFile(php, "php", context);
			php.setExecutable(true);
		} catch (IOException ignored) {}
		return null;
	}

	@Override
	protected void onCancelled() {
		if (listener != null) {
			listener.OnInstallEnd(false);
			listener = null;
		}		
	}

	@Override
	protected void onPostExecute(Void result) {
		if (listener != null) {
			listener.OnInstallEnd(true);
			listener = null;
		}
	}
	
}
