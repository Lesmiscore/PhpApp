package com.esminis.server.php.server.install;

import android.content.Context;

import com.esminis.server.library.model.manager.InstallPackageManager;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.installpackage.InstallHelper;
import com.esminis.server.library.service.server.installpackage.InstallerPackage;

import java.io.File;
import java.util.HashMap;

public class InstallerPackagePhp extends InstallerPackage {

	private final ServerControl serverControl;
	private final InstallHelper helper = new InstallHelper();
	private final InstallToDocumentRoot installToDocumentRoot;
	private final InstallPackageManager installPackageManager;

	public InstallerPackagePhp(
		ServerControl serverControl, InstallToDocumentRoot installToDocumentRoot,
		InstallPackageManager installPackageManager, Preferences preferences
	) {
		super(preferences);
		this.serverControl = serverControl;
		this.installToDocumentRoot = installToDocumentRoot;
		this.installPackageManager = installPackageManager;
	}

	@Override
	protected void onInstallComplete(Context context) throws Throwable {
		installOdbcInstIni(context, serverControl.getBinaryDirectory());
		if (installPackageManager.getInstalled() == null) {
			installToDocumentRoot.install(context, true);
		}
	}

	private void installOdbcInstIni(Context context, File directory) throws Throwable {
		final File file = new File(directory, "odbcinst.ini");
		final HashMap<String, String> variables = new HashMap<>();
		variables.put("moduleDirectory", directory.getAbsolutePath());
		helper.fromAssetFile(file, "odbcinst.ini", context);
		helper.preprocessFile(file, variables);
	}

}
