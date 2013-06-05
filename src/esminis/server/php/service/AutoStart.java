package esminis.server.php.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (
			new Preferences(context).getBoolean(Preferences.START_ON_BOOT)
		) {
			PhpServer.getInstance(context).startWhenReady();
		}
	}
	
}
