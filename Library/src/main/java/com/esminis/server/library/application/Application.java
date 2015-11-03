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
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.server.install.InstallServer;
import com.esminis.server.library.service.server.tasks.StatusServerTaskProvider;

import java.util.List;

import dagger.ObjectGraph;

abstract public class Application extends android.app.Application {

	private ObjectGraph objectGraph;

	private ServerControl serverControl;

	@Override
	public void onCreate() {
		super.onCreate();
		objectGraph = ObjectGraph.create(new ApplicationModule(this), createApplicationModule());
		serverControl = objectGraph.get(ServerControl.class);
		if (!getIsMainApplicationProcess()) {
			serverControl.requestStatus();
			return;
		}
		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (MainActivity.INTENT_ACTION.equals(intent.getAction())) {
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
		registerReceiver(receiver, new IntentFilter(MainActivity.INTENT_ACTION));
		BackgroundService.execute(this, StatusServerTaskProvider.class);
	}

	public ObjectGraph getObjectGraph() {
		return objectGraph;
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

	abstract protected Object createApplicationModule();

	abstract protected ServerFactory createServerFactory();

	public interface ServerFactory {

		ServerControl createControl(
			Network network, com.esminis.server.library.model.manager.Process process, Log log,
			Preferences preferences
		);

		InstallServer.InstallTaskFactory createInstallTaskFactory(
			Network network, Preferences preferences, ServerControl serverControl
		);

	}

	abstract public Class<? extends Fragment> getMenuFragmentClass();

}
