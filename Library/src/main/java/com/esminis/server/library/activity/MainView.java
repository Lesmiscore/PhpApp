package com.esminis.server.library.activity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

	CharSequence getLog();

	void setStatusLabel(CharSequence label);

	boolean createMenu(MenuInflater inflater, Menu menu);

	boolean onMenuItemSelected(MenuItem item);

	void setMessage(boolean containerVisible, boolean preloader, boolean button, String message);

	void showAbout();

	void showDocumentRootChooser(File root);

	void showInstallNewVersionRequest(CharSequence message);

	void closeDialog();

	void syncDrawer();

	void showMainContent();

	void showButton(int button);

}
