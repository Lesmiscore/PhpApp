package com.esminis.server.php;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.esminis.server.php.service.server.Php;

import javax.inject.Inject;

import dagger.ObjectGraph;

public class Application extends android.app.Application {

	private ObjectGraph objectGraph;

	@Inject
	protected Php php;

	@Override
	public void onCreate() {
		super.onCreate();
		objectGraph = ObjectGraph.create(new ApplicationModule(this));
		objectGraph.inject(this);
		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() != null && intent.getAction().equals(Php.INTENT_ACTION)) {
					Bundle extras = intent.getExtras();
					if (extras != null && extras.containsKey("errorLine")) {
						return;
					}
					if (extras != null && extras.getBoolean("running")) {
						php.requestRestart();
					}
					unregisterReceiver(this);
				}
			}

		};
		registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
	}

	public ObjectGraph getObjectGraph() {
		return objectGraph;
	}

}
