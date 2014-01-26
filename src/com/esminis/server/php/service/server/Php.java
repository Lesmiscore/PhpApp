/**
 * Copyright 2013 Tautvydas Andrikys
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.esminis.model.manager.Manager;
import com.esminis.model.manager.Process;
import com.esminis.server.php.R;
import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Preferences;

import java.io.File;
import java.io.IOException;

public class Php extends HandlerThread {
	
	static public final String INTENT_ACTION = "STATUS_SERVER_CHANGED";
	
	private static Php instance = null;
		
	private java.lang.Process process = null;
	
	private File php = null;		
	
	private Handler handler = null;
	
	private Context context = null;
	
	private String address = "";
	
	private boolean startWhenReady = false;
	
	private Preferences preferences = null;
	
	private Network network = null;

	static public Php getInstance(Context context) {
		if (instance == null) {
			instance = new Php(context);
			instance.start();
		}
		return instance;
	}
	
	public File getPhp() {
		return php;
	}
	
	public Php(Context context) {
		super("PhpServer");
		network = Manager.get(Network.class);
		preferences = Manager.get(Preferences.class);
		this.context = context.getApplicationContext();
		php = new File(context.getFilesDir() + File.separator + "php");		
		address = getIPAddress() + ":" + preferences.getString(context, Preferences.PORT);
	}

	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		if (getLooper() == null) {
			return;
		}
		handler = new Handler(getLooper()) {

			@Override
			public void handleMessage(Message message) {
				Bundle data = message.getData();
				if (data != null && data.get("action").equals("error")) {
					Intent intent = new Intent(INTENT_ACTION);		
					intent.putExtra("errorLine", data.getString("message"));
					context.sendBroadcast(intent);
				} else {
					if (data != null && data.get("action").equals("start")) {
						address = getIPAddress() + ":" + data.getString("port");
						serverStart(data.getString("documentRoot"));
					} else if (data != null && data.get("action").equals("stop")) {
						serverStop();
					}
					serverStatus();
				}				
			}
			
		};
		serverStatus();
		if (startWhenReady) {
			sendAction("start");
		}
	}
	
	private String getIPAddress() {
		int position = network.getPosition(preferences.getString(context, Preferences.ADDRESS));
		return position == -1 ? "0.0.0.0" : network.get(position).address;
	}
	
	private void serverStart(String documentRoot) {
		if (process == null) {
			File fileDocumentRoot = new File(documentRoot);
			try {
				File file = new File(fileDocumentRoot, "php.ini");
				process = Runtime.getRuntime().exec(
					new String[] {
						php.getAbsolutePath(), "-S", address, "-t", documentRoot, 
							"-c", file.exists() ? file.getAbsolutePath() : documentRoot
					}, null, fileDocumentRoot
				);
				new StreamReader().execute(process.getErrorStream(), this);
			} catch (IOException ignored) {
				if (process == null) {
					if (fileDocumentRoot.isDirectory()) {
						sendErrorLine(ignored.getCause().getMessage());
					} else {
						sendErrorLine(context.getString(R.string.error_document_root_does_not_exist));
					}
				}
			}
		}
	}

	private void serverStop() {
		if (process != null) {
			process.destroy();
			process = null;
		}
		new Process().kill(php);
	}
	
	private void serverStatus() {
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
	
	private void sendMessage(String action, Bundle bundle) {
		if (handler != null) {
			bundle.putString("action", action);
			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);
		}
	}
	
	public void sendErrorLine(String error) {
		if (handler != null) {
			Bundle bundle = new Bundle();
			bundle.putString("message", error);
			sendMessage("error", bundle);
		}
	}
	
	public void sendAction(String action) {
		if (handler != null) {
			Bundle bundle = new Bundle();			
			bundle.putString(
				Preferences.DOCUMENT_ROOT, 
				preferences.getString(context, Preferences.DOCUMENT_ROOT)
			);
			bundle.putString(
				Preferences.PORT, preferences.getString(context, Preferences.PORT)
			);
			sendMessage(action, bundle);
		}
	}
	
	public void startWhenReady() {
		if (handler == null) {
			startWhenReady = true;
		} else {
			sendAction("start");
		}
	}
	
}
