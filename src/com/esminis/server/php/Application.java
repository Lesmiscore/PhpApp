package com.esminis.server.php;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.esminis.model.manager.Manager;
import com.esminis.server.php.model.manager.Log;
import com.esminis.server.php.service.server.Php;

public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();
		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() != null && intent.getAction().equals(Php.INTENT_ACTION)) {
					Bundle extras = intent.getExtras();
					if (extras != null && extras.containsKey("errorLine")) {
						return;
					} else {
						if (extras != null && extras.getBoolean("running")) {
							Php.getInstance(context).requestRestart();
						}
						unregisterReceiver(this);
					}
				}
			}

		};
		registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
	}
}
