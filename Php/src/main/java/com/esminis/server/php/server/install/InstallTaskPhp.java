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
package com.esminis.server.php.server.install;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;

import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.server.install.InstallServer;
import com.esminis.server.library.service.server.install.InstallServerTask;

import java.io.File;

import rx.Subscriber;
import rx.Subscription;

public class InstallTaskPhp extends InstallServerTask {

	private boolean installSuccess = false;
	private boolean canStartInstall = false;
	private ServerControl serverControl;
	private LibraryApplication application;
	private InstallServer.OnInstallListener listener;
	private Preferences preferences;
	private Network network;

	public InstallTaskPhp(
		ServerControl serverControl, InstallServer.OnInstallListener listener, Preferences preferences,
		Network network, Activity activity
	) {
		this.serverControl = serverControl;
		this.application = (LibraryApplication)activity.getApplication();
		this.network = network;
		this.preferences = preferences;
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground(Void... arguments) {
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() == null || !intent.getAction().equals(MainActivity.INTENT_ACTION)) {
					return;
				}
				Bundle extras = intent.getExtras();
				if (extras == null || extras.containsKey("errorLine") || extras.getBoolean("running")) {
					return;
				}
				canStartInstall = true;
			}
		};
		application.registerReceiver(receiver, new IntentFilter(MainActivity.INTENT_ACTION));
		canStartInstall = false;
		serverControl.requestStop();
		while (!canStartInstall) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ignored) {}
		}
		application.unregisterReceiver(receiver);

		Subscription subscription = BackgroundService.execute(
			application, InstallTaskProvider.class,
			new Subscriber<Void>() {
				@Override
				public void onCompleted() {
					installSuccess = true;
				}

				@Override
				public void onError(Throwable e) {
				}

				@Override
				public void onNext(Void dummy) {
				}
			}
		);
		while (!subscription.isUnsubscribed()) {
			Thread.yield();
		}
		if (!installSuccess) {
			return false;
		}
		initializePreferences();
		return true;
	}

	private void initializePreferences() {
		if (!preferences.contains(application, Preferences.PORT)) {
			preferences.set(application, Preferences.PORT, "8080");
		}
		if (!preferences.contains(application, Preferences.ADDRESS)) {
			preferences.set(application, Preferences.ADDRESS, network.get(0).name);
		}
		if (!preferences.contains(application, Preferences.DOCUMENT_ROOT)) {
			preferences.set(
				application, Preferences.DOCUMENT_ROOT,
				getDefaultDocumentRoot().getAbsolutePath()
			);
		}
		preferences.set(application, Preferences.BUILD, preferences.getBuild(application));
	}

	@Override
	protected void onCancelled() {
		listener.onFinished(false);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		listener.onFinished(result);
	}

	public File getDefaultDocumentRoot() {
		return new File(Environment.getExternalStorageDirectory(), "www");
	}

}
