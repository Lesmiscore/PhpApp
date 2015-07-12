package com.esminis.server.php.service.background;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

class BackgroundServiceConnection implements ServiceConnection {

	static private final int STATE_CONNECTING = 1;
	static private final int STATE_CONNECTED = 2;
	static private final int STATE_FINISHED = 3;

	private final Application application;
	private final Object lock = new Object();
	private int state = STATE_CONNECTING;
	private Messenger messenger = null;

	BackgroundServiceConnection(Application application) {
		this.application = application;
		application.bindService(
			new Intent(application, BackgroundService.class), this, Context.BIND_AUTO_CREATE
		);
	}

	public void onServiceConnected(ComponentName className, IBinder service) {
		synchronized (lock) {
			if (state == STATE_CONNECTING) {
				messenger = new Messenger(service);
				lock.notify();
				state = STATE_CONNECTED;
			}
		}
	}

	public void onServiceDisconnected(ComponentName className) {}

	Messenger connect() {
		synchronized (lock) {
			if (messenger == null && state == STATE_CONNECTING) {
				try {
					lock.wait();
				} catch (InterruptedException ignored) {}
			}
			Messenger result = messenger;
			messenger = null;
			return result;
		}
	}

	void disconnect() {
		synchronized (lock) {
			if (state == STATE_CONNECTED) {
				application.unbindService(this);
				state = STATE_FINISHED;
			}
		}
	}

}
