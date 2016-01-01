package com.esminis.server.mariadb.application;

import com.esminis.server.library.activity.main.MainPresenter;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallServerTask;
import com.esminis.server.mariadb.activity.MariaDBPresenterImpl;
import com.esminis.server.mariadb.server.MariaDb;
import com.esminis.server.mariadb.server.MariaDbInstallServerTaskProvider;

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
	public MainPresenter provideMainPresenter(MariaDBPresenterImpl implementation) {
		return implementation;
	}


}
