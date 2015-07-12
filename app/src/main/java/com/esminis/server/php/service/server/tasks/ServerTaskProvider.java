package com.esminis.server.php.service.server.tasks;

import android.content.Context;

import com.esminis.server.php.Application;
import com.esminis.server.php.service.background.BackgroundServiceTaskProvider;

import rx.Observable;
import rx.Subscriber;

abstract class ServerTaskProvider implements BackgroundServiceTaskProvider {

	@Override
	public Observable<Void> createTask(Context context) {
		((Application)context.getApplicationContext()).getObjectGraph().inject(this);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				execute();
				subscriber.onCompleted();
			}
		});
	}

	abstract protected void execute();

}
