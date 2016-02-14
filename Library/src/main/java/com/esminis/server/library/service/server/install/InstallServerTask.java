package com.esminis.server.library.service.server.install;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;

import com.esminis.server.library.R;
import com.esminis.server.library.activity.main.MainActivity;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;
import com.esminis.server.library.service.server.ServerControl;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import rx.Observable;
import rx.Subscriber;

public class InstallServerTask implements Observable.OnSubscribe<Void> {

	private final ServerControl serverControl;
	private final LibraryApplication application;
	private final Preferences preferences;
	private final Network network;
	private final Class<? extends BackgroundServiceTaskProvider> classInstallTask;
	private final String defaultDocumentRoot;

	public InstallServerTask(
		ServerControl serverControl, Preferences preferences, Network network,
		LibraryApplication application,
		Class<? extends BackgroundServiceTaskProvider> classInstallTask, String defaultDirectoryName
	) {
		this.serverControl = serverControl;
		this.application = application;
		this.network = network;
		this.preferences = preferences;
		this.classInstallTask = classInstallTask;
		this.defaultDocumentRoot = new File(
			Environment.getExternalStorageDirectory(), defaultDirectoryName
		).getAbsolutePath();
	}

	@Override
	public void call(final Subscriber<? super Void> subscriber) {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		final BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (
					intent.getAction() == null ||
					!intent.getAction().equals(MainActivity.getIntentActionServerStatus(context))
				) {
					return;
				}
				final Bundle extras = intent.getExtras();
				if (extras == null || extras.containsKey("errorLine") || extras.getBoolean("running")) {
					return;
				}
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException ignored) {}
			}
		};
		application.registerReceiver(
			receiver, new IntentFilter(MainActivity.getIntentActionServerStatus(application))
		);
		serverControl.requestStop();
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException ignored) {}
		application.unregisterReceiver(receiver);

		if (!preferences.contains(application, Preferences.PORT)) {
			preferences.set(application, Preferences.PORT, application.getString(R.string.default_port));
		}
		if (!preferences.contains(application, Preferences.ADDRESS)) {
			preferences.set(application, Preferences.ADDRESS, network.get(0).name);
		}
		if (!preferences.contains(application, Preferences.DOCUMENT_ROOT)) {
			preferences.set(application, Preferences.DOCUMENT_ROOT, defaultDocumentRoot);
		}

		BackgroundService.execute(
			application, classInstallTask, new Subscriber<Void>() {
				@Override
				public void onCompleted() {
					preferences.set(application, Preferences.BUILD, preferences.getBuild(application));
					subscriber.onCompleted();
				}

				@Override
				public void onError(Throwable e) {
					subscriber.onError(e);
				}

				@Override
				public void onNext(Void dummy) {}
			}
		);
	}

}
