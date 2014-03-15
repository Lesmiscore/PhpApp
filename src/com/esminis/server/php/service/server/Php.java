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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.esminis.model.manager.Manager;
import com.esminis.model.manager.Process;
import com.esminis.server.php.R;
import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Log;
import com.esminis.server.php.model.manager.Preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Php {
	
	static public final String INTENT_ACTION = "STATUS_SERVER_CHANGED";
	
	private static Php instance = null;

	private java.lang.Process process = null;
	
	private File php = null;
	
	private String address = "";
	
	private boolean start = false;
	
	private Preferences preferences = Manager.get(Preferences.class);
	
	private Network network = Manager.get(Network.class);

	private PhpHandler handler = null;

	private Context context = null;

	static public Php getInstance(Context context) {
		if (instance == null) {
			instance = new Php(context);
		}
		return instance;
	}
	
	public File getPhp() {
		return php;
	}
	
	protected Php(Context context) {
		this.context = context.getApplicationContext();
		php = new File(context.getFilesDir() + File.separator + "php");		
		address = getIPAddress() + ":" + preferences.getString(context, Preferences.PORT);
		handler = new PhpHandler(this.context, this);
	}
	
	private String getIPAddress() {
		int position = network.getPosition(preferences.getString(context, Preferences.ADDRESS));
		return position == -1 ? "0.0.0.0" : network.get(position).address;
	}

	private String[] getModules() {
		return new String[] {};
	}

	private String[] getZendModules() {
		return new String[] {"opcache.so"};
	}

	private List<String> getIniModules(File iniDirectory) {
		List<String> list = new ArrayList<String>();
		File[] files = iniDirectory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().endsWith(".ini")) {
					list.addAll(getIniModulesFromFile(file));
				}
			}
		}
		return list;
	}

	private List<String> getIniModulesFromFile(File file) {
		List<String> list = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("#") || line.contains(";")) {
					continue;
				}
				if (line.contains("extension")) {
					File fileTemp = new File(
						line.replaceAll("^[^#]*(zend_extension|extension).*=(.+\\.so).*$", "$2").trim()
					);
					list.add(fileTemp.getName().toLowerCase());
				}
			}
			reader.close();
		} catch (IOException ignored) {
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ignored) {}
		}
		return list;
	}

	private void addStartupOptions(List<String> options, File iniDirectory) {
		File directory = context.getFilesDir();
		List<String> iniModules = getIniModules(iniDirectory);
		List<String> list = new ArrayList<String>();
		String[] modules = getZendModules();
		for (String module : modules) {
			File file = new File(directory, module);
			if (file.exists() && !iniModules.contains(file.getName().toLowerCase())) {
				list.add("zend_extension=" + file.getAbsolutePath());
			}
		}
		modules = getModules();
		for (String module : modules) {
			File file = new File(directory, module);
			if (file.exists() && !iniModules.contains(file.getName().toLowerCase())) {
				list.add("extension=" + file.getAbsolutePath());
			}
		}
		for (String row : list) {
			options.add("-d");
			options.add(row);
		}
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
			List<String> options = new ArrayList<String>();
			options.add(php.getAbsolutePath());
			options.add("-S");
			options.add(address);
			options.add("-t");
			options.add(root);
			addStartupOptions(options, fileRoot);
			process = Runtime.getRuntime().exec(
				options.toArray(new String[options.size()]), null, fileRoot
			);
			new PhpStreamReader(this, handler).execute(process.getErrorStream());
		} catch (IOException ignored) {
			if (process == null) {
				handler.sendError(ignored.getCause().getMessage());
			}
		}
	}

	private void stop() {
		if (process != null) {
			process.destroy();
			process = null;
		}
		new Process().kill(php);
	}
	
	private void status() {
		boolean running = process != null;
		String realAddress = address;
		if (process == null) {
			String[] commandLine = new Process().getCommandLine(php);
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

	public void requestRestart() {
		requestStop();
		requestStart();
	}

	void onHandlerReady() {
		status();
		if (start) {
			requestStart();
		}
	}

	void onHandlerMessage(Message message) {
		Bundle data = message.getData();
		if (data == null) {
			return;
		}
		if (data.get("action").equals("error")) {
			Intent intent = new Intent(INTENT_ACTION);
			intent.putExtra("errorLine", data.getString("message"));
			context.sendBroadcast(intent);
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
