package esminis.server.php.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;

public class PhpServer extends HandlerThread {
	
	static public final String INTENT_ACTION = "STATUS_SERVER_CHANGED";
	
	private static PhpServer instance = null;
		
	private java.lang.Process process = null;
	
	private File php = null;		
	
	private Handler handler = null;
	
	private Context context = null;
	
	private String address = "";
	
	private boolean startWhenReady = false;
	
	private Preferences preferences = null;
	
	private Network network = null;

	static public PhpServer getInstance(Context context) {
		if (instance == null) {
			instance = new PhpServer(context);
			instance.start();
		}
		return instance;
	}
	
	public PhpServer(Context context) {
		super("PhpServer");
		network = new Network();
		preferences = new Preferences(context);
		this.context = context.getApplicationContext();
		php = new File(context.getFilesDir() + File.separator + "php");		
		try {
			new Install().fromAssetFile(php, "php", context);
			php.setExecutable(true);
		} catch (IOException ex) {}
		address = getIPAddress() + ":" + preferences.getString(Preferences.PORT);
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
	
	private String getIPAddress() {
		return network.getAddress(
			network.getPosition(preferences.getString(Preferences.ADDRESS))
		);
	}
	
	private void serverStart(String documentRoot) {
		if (process == null) {
			try {
				File file = new File(documentRoot + File.separator + "php.ini");
				process = Runtime.getRuntime().exec(
					new String[] {
						php.getAbsolutePath(), "-S", address, "-t", documentRoot, 
							"-c", file.exists() ? file.getAbsolutePath() : documentRoot
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
			Bundle bundle = new Bundle();
			bundle.putString("action", action);
			bundle.putString(
				Preferences.DOCUMENT_ROOT, 
				preferences.getString(Preferences.DOCUMENT_ROOT)
			);
			bundle.putString(
				Preferences.PORT, preferences.getString(Preferences.PORT)
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
	
}
