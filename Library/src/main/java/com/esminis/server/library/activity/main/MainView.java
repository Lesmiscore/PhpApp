package com.esminis.server.library.activity.main;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.esminis.server.library.dialog.install.InstallPresenterImpl;
import com.esminis.server.library.model.InstallPackage;
import com.esminis.server.library.model.Network;

import java.io.File;
import java.util.List;

public interface MainView {

	int BUTTON_NONE = 0;
	int BUTTON_START = 1;
	int BUTTON_STOP = 2;

	void setDocumentRoot(String documentRoot);

	void setPort(String port, boolean valid);

	void setLog(CharSequence log);

	void setServerInterfaces(List<Network> list, int selectedPosition);

	void setInstallPackages(InstallPackage installed, InstallPackage newest);

	CharSequence getLog();

	void setStatusLabel(CharSequence label);

	boolean createMenu(MenuInflater inflater, Menu menu);

	boolean onMenuItemSelected(MenuItem item);

	void setMessage(
		boolean preloaderBackground, boolean preloader, String buttonTitle, String message
	);

	void showAbout();

	void showDocumentRootChooser(File root);

	void showInstall(InstallPresenterImpl presenter);

	void closeDialog();

	void syncDrawer();

	void showMainContent();

	void showButton(int button);

}
