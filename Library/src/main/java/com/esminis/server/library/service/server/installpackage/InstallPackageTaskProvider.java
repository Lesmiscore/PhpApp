package com.esminis.server.library.service.server.installpackage;

import android.content.Context;
import android.os.Bundle;

import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;

import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class InstallPackageTaskProvider implements BackgroundServiceTaskProvider {

	private final InstallPackage installer = new InstallPackage();

	@Override
	public Observable<Void> createTask(final Context context, final Bundle data) {
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				try {
					installer.install(
						context, new com.esminis.server.library.model.InstallPackage(
							new JSONObject(data.getString("package"))
						)
					);
					subscriber.onCompleted();
				} catch (Throwable throwable) {
					subscriber.onError(throwable);
				}
			}
		}).subscribeOn(Schedulers.newThread());
	}

}
