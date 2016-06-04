/**
 * Copyright 2016 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esminis.server.library.service.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.support.annotation.StringRes;
import android.text.Html;
import android.util.Pair;

import com.esminis.server.library.R;
import com.esminis.server.library.activity.main.MainActivity;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;
import com.esminis.server.library.service.server.tasks.RestartIfRunningServerTaskProvider;
import com.esminis.server.library.service.server.tasks.RestartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StatusServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StopServerTaskProvider;

import java.io.File;
import java.io.IOException;

import rx.Subscriber;

abstract public class ServerControl {

	private ServerHandler serverHandler = null;
	private final File binary;
	protected final LibraryApplication context;
	private final com.esminis.server.library.model.manager.Process managerProcess;
	private final boolean mainProcess;
	protected final Preferences preferences;
	private final Network network;
	private final ServerNotification serverNotification;

	private ServerStreamReader streamReader = null;
	private java.lang.Process process = null;
	private String address = "";
	private boolean start = false;
	private final Log log;

	public ServerControl(
		String binaryName, LibraryApplication context,
		Network network, Preferences preferences, Log log,
		com.esminis.server.library.model.manager.Process managerProcess, boolean mainProcess,
		ServerNotification serverNotification
	) {
		this.binary = new File(context.getFilesDir(), binaryName);
		this.context = context;
		this.managerProcess = managerProcess;
		this.mainProcess = mainProcess;
		this.network = network;
		this.preferences = preferences;
		this.log = log;
		address = getIPAddress() + ":" + preferences.getString(context, Preferences.PORT);
		this.serverNotification = serverNotification;
		getServerHandler();
	}

	private String getIPAddress() {
		int position = network.getPosition(preferences.getString(context, Preferences.ADDRESS));
		return position == -1 ? "0.0.0.0" : network.get(position).address;
	}

	private ServerHandler getServerHandler() {
		if (serverHandler == null) {
			serverHandler = new ServerHandler(this);
		}
		return serverHandler;
	}

	public File getBinary() {
		return binary;
	}

	public File getBinaryDirectory() {
		return binary.getParentFile();
	}

	protected void sendWarning(@StringRes int message, String... parameters) {
		getServerHandler().sendError(
			context.getString(R.string.warning_message, context.getString(message, parameters))
		);
	}

	abstract protected void stop(java.lang.Process process);

	abstract protected java.lang.Process start(File root, String address) throws IOException;

	private void start(String root) {
		if (process != null) {
			return;
		}
		if (getStatus().first) {
			stop(false);
		}
		final File fileRoot = new File(root);
		if (!fileRoot.isDirectory()) {
			getServerHandler().sendError(context.getString(R.string.error_document_root_does_not_exist));
			return;
		}
		try {
			process = start(fileRoot, address);
			streamReader = new ServerStreamReader(this, getServerHandler());
			streamReader.execute(process.getErrorStream());
		} catch (IOException error) {
			if (process == null) {
				getServerHandler().sendError(error.getCause().getMessage());
			}
		}
		getStatus();
	}

	private void stop(boolean restartIfUserDidNotStop) {
		if (streamReader != null) {
			streamReader.cancel(false);
			streamReader = null;
		}
		stop(process);
		managerProcess.kill(getBinary());
		if (process != null) {
			process.destroy();
			process = null;
		}
		getStatus();
		if (restartIfUserDidNotStop && preferences.getBoolean(context, Preferences.SERVER_STARTED)) {
			requestStart(null);
		}
	}

	private Pair<Boolean, String> getStatus() {
		boolean running = process != null;
		String realAddress = address;
		if (process == null) {
			String[] commandLine = managerProcess.getCommandLine(getBinary());
			if (commandLine != null) {
				boolean next = false;
				for (String part : commandLine) {
					if (part.equals("-S")) {
						next = true;
					} else if (next) {
						realAddress = part;
						break;
					}
				}
				running = true;
			}
		}
		if (running) {
			serverNotification.show(
				context.getApplicationContext(),
				Html.fromHtml(getServerRunningLabel(context, address)).toString(),
				context.getString(R.string.server_running_public)
			);
		} else {
			serverNotification.hide(context.getApplicationContext());
		}
		return new Pair<>(running, realAddress);
	}

	public void onHandlerReady() {
		status();
		if (start || preferences.getBoolean(context, Preferences.SERVER_STARTED)) {
			requestStart(null);
		}
	}

	public void onHandlerMessage(Message message) {
		if (mainProcess) {
			return;
		}
		Bundle data = message.getData();
		if (data == null) {
			return;
		}
		Object action = data.get("action");
		if ("error".equals(action)) {
			String line = data.getString("message");
			if (line != null) {
				log.add(context, line);
				Intent intent = new Intent(MainActivity.getIntentActionServerStatus(context));
				intent.putExtra("errorLine", line);
				context.sendBroadcast(intent);
			}
			return;
		}
		if ("start".equals(action)) {
			network.refresh();
			address = getIPAddress() + ":" + data.getString("port");
			start(data.getString("documentRoot"));
		} else if ("stop".equals(action)) {
			stop(true);
		}
		status();
	}

	private void status() {
		Pair<Boolean, String> status = getStatus();
		Intent intent = new Intent(MainActivity.getIntentActionServerStatus(context));
		intent.putExtra("running", status.first);
		if (status.first) {
			intent.putExtra("address", status.second);
		}
		context.sendBroadcast(intent);
	}

	private void requestMainExecute(
		Class<? extends BackgroundServiceTaskProvider> provider, Subscriber<Void> mainSubscriber
	) {
		if (mainSubscriber == null) {
			mainSubscriber = new Subscriber<Void>() {
				@Override
				public void onCompleted() {}

				@Override
				public void onError(Throwable e) {}

				@Override
				public void onNext(Void aVoid) {}
			};
		}
		BackgroundService.execute(context, provider, mainSubscriber);
	}

	public void requestStart(Subscriber<Void> mainSubscriber) {
		if (mainProcess) {
			requestMainExecute(StartServerTaskProvider.class, mainSubscriber);
		} else if (getServerHandler().isReady()) {
			sendAction("start");
		} else {
			start = true;
		}
	}

	public void requestStatus(Subscriber<Void> mainSubscriber) {
		if (mainProcess) {
			requestMainExecute(StatusServerTaskProvider.class, mainSubscriber);
		} else {
			sendAction("status");
		}
	}

	public void requestStop(Subscriber<Void> mainSubscriber) {
		if (mainProcess) {
			requestMainExecute(StopServerTaskProvider.class, mainSubscriber);
		} else {
			sendAction("stop");
		}
	}

	public void requestRestartIfRunning(Subscriber<Void> mainSubscriber) {
		if (mainProcess) {
			requestMainExecute(RestartIfRunningServerTaskProvider.class, mainSubscriber);
			return;
		}
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (MainActivity.getIntentActionServerStatus(context).equals(intent.getAction())) {
					Bundle extra = intent.getExtras();
					if (extra != null && !extra.containsKey("errorLine") && extra.getBoolean("running")) {
						requestRestart(null);
					}
					context.unregisterReceiver(this);
				}
			}
		}, new IntentFilter(MainActivity.getIntentActionServerStatus(context)));
		requestStatus(null);
	}

	public void requestRestart(Subscriber<Void> mainSubscriber) {
		if (mainProcess) {
			requestMainExecute(RestartServerTaskProvider.class, mainSubscriber);
		} else {
			requestStop(null);
			requestStart(null);
		}
	}

	private void sendAction(String action) {
		Bundle bundle = new Bundle();
		bundle.putString(
			Preferences.DOCUMENT_ROOT, preferences.getString(context, Preferences.DOCUMENT_ROOT)
		);
		bundle.putString(Preferences.PORT, preferences.getString(context, Preferences.PORT));
		getServerHandler().sendAction(action, bundle);
	}

	public String getServerRunningLabel(Context context, String address) {
		return String.format(
			context.getString(R.string.server_running),
			"<a href=\"http://" + address + "\">" + address + "</a>"
		);
	}

}
