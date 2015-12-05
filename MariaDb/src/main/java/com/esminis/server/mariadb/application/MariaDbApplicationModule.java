package com.esminis.server.mariadb.application;

import android.os.Environment;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.activity.MainActivityHelper;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallServerTask;
import com.esminis.server.mariadb.activity.MariaDbDrawerFragment;
import com.esminis.server.mariadb.activity.MariaDbMainActivityHelper;
import com.esminis.server.mariadb.server.MariaDbInstallServerTaskProvider;
import com.esminis.server.mariadb.server.MariaDb;
import com.squareup.otto.Bus;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MariaDbApplicationModule {

	private final LibraryApplication application;

	public MariaDbApplicationModule(LibraryApplication application) {
		this.application = application;
	}

	@Provides
	@Singleton
	public ServerControl provideServerControl(
		Network network, com.esminis.server.library.model.manager.Process process, Log log,
		Preferences preferences
	) {
		return new MariaDb(
			application, network, preferences, log, process, application.getIsMainApplicationProcess()
		);
	}

	@Provides
	public InstallServerTask provideInstallTaskFactory(
		final Network network, final Preferences preferences, final ServerControl serverControl
	) {
		return new InstallServerTask(
			serverControl, preferences, network, application, MariaDbInstallServerTaskProvider.class,
			"mariadb"
		);
	}

	@Provides
	public DrawerFragment provide() {
		return new MariaDbDrawerFragment();
	}

	@Provides
	@Singleton
	public MainActivityHelper provideActivityHelper(Bus bus, Preferences preferences) {
		return new MariaDbMainActivityHelper(preferences, bus);
	}

}
