package com.esminis.server.php.service.background;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BackgroundService extends Service {

	static public final int ACTION_TASK = 1;
	static public final int ACTION_TASK_COMPLETE = 2;
	static public final int ACTION_TASK_FAILED = 3;

	static final String FIELD_PROVIDER = "provider";
	static final String FIELD_MESSAGE_ID = "id";

	private final Messenger messenger = new Messenger(
		new Handler() {
			@Override
			public void handleMessage(final Message message) {
				if (message.what != ACTION_TASK) {
					super.handleMessage(message);
					return;
				}
				try {
					Bundle data = message.getData();
					Class<?> taskProviderClass = Class.forName(data.getString(FIELD_PROVIDER));
					if (!BackgroundServiceTaskProvider.class.isAssignableFrom(taskProviderClass)) {
						sendMessageForSender(message, ACTION_TASK_FAILED);
						return;
					}
					BackgroundServiceTaskProvider provider = (BackgroundServiceTaskProvider)
						taskProviderClass.newInstance();
					provider.createTask(getApplicationContext()).subscribe(new Subscriber<Void>() {
						@Override
						public void onCompleted() {
							sendMessageForSender(message, ACTION_TASK_COMPLETE);
						}

						@Override
						public void onError(Throwable e) {
							sendMessageForSender(message, ACTION_TASK_FAILED);
						}

						@Override
						public void onNext(Void aVoid) {}
					});
				} catch (Exception ignored) {
					sendMessageForSender(message, ACTION_TASK_FAILED);
				}
			}
		}
	);

	private void sendMessageForSender(Message sender, int code) {
		try {
			Message message = Message.obtain(null, code);
			Bundle data = new Bundle();
			data.putLong(FIELD_MESSAGE_ID, sender.getData().getLong(FIELD_MESSAGE_ID));
			message.setData(data);
			sender.replyTo.send(message);
		} catch (RemoteException ignored) {}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	static public Observable<Void> execute(
		final Application application, final Class<? extends BackgroundServiceTaskProvider> provider)
	{
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				new BackgroundServiceExecutor(application, provider, subscriber);
			}
		}).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
	}

}
