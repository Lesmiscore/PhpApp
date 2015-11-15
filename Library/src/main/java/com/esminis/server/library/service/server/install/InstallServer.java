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
package com.esminis.server.library.service.server.install;

import android.app.Activity;
import android.os.AsyncTask;

import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.activity.MainActivity;

import java.io.File;

public class InstallServer {

	private final ServerControl serverControl;
	private final Preferences preferences;
	private final InstallTaskFactory factory;

	private OnInstallServerListener listener = null;
	private InstallServerTask installTask = null;
	private final Object lock = new Object();

	public interface OnInstallListener {

		void onFinished(boolean result);

	}

	public interface InstallTaskFactory {

		InstallServerTask create(Activity activity, OnInstallListener listener);

	}

	public InstallServer(
		Preferences preferences, ServerControl serverControl, InstallTaskFactory factory
	) {
		this.factory = factory;
		this.preferences = preferences;
		this.serverControl = serverControl;
	}

	public void install(MainActivity activity) {
		synchronized (lock) {
			this.listener = activity;
			if (installTask != null) {
				return;
			}
		}
		File file = serverControl.getBinary();
		if (file.isFile() && preferences.getIsInstalled(activity)) {
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

	public void installNewVersionConfirmed(Activity activity) {
		start(activity);
	}

	public void installFinish() {
		finish(true);
	}

	private void start(Activity activity) {
		synchronized (lock) {
			if (installTask == null) {
				installTask = factory.create(activity, new OnInstallListener() {
					@Override
					public void onFinished(boolean result) {
						finish(result);
					}
				});
				installTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
	}

	private void finish(boolean success) {
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
