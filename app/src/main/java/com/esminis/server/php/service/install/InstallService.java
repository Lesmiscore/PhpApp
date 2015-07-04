package com.esminis.server.php.service.install;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.esminis.model.manager.Network;
import com.esminis.server.php.Application;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.inject.Inject;

public class InstallService extends Service {

	static public final int ACTION_INSTALL = 1;
	static public final int ACTION_INSTALL_COMPLETE = 2;
	static public final int ACTION_INSTALL_ERROR = 3;

	@Inject
	protected Preferences preferences;

	@Inject
	protected Php php;

	@Inject
	protected InstallToDocumentRoot installToDocumentRoot;

	private final Messenger messenger = new Messenger(
		new Handler() {
			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
					case ACTION_INSTALL:
						try {
							message.replyTo.send(
								Message.obtain(
									null,
									install(getApplicationContext()) ? ACTION_INSTALL_COMPLETE : ACTION_INSTALL_ERROR
								)
							);
						} catch (RemoteException ignored) {}
						break;
					default:
						super.handleMessage(message);
				}
			}
		}
	);

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		((Application)getApplication()).getObjectGraph().inject(this);
	}

	private boolean install(Context context) {
		InstallHelper helper = new InstallHelper();
		if (!preferences.contains(context, Preferences.DOCUMENT_ROOT)) {
			try {
				installToDocumentRoot.install(context, true);
			} catch (Exception ignored) {}
		}
		String[] list = preferences.getInstallPaths(context);
		File moduleDirectory = php.getPhp().getParentFile();
		if (!helper.fromAssetFiles(moduleDirectory, list, context)) {
			return false;
		}
		HashMap<String, String> variables = new HashMap<>();
		variables.put("moduleDirectory", moduleDirectory.getAbsolutePath());
		helper.preprocessFile(new File(php.getPhp().getParentFile(), "odbcinst.ini"), variables);
		return true;
	}

}
