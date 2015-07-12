package com.esminis.server.php.service.background;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import rx.Subscriber;

class BackgroundServiceExecutor {

	private final Object lock = new Object();
	private Boolean result = null;
	static private long nextMessageId = 0;
	static final private BackgroundServiceLauncher launcher = new BackgroundServiceLauncher();

	BackgroundServiceExecutor(
		Application application, Class<? extends BackgroundServiceTaskProvider> provider,
		Subscriber<? super Void> subscriber
	) {
		launcher.start(application);
		final long messageId;
		synchronized (lock) {
			messageId = nextMessageId++;
		}
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle bundle = intent.getExtras();
				if (
					!BackgroundService.INTENT_ACTION.equals(intent.getAction()) || bundle == null ||
					bundle.getLong(BackgroundService.FIELD_MESSAGE_ID) != messageId ||
					!bundle.containsKey(BackgroundService.FIELD_ACTION)
				) {
					return;
				}
				synchronized (lock) {
					result = bundle.getInt(BackgroundService.FIELD_ACTION) ==
						BackgroundService.ACTION_TASK_COMPLETE;
				}
			}
		};
		application.registerReceiver(receiver, new IntentFilter(BackgroundService.INTENT_ACTION));
		Intent intent = new Intent(BackgroundService.INTENT_ACTION);
		intent.putExtra(BackgroundService.FIELD_PROVIDER, provider.getName());
		intent.putExtra(BackgroundService.FIELD_MESSAGE_ID, messageId);
		application.sendBroadcast(intent);
		for (;;) {
			synchronized (lock) {
				if (result != null) {
					break;
				}
			}
			Thread.yield();
		}
		application.unregisterReceiver(receiver);
		synchronized (lock) {
			if (result) {
				subscriber.onCompleted();
			} else {
				subscriber.onError(new Exception("Task failed"));
			}
		}
	}

}
