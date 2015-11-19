package com.esminis.server.mariadb.server;

import android.content.Context;

import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallHelper;
import com.esminis.server.mariadb.application.MariaDbApplication;

import java.io.File;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class MariaDbInstallServerTaskProvider implements BackgroundServiceTaskProvider {

	@Inject
	protected ServerControl serverControl;

	@Inject
	protected com.esminis.server.library.model.manager.Process managerProcess;

	@Inject
	protected Preferences preferences;

	@Override
	public Observable<Void> createTask(final Context context) {
		((MariaDbApplication)context.getApplicationContext()).getComponent().inject(this);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				final File binary = serverControl.getBinary();
				try {
					new InstallHelper().fromAssetDirectory(binary.getParentFile(), "install", context, true);
					if (!binary.canExecute() && !binary.setExecutable(true)) {
						subscriber.onError(new Exception("Install failed: cannot set execute permission"));
						return;
					}
					new MariaDbServerLauncher(managerProcess)
						.initializeDataDirectory(
							context, binary, new File(preferences.getString(context, Preferences.DOCUMENT_ROOT))
						);
					subscriber.onCompleted();
				} catch (Exception e) {
					subscriber.onError(new Exception("Install failed"));
				}
			}
		});
	}

}
