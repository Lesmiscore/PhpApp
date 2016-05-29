/**
 * Copyright 2016 Tautvydas Andrikys
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

import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.service.server.ServerControl;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

public class AutoStart extends BroadcastReceiver {

	@Inject
	protected ServerControl serverControl;

	@Inject
	protected Preferences preferences;

	@Override
	public void onReceive(Context context, Intent intent) {
		Context applicationContext = context.getApplicationContext();
		if (!(applicationContext instanceof LibraryApplication)) {
			return;
		}
		if (serverControl == null) {
			((LibraryApplication)applicationContext).getComponent().inject(this);
		}
		if (preferences.getBoolean(context, Preferences.START_ON_BOOT)) {
			preferences.set(context, Preferences.SERVER_STARTED, true);
			serverControl.requestStart();
		} else {
			preferences.set(context, Preferences.SERVER_STARTED, false);
			serverControl.requestStop();
		}
	}
	
}
