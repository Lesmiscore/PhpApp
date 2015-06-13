/**
 * Copyright 2014 Tautvydas Andrikys
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

import android.app.Activity;
import android.os.AsyncTask;

import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InstallServer {

	public interface OnInstallListener {

		void OnInstallNewVersionRequest(InstallServer installer);

		void OnInstallEnd(boolean success);
		
	}

	@Inject
	protected Php php;

	@Inject
	protected Preferences preferences;

	@Inject
	protected Network network;

	private OnInstallListener listener = null;
	private InstallTask installTask = null;
	private final Object lock = new Object();

	public void installIfNeeded(OnInstallListener listener, Activity activity) {
		synchronized (lock) {
			this.listener = listener;
			if (installTask != null) {
				return;
			}
		}
		File file = php.getPhp();
		if (file.isFile()) {
			if (!preferences.getIsSameBuild(activity)) {
				if (listener != null) {
					listener.OnInstallNewVersionRequest(this);
				}
			} else {
				finish(true);
			}
		} else {
			start(activity);
		}
	}

	public void continueInstall(Activity activity, boolean confirm) {
		if (confirm) {
			start(activity);
		} else {
			finish(true);
		}
	}

	void start(Activity activity) {
		synchronized (lock) {
			if (installTask == null) {
				installTask = new InstallTask(php, this, preferences, network, activity);
				installTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
	}

	void finish(boolean success) {
		OnInstallListener listener;
		synchronized (lock) {
			installTask = null;
			listener = this.listener;
			this.listener = null;
		}
		if (listener != null) {
			listener.OnInstallEnd(success);
		}
	}
	
}
