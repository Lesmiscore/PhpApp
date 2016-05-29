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
package com.esminis.server.library.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.List;

class ReceiverManager {

	private static class Receiver {

		private final BroadcastReceiver receiver;
		private final IntentFilter filter;
		private Context context = null;

		private Receiver(BroadcastReceiver receiver, IntentFilter filter) {
			this.receiver = receiver;
			this.filter = filter;
		}

		private void register(Context context) {
			unregister();
			this.context = context;
			if (context != null) {
				context.registerReceiver(receiver, filter);
			}
		}

		private void unregister() {
			if (context != null) {
				context.unregisterReceiver(receiver);
				context = null;
			}
		}

	}

	private final List<Receiver> list = new ArrayList<>();
	private boolean paused = true;

	void add(Context context, IntentFilter filter, BroadcastReceiver broadcastReceiver) {
		final Receiver receiver = new Receiver(broadcastReceiver, filter);
		list.add(receiver);
		if (!paused) {
			receiver.register(context);
		}
	}

	void onPause() {
		if (paused) {
			return;
		}
		paused = true;
		for (Receiver receiver : list) {
			receiver.unregister();
		}
	}

	void onResume(Context context) {
		if (!paused) {
			return;
		}
		paused = false;
		for (Receiver receiver : list) {
			receiver.register(context);
		}
	}

	void cleanup() {
		onPause();
		list.clear();
	}

}
