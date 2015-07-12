package com.esminis.server.php.service.background;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

class BackgroundServiceExecutor {

	static private final Object lock = new Object();
	static private Map<Long, Boolean> results = new HashMap<>();
	static private BackgroundServiceConnectionManager connectionManager = new BackgroundServiceConnectionManager();
	static private Messenger messengerReceiver = null;
	static private long nextMessageId = 0;

	BackgroundServiceExecutor(
		Application application, Class<? extends BackgroundServiceTaskProvider> provider,
		Subscriber<? super Void> subscriber
	) {
		final long messageId;
		synchronized (lock) {
			messageId = nextMessageId++;
		}
		try {
			final Message message = Message.obtain(null, BackgroundService.ACTION_TASK);
			final Messenger messenger = connectionManager.connect(application);
			if (messenger == null) {
				subscriber.onError(new Exception("Could not connect to background service"));
				return;
			}
			final Bundle bundle = new Bundle();
			bundle.putString(BackgroundService.FIELD_PROVIDER, provider.getName());
			bundle.putLong(BackgroundService.FIELD_MESSAGE_ID, messageId);
			message.replyTo = getMessengerReceiver();
			message.setData(bundle);
			synchronized (lock) {
				messenger.send(message);
				try {
					lock.wait();
				} catch (InterruptedException ignored) {}
				synchronized (lock) {
					if (results.containsKey(messageId) && results.get(messageId)) {
						subscriber.onCompleted();
					} else {
						subscriber.onError(new Exception("Task failed"));
					}
				}
			}
		} catch (RemoteException e) {
			subscriber.onError(e);
		}
		synchronized (lock) {
			if (results.containsKey(messageId)) {
				results.remove(messageId);
			}
		}
	}

	static private Messenger getMessengerReceiver() {
		synchronized (lock) {
			if (messengerReceiver == null) {
				HandlerThread handlerThread = new HandlerThread("backgroundServiceHelper");
				handlerThread.start();
				messengerReceiver = new Messenger(new Handler(handlerThread.getLooper()) {

					@Override
					public void handleMessage(Message message) {
						switch (message.what) {
							case BackgroundService.ACTION_TASK_COMPLETE:
							case BackgroundService.ACTION_TASK_FAILED:
								synchronized (lock) {
									results.put(
										message.getData().getLong(BackgroundService.FIELD_MESSAGE_ID),
										message.what == BackgroundService.ACTION_TASK_COMPLETE
									);
									lock.notify();
								}
								break;
							default:
								super.handleMessage(message);
						}
					}

				});
			}
			return messengerReceiver;
		}
	}

}
