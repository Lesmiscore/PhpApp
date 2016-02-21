package com.esminis.server.mariadb.server;

import android.content.Context;

import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.installpackage.InstallerPackage;

import java.io.File;
import java.io.IOException;

public class InstallerPackageMariaDb extends InstallerPackage {

	private final ServerControl serverControl;
	private final Preferences preferences;

	public InstallerPackageMariaDb(Preferences preferences, ServerControl serverControl) {
		this.serverControl = serverControl;
		this.preferences = preferences;
	}

	@Override
	protected void onInstallComplete(Context context) throws Throwable {
		if (!(serverControl instanceof MariaDb)) {
			throw new IOException("Invalid server control");
		}
		((MariaDb)serverControl).launcher.initializeDataDirectory(
			context, serverControl.getBinary(),
			new File(preferences.getString(context, Preferences.DOCUMENT_ROOT))
		);
	}

}
