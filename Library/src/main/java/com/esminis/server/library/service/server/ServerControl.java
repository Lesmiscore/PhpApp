package com.esminis.server.library.service.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.support.annotation.StringRes;
import android.util.Pair;

import com.esminis.server.library.R;
import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.server.tasks.RestartIfRunningServerTaskProvider;
import com.esminis.server.library.service.server.tasks.RestartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StatusServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StopServerTaskProvider;

import java.io.File;
import java.io.IOException;

abstract public class ServerControl {

	private ServerHandler serverHandler = null;
	private final File binary;
	protected final LibraryApplication context;
	private final com.esminis.server.library.model.manager.Process managerProcess;
	private final boolean mainProcess;
	protected final Preferences preferences;
	private final Network network;

	private ServerStreamReader streamReader = null;
	private java.lang.Process process = null;
	private String address = "";
	private boolean start = false;
	private final Log log;

	public ServerControl(
		String binaryName, LibraryApplication context,
		Network network, Preferences preferences, Log log,
		com.esminis.server.library.model.manager.Process managerProcess, boolean mainProcess
	) {
		this.binary = new File(context.getFilesDir(), binaryName);
		this.context = context;
		this.managerProcess = managerProcess;
		this.mainProcess = mainProcess;
		this.network = network;
		this.preferences = preferences;
		this.log = log;
		address = getIPAddress() + ":" + preferences.getString(context, Preferences.PORT);
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
			stop();
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
			preferences.set(context, Preferences.SERVER_STARTED, true);
		} catch (IOException error) {
			if (process == null) {
				getServerHandler().sendError(error.getCause().getMessage());
			}
		}
	}

	private void stop() {
		preferences.set(context, Preferences.SERVER_STARTED, false);
		if (streamReader != null) {
			streamReader.cancel(false);
			streamReader = null;
		}
		stop(process);
		if (process != null) {
			process.destroy();
			process = null;
		}
		managerProcess.kill(getBinary());
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
		return new Pair<>(running, realAddress);
	}

	public void onHandlerReady() {
		status();
		if (
			start || (preferences.getBoolean(context, Preferences.SERVER_STARTED) && isKeepRunning())
		) {
			requestStart();
		}
	}

	protected boolean isKeepRunning() {
		return preferences.getBoolean(context, Preferences.KEEP_RUNNING);
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
				Intent intent = new Intent(MainActivity.getIntentAction(context));
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
			stop();
		}
		status();
	}

	private void status() {
		Pair<Boolean, String> status = getStatus();
		Intent intent = new Intent(MainActivity.getIntentAction(context));
		intent.putExtra("running", status.first);
		if (status.first) {
			intent.putExtra("address", status.second);
		}
		context.sendBroadcast(intent);
	}

	public void requestStart() {
		if (mainProcess) {
			BackgroundService.execute(context, StartServerTaskProvider.class);
		} else if (getServerHandler().isReady()) {
			sendAction("start");
		} else {
			start = true;
		}
	}

	public void requestStatus() {
		if (mainProcess) {
			BackgroundService.execute(context, StatusServerTaskProvider.class);
		} else {
			sendAction("status");
		}
	}

	public void requestStop() {
		if (mainProcess) {
			BackgroundService.execute(context, StopServerTaskProvider.class);
		} else {
			sendAction("stop");
		}
	}

	public void requestRestartIfRunning() {
		if (mainProcess) {
			BackgroundService.execute(context, RestartIfRunningServerTaskProvider.class);
			return;
		}
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (MainActivity.getIntentAction(context).equals(intent.getAction())) {
					Bundle extra = intent.getExtras();
					if (extra != null && !extra.containsKey("errorLine") && extra.getBoolean("running")) {
						requestRestart();
					}
					context.unregisterReceiver(this);
				}
			}
		}, new IntentFilter(MainActivity.getIntentAction(context)));
		requestStatus();
	}

	public void requestRestart() {
		if (mainProcess) {
			BackgroundService.execute(context, RestartServerTaskProvider.class);
		} else {
			requestStop();
			requestStart();
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

}
