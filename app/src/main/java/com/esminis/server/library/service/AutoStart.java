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
package com.esminis.server.library.service;

import com.esminis.server.php.Application;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.php.model.manager.Preferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import dagger.ObjectGraph;

public class AutoStart extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Context applicationContext = context.getApplicationContext();
		if (!(applicationContext instanceof Application)) {
			return;
		}
		ObjectGraph graph = ((Application)applicationContext).getObjectGraph();
		final ServerControl serverControl = graph.get(ServerControl.class);
		if (graph.get(Preferences.class).getBoolean(context, Preferences.START_ON_BOOT)) {
			serverControl.requestStart();
		} else {
			serverControl.requestStop();
		}
	}
	
}
