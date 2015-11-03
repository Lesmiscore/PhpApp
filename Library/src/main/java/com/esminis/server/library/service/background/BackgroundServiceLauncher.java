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
package com.esminis.server.library.service.background;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

class BackgroundServiceLauncher {

	private final Object lock = new Object();

	private boolean started = false;
	private boolean starting = false;
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (
				BackgroundService.INTENT_ACTION.equals(intent.getAction()) && bundle != null &&
				bundle.containsKey(BackgroundService.FIELD_ACTION) &&
				bundle.getInt(BackgroundService.FIELD_ACTION) == BackgroundService.ACTION_PING_BACK
			) {
				synchronized (lock) {
					if (!started) {
						started = true;
					}
				}
			}
		}
	};

	void start(Application application) {
		final boolean onlyWait;
		synchronized (lock) {
			if (started) {
				return;
			}
			onlyWait = starting;
			starting = true;
		}
		if (!onlyWait) {
			application.registerReceiver(receiver, new IntentFilter(BackgroundService.INTENT_ACTION));
			application.startService(new Intent(application, BackgroundService.class));
		}
		for (;;) {
			synchronized (lock) {
				if (!onlyWait) {
					Intent intent = new Intent(BackgroundService.INTENT_ACTION);
					intent.putExtra(BackgroundService.FIELD_ACTION, BackgroundService.ACTION_PING);
					application.sendBroadcast(intent);
				}
				if (started) {
					break;
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {}
		}
		if (!onlyWait) {
			application.unregisterReceiver(receiver);
		}
	}

}
