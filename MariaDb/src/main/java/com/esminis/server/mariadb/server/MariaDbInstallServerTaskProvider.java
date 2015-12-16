package com.esminis.server.mariadb.server;

import android.content.Context;

import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallHelper;
import com.esminis.server.mariadb.application.MariaDbApplication;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;

@Singleton
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
				try {
					final File binary = serverControl.getBinary();
					if (installFiles(context, binary)) {
						initializeDataDirectory(context, binary);
						subscriber.onCompleted();
					} else {
						subscriber.onError(new Exception("cannot set execute permission"));
					}
				} catch (Throwable e) {
					subscriber.onError(e);
				}
			}
		});
	}

	private boolean installFiles(Context context, File binary) throws Exception {
		new InstallHelper().fromAssetDirectory(binary.getParentFile(), "install", context, true);
		return binary.canExecute() || binary.setExecutable(true);
	}

	private void initializeDataDirectory(Context context, File binary) throws IOException {
		if (!(serverControl instanceof MariaDb)) {
			throw new IOException("Invalid server control");
		}
		((MariaDb)serverControl).launcher.initializeDataDirectory(
			context, binary, new File(preferences.getString(context, Preferences.DOCUMENT_ROOT))
		);
	}

}
