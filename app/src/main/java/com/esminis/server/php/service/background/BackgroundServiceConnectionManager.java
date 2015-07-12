package com.esminis.server.php.service.background;

import android.app.Application;
import android.os.Messenger;

class BackgroundServiceConnectionManager {

	private final Object lock = new Object();
	private Messenger messenger = null;
	private BackgroundServiceConnection connection = null;

	Messenger connect(Application application) {
		synchronized (lock) {
			if (messenger == null) {
				if (connection != null) {
					connection.disconnect();
				}
				connection = new BackgroundServiceConnection(application);
				messenger = connection.connect();
			}
			return messenger;
		}
	}

}
