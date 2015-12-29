package com.esminis.server.library.activity;

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
