package com.esminis.server.mariadb.server;

import android.content.Context;
import android.os.Build;

import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallHelper;
import com.esminis.server.mariadb.R;
import com.esminis.server.mariadb.application.MariaDbApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class InstallServerMariaDbTaskProvider implements BackgroundServiceTaskProvider {

	@Inject
	protected ServerControl serverControl;

	@Override
	public Observable<Void> createTask(final Context context) {
		((MariaDbApplication)context.getApplicationContext()).getComponent().inject(this);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				InstallHelper helper = new InstallHelper();
				String[] list = getInstallPaths(context);
				final File moduleDirectory = serverControl.getBinary().getParentFile();
				if (!helper.fromAssetFiles(moduleDirectory, list, context)) {
					subscriber.onError(new Exception("Install failed"));
					return;
				}
				subscriber.onCompleted();
			}
		});
	}

	public String[] getInstallPaths(Context context) {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, context.getResources().getStringArray(R.array.install_binaries));
		String pathBinaries = "bin/" + (Build.CPU_ABI.toLowerCase().startsWith("x86") ? "x86" : "arm")
			+ "/";
		for (int i = 0; i < list.size(); i++) {
			list.set(i, pathBinaries + list.get(i));
		}
		Collections.addAll(list, context.getResources().getStringArray(R.array.install_files));
		return list.toArray(new String[list.size()]);
	}

}
