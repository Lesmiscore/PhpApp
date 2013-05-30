package esminis.server.php.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;

public class PhpServer extends HandlerThread {
	
	static public String PREFERENCES_DOCUMENT_ROOT = "documentRoot";
	
	static public String PREFERENCES_PORT = "port";
	
	static public String PREFERENCES_START_ON_BOOT = "startOnBoot";
	
	static public final String INTENT_ACTION = "STATUS_SERVER_CHANGED";
	
	private static PhpServer instance = null;
		
	private java.lang.Process process = null;
	
	private File php = null;		
	
	private Handler handler = null;
	
	private Context context = null;
	
	private String address = "";
	
	private boolean startWhenReady = false;

	static public PhpServer getInstance(Context context) {
		if (instance == null) {
			instance = new PhpServer(context);
			instance.start();
		}
		return instance;
	}
	
	public PhpServer(Context context) {
		super("PhpServer");
		this.context = context.getApplicationContext();
		php = new File(context.getFilesDir() + File.separator + "php");		
		try {
			new Install().fromAsset(php, "php", context);
			php.setExecutable(true);
		} catch (IOException ex) {}
		address = getIPAddress() + ":" + PreferenceManager
			.getDefaultSharedPreferences(context).getString(PREFERENCES_PORT, "8080");
	}

	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		handler = new Handler(getLooper()) {

			@Override
			public void handleMessage(Message message) {
				Bundle data = message.getData();
				if (data.get("action").equals("start")) {
					address = getIPAddress() + ":" + data.getString("port");
					serverStart(data.getString("documentRoot"));
				} else if (data.get("action").equals("stop")) {
					serverStop();
				}
				serverStatus();
			}
			
		};
		serverStatus();
		if (startWhenReady) {
			sendAction("start");
		}
	}
	
	public Handler getHandler() {
		return handler;
	}
	
	public static String getIPAddress() {
		try {
			List<NetworkInterface> interfaces = Collections.list(
				NetworkInterface.getNetworkInterfaces()
			);
			for (NetworkInterface iface : interfaces) {
				List<InetAddress> addresses = Collections.list(
					iface.getInetAddresses()
				);
				for (InetAddress address : addresses) {
					if (!address.isLoopbackAddress()) {
						String host = address.getHostAddress().toUpperCase();
						if (InetAddressUtils.isIPv4Address(host)) {
							return host;
						}
					}
				}
			}
		} catch (Exception ex) {}
		return "127.0.0.1";
	}
	
	private void serverStart(String documentRoot) {
		if (process == null) {
			try {
				process = Runtime.getRuntime().exec(
					new String[] {
						php.getAbsolutePath(), "-S", address, "-t", documentRoot
					}
				);
			} catch (IOException ex) {}
		}
	}

	private void serverStop() {
		if (process != null) {
			process.destroy();
			process = null;
		}
		new Process().killIfFound(php);
	}
	
	private void serverStatus() {
		boolean running = process != null || new Process().getIsRunning(php);
		Intent intent = new Intent(INTENT_ACTION);		
		intent.putExtra("running", running);
		if (running) {
			intent.putExtra("address", address);
		}
		context.sendBroadcast(intent);
	}
	
	public void sendAction(String action) {		
		if (handler != null) {
			SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
			Bundle bundle = new Bundle();
			bundle.putString("action", action);
			bundle.putString(
				PREFERENCES_DOCUMENT_ROOT, 
				preferences.getString(PREFERENCES_DOCUMENT_ROOT, "")
			);
			bundle.putString(
				PREFERENCES_PORT, preferences.getString(PREFERENCES_PORT, "")
			);
			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);
		}
	}
	
	public void startWhenReady() {
		if (handler == null) {
			startWhenReady = true;
		} else {
			sendAction("start");
		}
	}
	
	public SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
}
