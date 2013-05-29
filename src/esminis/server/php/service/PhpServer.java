package esminis.server.php.service;

import android.content.Context;
import android.content.Intent;
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
		new Process().killIfFound(php);
	}

	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		handler = new Handler(getLooper()) {

			@Override
			public void handleMessage(Message message) {
				if (message.getData().get("action").equals("start")) {
					serverStart();
				} else if (message.getData().get("action").equals("stop")) {
					serverStop();
				}
				serverStatus();
			}
			
		};
		serverStatus();
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
	
	private String getAddress() {
		return getIPAddress() + ":8080";
	}
	
	private void serverStart() {
		if (process == null) {
			try {
				process = Runtime.getRuntime().exec(
					new String[] {
						php.getAbsolutePath(), "-S", getAddress(), "-t", "/sdcard/www"
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
	}
	
	private void serverStatus() {
		boolean running = process != null;
		Intent intent = new Intent(INTENT_ACTION);		
		intent.putExtra("running", running);
		if (running) {
			intent.putExtra("address", getAddress());
		}
		context.sendBroadcast(intent);
	}
	
}
