package com.esminis.server.php.service.install;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;

class InstallTask extends AsyncTask<Void, Void, Boolean> {

	private final Object lock = new Object();

	private boolean installSuccess = false;
	private boolean canStartInstall = false;
	private Php php;
	private Activity activity;
	private InstallServer installServer;
	private Preferences preferences;
	private Network network;

	private Messenger messengerReceiver = new Messenger(new Handler() {

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
				case InstallService.ACTION_INSTALL_COMPLETE:
				case InstallService.ACTION_INSTALL_ERROR:
					synchronized (lock) {
						installSuccess = message.what == InstallService.ACTION_INSTALL_COMPLETE;
						lock.notify();
					}
					break;
				default:
					super.handleMessage(message);
			}
		}

	});

	public InstallTask(
		Php php, InstallServer installServer, Preferences preferences, Network network,
		Activity activity
	) {
		this.php = php;
		this.activity = activity;
		this.network = network;
		this.preferences = preferences;
		this.installServer = installServer;
	}

	@Override
	protected Boolean doInBackground(Void... arguments) {
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() == null || !intent.getAction().equals(Php.INTENT_ACTION)) {
					return;
				}
				Bundle extras = intent.getExtras();
				if (extras == null || extras.containsKey("errorLine") || extras.getBoolean("running")) {
					return;
				}
				canStartInstall = true;
			}
		};
		activity.registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		canStartInstall = false;
		php.requestStop();
		while (!canStartInstall) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ignored) {}
		}
		activity.unregisterReceiver(receiver);
		InstallTaskConnection connection = new InstallTaskConnection(activity);
		try {
			final Message message = Message.obtain(null, InstallService.ACTION_INSTALL);
			final Messenger messenger = connection.connect();
			message.replyTo = messengerReceiver;
			synchronized (lock) {
				messenger.send(message);
				try {
					lock.wait();
				} catch (InterruptedException ignored) {}
				if (!installSuccess) {
					return false;
				}
				initializePreferences();
				return true;
			}
		} catch (RemoteException e) {
			return false;
		} finally {
			connection.disconnect();
		}
	}

	private void initializePreferences() {
		if (!preferences.contains(activity, Preferences.PORT)) {
			preferences.set(activity, Preferences.PORT, "8080");
		}
		if (!preferences.contains(activity, Preferences.ADDRESS)) {
			preferences.set(activity, Preferences.ADDRESS, network.get(0).name);
		}
		if (!preferences.contains(activity, Preferences.DOCUMENT_ROOT)) {
			preferences.set(
				activity, Preferences.DOCUMENT_ROOT,
				preferences.getDefaultDocumentRoot().getAbsolutePath()
			);
		}
		preferences.set(activity, Preferences.PHP_BUILD, preferences.getPhpBuild(activity));
	}

	@Override
	protected void onCancelled() {
		installServer.finish(false);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		installServer.finish(result);
	}

}
