package com.esminis.server.library.dialog.install;

import android.app.Activity;
import android.support.annotation.StringRes;

import com.esminis.server.library.model.InstallPackage;

public interface InstallView {

	void setupOnCreate();

	void showList(InstallPackage[] list);

	void showMessage(boolean preloader, @StringRes int message, String... argument);

	void showMessageError(@StringRes int message, Throwable error, String... arguments);

	void showMessageInstall(InstallPackage model, @StringRes int message, String... arguments);

	void showMessageInstallFailed(InstallPackage model, Throwable error);

	void hideMessage();

	Activity getActivity();

}
