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

import rx.Subscriber;

class BackgroundServiceExecutor {

	private final Object lock = new Object();
	private Boolean result = null;
	private Throwable resultError = null;
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
		final String intentAction = BackgroundService.getIntentAction(application);
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final Bundle bundle = intent.getExtras();
				if (
					!intentAction.equals(intent.getAction()) || bundle == null ||
					bundle.getLong(BackgroundService.FIELD_MESSAGE_ID) != messageId ||
					!bundle.containsKey(BackgroundService.FIELD_ACTION)
				) {
					return;
				}
				final int action = bundle.getInt(BackgroundService.FIELD_ACTION);
				if (
					action == BackgroundService.ACTION_TASK_COMPLETE ||
					action == BackgroundService.ACTION_TASK_FAILED
				) {
					synchronized (lock) {
						result = action == BackgroundService.ACTION_TASK_COMPLETE;
						if (
							action == BackgroundService.ACTION_TASK_FAILED &&
							bundle.containsKey(BackgroundService.FIELD_ERROR)
						) {
							Object object = bundle.getSerializable(BackgroundService.FIELD_ERROR);
							resultError = object instanceof Throwable ?
								(Throwable)object : new Exception("Invalid error message: " + object);
						}
					}
				}
			}
		};
		application.registerReceiver(receiver, new IntentFilter(intentAction));
		Intent intent = new Intent(intentAction);
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
			} else if (resultError == null) {
				subscriber.onError(new Exception("Task failed: " + provider.getName()));
			} else {
				subscriber.onError(resultError);
			}
		}
	}

}
