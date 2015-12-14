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
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BackgroundService extends Service {

	static public final int ACTION_PING = 1;
	static public final int ACTION_PING_BACK = 2;
	static public final int ACTION_TASK_COMPLETE = 3;
	static public final int ACTION_TASK_FAILED = 4;

	static final String FIELD_PROVIDER = "provider";
	static final String FIELD_MESSAGE_ID = "id";
	static final String FIELD_ACTION = "action";
	static final String FIELD_ERROR = "error";

	static public String getIntentAction(Context context) {
		return "__BACKGROUND_SERVICE_TASK_" + context.getPackageName() + "__";
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			try {
				Bundle data = intent.getExtras();
				if (!getIntentAction(context).equals(intent.getAction()) || data == null) {
					return;
				}
				if (data.containsKey(FIELD_ACTION)) {
					if (data.getInt(FIELD_ACTION) == ACTION_PING) {
						sendMessageForSender(intent, ACTION_PING_BACK);
						return;
					}
					return;
				}
				Class<?> taskProviderClass = Class.forName(data.getString(FIELD_PROVIDER));
				if (!BackgroundServiceTaskProvider.class.isAssignableFrom(taskProviderClass)) {
					sendMessageForSenderFailed(
						intent, new Exception("Invalid task provider class: " + taskProviderClass.getName())
					);
					return;
				}
				BackgroundServiceTaskProvider provider = (BackgroundServiceTaskProvider)
					taskProviderClass.newInstance();
				provider.createTask(getApplicationContext()).subscribe(new Subscriber<Void>() {
					@Override
					public void onCompleted() {
						sendMessageForSender(intent, ACTION_TASK_COMPLETE);
					}

					@Override
					public void onError(Throwable e) {
						sendMessageForSenderFailed(intent, e);
					}

					@Override
					public void onNext(Void aVoid) {}
				});
			} catch (Exception e) {
				sendMessageForSenderFailed(intent, e);
			}
		}
	};

	private void sendMessageForSenderFailed(Intent intent, Throwable throwable) {
		Intent intentSend = new Intent(getIntentAction(getApplicationContext()));
		intentSend.putExtra(FIELD_ACTION, ACTION_TASK_FAILED);
		intentSend.putExtra(FIELD_MESSAGE_ID, intent.getExtras().getLong(FIELD_MESSAGE_ID));
		intentSend.putExtra(FIELD_ERROR, throwable);
		sendBroadcast(intentSend);
	}

	private void sendMessageForSender(Intent intent, int action) {
		Intent intentSend = new Intent(getIntentAction(getApplicationContext()));
		intentSend.putExtra(FIELD_ACTION, action);
		intentSend.putExtra(FIELD_MESSAGE_ID, intent.getExtras().getLong(FIELD_MESSAGE_ID));
		sendBroadcast(intentSend);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		registerReceiver(receiver, new IntentFilter(getIntentAction(getApplicationContext())));
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	static public void execute(
		final Application application, final Class<? extends BackgroundServiceTaskProvider> provider
	) {
		execute(
			application, provider, new Subscriber<Void>() {
				@Override
				public void onCompleted() {}

				@Override
				public void onError(Throwable e) {}

				@Override
				public void onNext(Void aVoid) {}
			}
		);
	}

	static public Subscription execute(
		final Application application, final Class<? extends BackgroundServiceTaskProvider> provider,
		Subscriber<Void> subscriber
	) {
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				new BackgroundServiceExecutor(application, provider, subscriber);
			}
		}).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
			.subscribe(subscriber);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
}
