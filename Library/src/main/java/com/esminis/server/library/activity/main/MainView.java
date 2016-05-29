/**
 * Copyright 2016 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
