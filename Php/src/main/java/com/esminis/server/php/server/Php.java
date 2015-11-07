/**
 * Copyright 2015 Tautvydas Andrikys
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
package com.esminis.server.php.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.StringRes;
import android.util.Pair;

import com.esminis.server.library.model.manager.Process;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerHandler;
import com.esminis.server.library.service.server.ServerStreamReader;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.php.R;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.server.tasks.RestartIfRunningServerTaskProvider;
import com.esminis.server.library.service.server.tasks.RestartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StatusServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StopServerTaskProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Php implements ServerControl {

	private java.lang.Process process = null;
	
	private File php = null;

	private File modulesDirectory = null;
	
	private String address = "";
	
	private boolean start = false;
	
	private Preferences preferences;

	protected Network network = null;

	private ServerHandler serverHandler = null;

	private LibraryApplication context = null;

	private PhpStartup startup = null;

	private ServerStreamReader streamReader = null;

	private Process managerProcess = null;

	private Log log = null;

	private final boolean mainProcess;

	public Php(
		Network network, Process process, Preferences preferences, Log log, LibraryApplication application,
		boolean mainProcess
	) {
		this.log = log;
		this.preferences = preferences;
		this.startup = new PhpStartup(process);
		this.managerProcess = process;
		this.network = network;
		this.context = application;
		modulesDirectory = context.getFilesDir();
		php = new File(modulesDirectory, "php");
		address = getIPAddress() + ":" + preferences.getString(context, Preferences.PORT);
		this.mainProcess = mainProcess;
	}

	private ServerHandler getServerHandler() {
		if (serverHandler == null) {
			serverHandler = new ServerHandler(this);
		}
		return serverHandler;
	}

	public File getBinary() {
		return php;
	}
	
	private String getIPAddress() {
		int position = network.getPosition(preferences.getString(context, Preferences.ADDRESS));
		return position == -1 ? "0.0.0.0" : network.get(position).address;
	}

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
		String[] modules = getEnabledModules(context);
		if (!fileRoot.canWrite()) {
			final List<String> list = new ArrayList<>();
			for (String module : modules) {
				if ("zend_opcache".equals(module)) {
					sendWarning(R.string.warning_opcache_disabled);
				} else {
					list.add(module);
				}
			}
			modules = list.toArray(new String[list.size()]);
		}
		validatePhpIni(new File(fileRoot, "php.ini"));
		try {
			process = startup.start(
				php, address, root, modulesDirectory, fileRoot,
				preferences.getBoolean(context, Preferences.KEEP_RUNNING),
				preferences.getBoolean(context, Preferences.INDEX_PHP_ROUTER), modules, context
			);
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
		if (process != null) {
			process.destroy();
			process = null;
		}
		managerProcess.kill(php);
	}

	private Pair<Boolean, String> getStatus() {
		boolean running = process != null;
		String realAddress = address;
		if (process == null) {
			String[] commandLine = managerProcess.getCommandLine(php);
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

	private void status() {
		Pair<Boolean, String> status = getStatus();
		Intent intent = new Intent(MainActivity.INTENT_ACTION);
		intent.putExtra("running", status.first);
		if (status.first) {
			intent.putExtra("address", status.second);
		}
		context.sendBroadcast(intent);
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

	public void requestStart() {
		if (mainProcess) {
			BackgroundService.execute(context, StartServerTaskProvider.class);
		} else if (getServerHandler().isReady()) {
			sendAction("start");
		} else {
			start = true;
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

	public void requestRestartIfRunning() {
		if (mainProcess) {
			BackgroundService.execute(context, RestartIfRunningServerTaskProvider.class);
			return;
		}
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (MainActivity.INTENT_ACTION.equals(intent.getAction())) {
					Bundle extra = intent.getExtras();
					if (extra != null && !extra.containsKey("errorLine") && extra.getBoolean("running")) {
						requestRestart();
					}
					context.unregisterReceiver(this);
				}
			}
		}, new IntentFilter(MainActivity.INTENT_ACTION));
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

	public void onHandlerReady() {
		status();
		if (
			start || (
				preferences.getBoolean(context, Preferences.SERVER_STARTED) &&
				preferences.getBoolean(context, Preferences.KEEP_RUNNING)
			)
		) {
			requestStart();
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
				Intent intent = new Intent(MainActivity.INTENT_ACTION);
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

	private void validatePhpIni(File file) {
		FileInputStream inputStream = null;
		Properties properties = new Properties();
		try {
			inputStream = new FileInputStream(file);
			properties.load(inputStream);
		} catch (IOException ignored) {
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ignored) {}
			}
		}
		validateIsPhpIniDirectory(properties, "session.save_path");
		validateIsPhpIniDirectory(properties, "upload_tmp_dir");
	}

	private void validateIsPhpIniDirectory(Properties properties, String property) {
		final String path = properties.getProperty(property, null);
		final File file = path == null ? null : new File(path);
		Integer error = null;
		if (file == null) {
			error = R.string.warning_php_ini_property_not_defined;
		} else if (!file.isDirectory()) {
			error = R.string.warning_php_ini_directory_does_not_exist;
		} else if (!file.canWrite()) {
			error = R.string.warning_php_ini_directory_not_writable;
		}
		if (error != null) {
			sendWarning(error, property);
		}
	}

	private void sendWarning(@StringRes int message, String... parameters) {
		getServerHandler().sendError(
			context.getString(R.string.warning_message, context.getString(message, parameters))
		);
	}

	public String[] getEnabledModules(Context context) {
		List<String> modules = new ArrayList<>();
		String[] list = context.getResources().getStringArray(R.array.modules);
		for (int i = 0; i < list.length; i += 3) {
			if (preferences.getBoolean(context, "module_" + list[i])) {
				modules.add(list[i]);
			}
		}
		return modules.toArray(new String[modules.size()]);
	}

}
