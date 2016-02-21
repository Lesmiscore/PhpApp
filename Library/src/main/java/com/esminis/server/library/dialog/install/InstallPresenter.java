package com.esminis.server.library.dialog.install;

import com.esminis.server.library.dialog.DialogPresenter;
import com.esminis.server.library.model.InstallPackage;

import java.util.Observable;

public interface InstallPresenter extends DialogPresenter<InstallView> {

	void downloadList();

	void install(InstallPackage model);

	InstallPackage getInstalled();

	boolean isInstalling();

}
