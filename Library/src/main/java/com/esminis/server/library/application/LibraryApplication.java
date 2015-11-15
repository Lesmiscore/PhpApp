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
package com.esminis.server.library.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.tasks.StatusServerTaskProvider;

import java.util.List;

abstract public class LibraryApplication<T extends LibraryApplicationComponent> extends Application {

	private T component;

	@Override
	public void onCreate() {
		super.onCreate();
		component = createComponent();
		final ServerControl serverControl = component.getServerControl();
		if (!getIsMainApplicationProcess()) {
			serverControl.requestStatus();
			return;
		}
		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (MainActivity.getIntentAction(context).equals(intent.getAction())) {
					Bundle extras = intent.getExtras();
					if (extras != null && extras.containsKey("errorLine")) {
						return;
					}
					if (extras != null && extras.getBoolean("running")) {
						serverControl.requestRestart();
					}
					unregisterReceiver(this);
				}
			}

		};
		registerReceiver(receiver, new IntentFilter(MainActivity.getIntentAction(this)));
		BackgroundService.execute(this, StatusServerTaskProvider.class);
	}

	abstract protected T createComponent();

	public T getComponent() {
		return component;
	}

	public boolean getIsMainApplicationProcess() {
		ActivityManager man = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list =  man.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo item : list) {
			if (
				android.os.Process.myPid() == item.pid &&
					getApplicationInfo().packageName.equals(item.processName)
				) {
				return true;
			}
		}
		return false;
	}

}
