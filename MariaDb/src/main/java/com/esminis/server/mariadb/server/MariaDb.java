package com.esminis.server.mariadb.server;

import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.*;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;

import java.io.File;
import java.io.IOException;
import java.lang.Process;

public class MariaDb extends ServerControl {

	private final MariaDbServerLauncher launcher;

	public MariaDb(
		LibraryApplication context, Network network, Preferences preferences,
		Log log, com.esminis.server.library.model.manager.Process managerProcess, boolean mainProcess
	) {
		super("mysqld", context, network, preferences, log, managerProcess, mainProcess);
		launcher = new MariaDbServerLauncher(managerProcess);
	}

	@Override
	protected Process start(File root, String address) throws IOException {
		return launcher.start(getBinary(), address, root, isKeepRunning(), context);
	}

}
