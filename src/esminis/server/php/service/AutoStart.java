package esminis.server.php.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PhpServer server = PhpServer.getInstance(context);
		if (
			server.getPreferences()
				.getBoolean(PhpServer.PREFERENCES_START_ON_BOOT, false)
		) {
			server.startWhenReady();
		}
	}
	
}
