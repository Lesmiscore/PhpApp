package com.esminis.server.php.application;

import android.app.Activity;
import android.os.Environment;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.application.*;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallServer;
import com.esminis.server.php.activity.DrawerPhpFragment;
import com.esminis.server.php.server.Php;
import com.esminis.server.library.service.server.install.InstallServerTask;
import com.esminis.server.php.server.install.InstallServerPhpTaskProvider;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PhpApplicationModule {

	private final LibraryApplication application;

	public PhpApplicationModule(LibraryApplication application) {
		this.application = application;
	}

	@Provides
	@Singleton
	public ServerControl provideServerControl(
		Network network, com.esminis.server.library.model.manager.Process process, Log log,
		Preferences preferences
	) {
		return new Php(
			network, process, preferences, log, application, application.getIsMainApplicationProcess()
		);
	}

	@Provides
	@Singleton
	public InstallServer.InstallTaskFactory provideInstallTaskFactory(
		final Network network, final Preferences preferences, final ServerControl serverControl
	) {
		return new InstallServer.InstallTaskFactory() {
			@Override
			public com.esminis.server.library.service.server.install.InstallServerTask create(
				Activity activity, InstallServer.OnInstallListener listener
			) {
				return new InstallServerTask(
					serverControl, listener, preferences, network, activity, InstallServerPhpTaskProvider.class,
					new File(Environment.getExternalStorageDirectory(), "www")
				);
			}
		};
	}

	@Provides
	public DrawerFragment provideDrawerFragment() {
		return new DrawerPhpFragment();
	}

}
