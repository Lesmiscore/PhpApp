package com.esminis.server.library.service.server.installpackage;

import android.content.Context;
import android.os.Bundle;

import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.InstallPackageManager;
import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;

import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class InstallPackageTaskProvider implements BackgroundServiceTaskProvider {

	@Override
	public Observable<Void> createTask(final Context context, final Bundle data) {
		final LibraryApplication application = (LibraryApplication)context.getApplicationContext();
		final InstallerPackage installer = application.getComponent().getInstallerPackage();
		final InstallPackageManager installPackageManager = application.getComponent()
			.getInstallPackageManager();
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				try {
					synchronized (installer) {
						installer.install(
							application, new com.esminis.server.library.model.InstallPackage(
								new JSONObject(data.getString("package"))
							), application.getComponent().getServerControl(), installPackageManager
						);
					}
					subscriber.onCompleted();
				} catch (Throwable throwable) {
					subscriber.onError(throwable);
				}
			}
		}).subscribeOn(Schedulers.newThread());
	}

}
