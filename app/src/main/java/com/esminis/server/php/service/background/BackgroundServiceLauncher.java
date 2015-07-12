package com.esminis.server.php.service.background;

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
