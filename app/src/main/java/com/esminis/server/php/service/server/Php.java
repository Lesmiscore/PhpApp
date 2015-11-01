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
package com.esminis.server.php.service.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.StringRes;
import android.util.Pair;

import com.esminis.model.manager.Process;
import com.esminis.server.php.Application;
import com.esminis.server.php.R;
import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Log;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.background.BackgroundService;
import com.esminis.server.php.service.server.tasks.RestartIfRunningServerTaskProvider;
import com.esminis.server.php.service.server.tasks.RestartServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StartServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StatusServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StopServerTaskProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Php {
	
	static public final String INTENT_ACTION = "STATUS_SERVER_CHANGED";

	private java.lang.Process process = null;
	
	private File php = null;

	private File modulesDirectory = null;
	
	private String address = "";
	
	private boolean start = false;
	
	private Preferences preferences;

	protected Network network = null;

	private PhpHandler phpHandler = null;

	private Application context = null;

	private PhpStartup startup = null;

	private PhpStreamReader streamReader = null;

	private Process managerProcess = null;

	private Log log = null;

	private final boolean mainProcess;

	public Php(
		Network network, Process process, PhpStartup startup, Preferences preferences, Log log,
		Application application, boolean mainProcess
	) {
		this.log = log;
		this.preferences = preferences;
		this.startup = startup;
		this.managerProcess = process;
		this.network = network;
		this.context = application;
		modulesDirectory = context.getFilesDir();
		php = new File(modulesDirectory, "php");
		address = getIPAddress() + ":" + preferences.getString(context, Preferences.PORT);
		this.mainProcess = mainProcess;
	}

	private PhpHandler getPhpHandler() {
		if (phpHandler == null) {
			phpHandler = new PhpHandler(context, this, preferences);
		}
		return phpHandler;
	}

	public File getPhp() {
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
			getPhpHandler().sendError(context.getString(R.string.error_document_root_does_not_exist));
			return;
		}
		String[] modules = preferences.getEnabledModules(context);
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
			streamReader = new PhpStreamReader(this, getPhpHandler());
			streamReader.execute(process.getErrorStream());
			preferences.set(context, Preferences.SERVER_STARTED, true);
		} catch (IOException error) {
			if (process == null) {
				getPhpHandler().sendError(error.getCause().getMessage());
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
		Intent intent = new Intent(INTENT_ACTION);		
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
			getPhpHandler().sendAction("status");
		}
	}

	public void requestStop() {
		if (mainProcess) {
			BackgroundService.execute(context, StopServerTaskProvider.class);
		} else {
			getPhpHandler().sendAction("stop");
		}
	}

	public void requestStart() {
		if (mainProcess) {
			BackgroundService.execute(context, StartServerTaskProvider.class);
		} else if (getPhpHandler().isReady()) {
			getPhpHandler().sendAction("start");
		} else {
			start = true;
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
				if (Php.INTENT_ACTION.equals(intent.getAction())) {
					Bundle extra = intent.getExtras();
					if (extra != null && !extra.containsKey("errorLine") && extra.getBoolean("running")) {
						requestRestart();
					}
					context.unregisterReceiver(this);
				}
			}
		}, new IntentFilter(Php.INTENT_ACTION));
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

	protected void onHandlerReady() {
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

	protected void onHandlerMessage(Message message) {
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
				Intent intent = new Intent(INTENT_ACTION);
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
		getPhpHandler().sendError(
			context.getString(R.string.warning_message, context.getString(message, parameters))
		);
	}

}
