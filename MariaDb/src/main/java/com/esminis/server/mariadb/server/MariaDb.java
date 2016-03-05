package com.esminis.server.mariadb.server;

import android.content.Context;

import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.*;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.ServerNotification;

import java.io.File;
import java.io.IOException;
import java.lang.Process;

public class MariaDb extends ServerControl {

	final MariaDbServerLauncher launcher;

	public MariaDb(
		LibraryApplication context, Network network, Preferences preferences,
		Log log, com.esminis.server.library.model.manager.Process managerProcess, boolean mainProcess,
		ServerNotification serverNotification
	) {
		super(
			"mysqld", context, network, preferences, log, managerProcess, mainProcess, serverNotification
		);
		launcher = new MariaDbServerLauncher(managerProcess);
	}

	@Override
	protected Process start(File root, String address) throws IOException {
		return launcher.start(getBinary(), address, root, context);
	}

	@Override
	protected void stop(java.lang.Process process) {
		launcher.stop(process, getBinary());
	}

	@Override
	public String getServerRunningLabel(Context context, String address) {
		return String.format(
			context.getString(com.esminis.server.library.R.string.server_running),
			"<b>" + address + "</b>"
		);
	}

}
