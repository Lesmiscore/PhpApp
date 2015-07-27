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
package com.esminis.server.php.service.background.install;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.background.BackgroundService;
import com.esminis.server.php.service.server.Php;

import rx.Subscriber;
import rx.Subscription;

class InstallTaskLocal extends AsyncTask<Void, Void, Boolean> {

	private boolean installSuccess = false;
	private boolean canStartInstall = false;
	private Php php;
	private Activity activity;
	private InstallServer installServer;
	private Preferences preferences;
	private Network network;

	public InstallTaskLocal(
		Php php, InstallServer installServer, Preferences preferences, Network network,
		Activity activity
	) {
		this.php = php;
		this.activity = activity;
		this.network = network;
		this.preferences = preferences;
		this.installServer = installServer;
	}

	@Override
	protected Boolean doInBackground(Void... arguments) {
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
		activity.registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		canStartInstall = false;
		php.requestStop();
		while (!canStartInstall) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ignored) {}
		}
		activity.unregisterReceiver(receiver);

		Subscription subscription = BackgroundService.execute(
			activity.getApplication(), InstallTaskProvider.class,
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
		if (!preferences.contains(activity, Preferences.PORT)) {
			preferences.set(activity, Preferences.PORT, "8080");
		}
		if (!preferences.contains(activity, Preferences.ADDRESS)) {
			preferences.set(activity, Preferences.ADDRESS, network.get(0).name);
		}
		if (!preferences.contains(activity, Preferences.DOCUMENT_ROOT)) {
			preferences.set(
				activity, Preferences.DOCUMENT_ROOT,
				preferences.getDefaultDocumentRoot().getAbsolutePath()
			);
		}
		preferences.set(activity, Preferences.PHP_BUILD, preferences.getPhpBuild(activity));
	}

	@Override
	protected void onCancelled() {
		installServer.finish(false);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		installServer.finish(result);
	}

}
