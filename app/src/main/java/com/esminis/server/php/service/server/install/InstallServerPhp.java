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
package com.esminis.server.php.service.server.install;

import android.app.Activity;
import android.os.AsyncTask;

import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.php.MainActivity;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.library.service.server.install.InstallServer;
import com.esminis.server.library.service.server.install.OnInstallServerListener;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InstallServerPhp implements InstallServer {

	@Inject
	protected ServerControl serverControl;

	@Inject
	protected Preferences preferences;

	@Inject
	protected Network network;

	private OnInstallServerListener listener = null;
	private InstallTaskLocal installTask = null;
	private final Object lock = new Object();

	public void install(MainActivity activity) {
		synchronized (lock) {
			this.listener = activity;
			if (installTask != null) {
				return;
			}
		}
		File file = serverControl.getBinary();
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
				installTask = new InstallTaskLocal(serverControl, this, preferences, network, activity);
				installTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
	}

	void finish(boolean success) {
		OnInstallServerListener listener;
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
