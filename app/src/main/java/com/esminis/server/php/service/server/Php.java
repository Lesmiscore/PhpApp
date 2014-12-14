/**
 * Copyright 2014 Tautvydas Andrikys
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

import com.esminis.model.manager.Process;
import com.esminis.server.php.R;
import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Log;
import com.esminis.server.php.model.manager.Preferences;

import java.io.File;
import java.io.IOException;

public class Php {
	
	static public final String INTENT_ACTION = "STATUS_SERVER_CHANGED";

	private java.lang.Process process = null;
	
	private File php = null;

	private File modulesDirectory = null;
	
	private String address = "";
	
	private boolean start = false;
	
	private Preferences preferences;

	protected Network network = null;

	private PhpHandler handler = null;

	private Context context = null;

	private PhpStartup startup = null;

	private PhpStreamReader streamReader = null;

	private Process managerProcess = null;

	private Log log = null;

	public Php(
		Network network, Process process, PhpStartup startup, Preferences preferences, Log log,
		Context context
	) {
		this.log = log;
		this.preferences = preferences;
		this.startup = startup;
		this.managerProcess = process;
		this.network = network;
		this.context = context.getApplicationContext();
		modulesDirectory = context.getFilesDir();
		php = new File(modulesDirectory, "php");
		address = getIPAddress() + ":" + preferences.getString(context, Preferences.PORT);
		handler = new PhpHandler(context, this, preferences);
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
		File fileRoot = new File(root);
		if (!fileRoot.isDirectory()) {
			handler.sendError(context.getString(R.string.error_document_root_does_not_exist));
		}
		try {
			process = startup.start(
				php, address, root, modulesDirectory, fileRoot,
				preferences.getBoolean(context, Preferences.KEEP_RUNNING),
				preferences.getEnabledModules(context), context
			);
			streamReader = new PhpStreamReader(this, handler);
			streamReader.execute(process.getErrorStream());
		} catch (IOException error) {
			if (process == null) {
				handler.sendError(error.getCause().getMessage());
			}
		}
	}

	private void stop() {
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
	
	private void status() {
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
		Intent intent = new Intent(INTENT_ACTION);		
		intent.putExtra("running", running);
		if (running) {
			intent.putExtra("address", realAddress);
		}
		context.sendBroadcast(intent);
	}

	public void requestStatus() {
		handler.sendAction("status");
	}

	public void requestStop() {
		handler.sendAction("stop");
	}

	public void requestStart() {
		if (handler.isReady()) {
			handler.sendAction("start");
		} else {
			start = true;
		}
	}

	public void requestRestartIfRunning() {
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() != null && intent.getAction().equals(Php.INTENT_ACTION)) {
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
		requestStop();
		requestStart();
	}

	protected void onHandlerReady() {
		status();
		if (start) {
			requestStart();
		}
	}

	protected void onHandlerMessage(Message message) {
		Bundle data = message.getData();
		if (data == null) {
			return;
		}
		if (data.get("action").equals("error")) {
			String line = data.getString("message");
			if (line != null) {
				log.add(context, line);
				Intent intent = new Intent(INTENT_ACTION);
				intent.putExtra("errorLine", line);
				context.sendBroadcast(intent);
			}
			return;
		}
		if (data.get("action").equals("start")) {
			address = getIPAddress() + ":" + data.getString("port");
			start(data.getString("documentRoot"));
		} else if (data.get("action").equals("stop")) {
			stop();
		}
		status();
	}
	
}
