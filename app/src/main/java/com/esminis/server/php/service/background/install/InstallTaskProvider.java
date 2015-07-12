package com.esminis.server.php.service.background.install;

import android.content.Context;

import com.esminis.server.php.Application;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.background.BackgroundServiceTaskProvider;
import com.esminis.server.php.service.server.Php;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class InstallTaskProvider implements BackgroundServiceTaskProvider {

	@Inject
	protected Preferences preferences;

	@Inject
	protected Php php;

	@Inject
	protected InstallToDocumentRoot installToDocumentRoot;

	@Override
	public Observable<Void> createTask(final Context context) {
		((Application)context.getApplicationContext()).getObjectGraph().inject(this);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				InstallHelper helper = new InstallHelper();
				if (!preferences.contains(context, Preferences.DOCUMENT_ROOT)) {
					try {
						installToDocumentRoot.install(context, true);
					} catch (Exception ignored) {}
				}
				String[] list = preferences.getInstallPaths(context);
				File moduleDirectory = php.getPhp().getParentFile();
				if (!helper.fromAssetFiles(moduleDirectory, list, context)) {
					subscriber.onError(new Exception("Install failed"));
					return;
				}
				HashMap<String, String> variables = new HashMap<>();
				variables.put("moduleDirectory", moduleDirectory.getAbsolutePath());
				helper.preprocessFile(new File(php.getPhp().getParentFile(), "odbcinst.ini"), variables);
				subscriber.onCompleted();
			}
		});
	}

}
