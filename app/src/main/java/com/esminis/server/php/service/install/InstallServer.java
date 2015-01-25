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

import android.content.Context;
import android.os.AsyncTask;

import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InstallServer {

	public interface OnInstallListener {

		public void OnInstallNewVersionRequest(InstallServer installer);

		public void OnInstallEnd(boolean success);
		
	}

	@Inject
	protected Php php;

	@Inject
	protected Preferences preferences;

	private OnInstallListener listener = null;
	private InstallTask installTask = null;
	private final Object lock = new Object();

	public void installIfNeeded(OnInstallListener listener, Context context) {
		synchronized (lock) {
			this.listener = listener;
			if (installTask != null) {
				return;
			}
		}
		File file = php.getPhp();
		if (file.isFile()) {
			if (!preferences.getIsSameBuild(context)) {
				if (listener != null) {
					listener.OnInstallNewVersionRequest(this);
				}
			} else {
				finish(true);
			}
		} else {
			start(context);
		}
	}

	public void continueInstall(Context context, boolean confirm) {
		if (confirm) {
			start(context);
		} else {
			finish(true);
		}
	}

	void start(Context context) {
		synchronized (lock) {
			if (installTask == null) {
				installTask = new InstallTask(php, this, context);
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
