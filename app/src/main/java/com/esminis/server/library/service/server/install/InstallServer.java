package com.esminis.server.library.service.server.install;

import android.app.Activity;

import com.esminis.server.php.MainActivity;

public interface InstallServer {

	void continueInstall(Activity activity, boolean confirm);

	void install(MainActivity activity);

}
