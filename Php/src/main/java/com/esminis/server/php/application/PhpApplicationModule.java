package com.esminis.server.php.application;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.activity.main.MainPresenter;
import com.esminis.server.library.activity.main.MainPresenterImpl;
import com.esminis.server.library.application.*;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallServerTask;
import com.esminis.server.php.activity.DrawerPhpFragment;
import com.esminis.server.php.server.Php;
import com.esminis.server.php.server.install.InstallServerPhpTaskProvider;

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
		return new Php(network, process, preferences, log, application);
	}

	@Provides
	public InstallServerTask provideInstallTask(
		final Network network, final Preferences preferences, final ServerControl serverControl
	) {
		return new InstallServerTask(
			serverControl, preferences, network, application, InstallServerPhpTaskProvider.class, "www"
		);
	}

	@Provides
	public DrawerFragment provideDrawerFragment(DrawerPhpFragment implementation) {
		return implementation;
	}

	@Provides
	public MainPresenter provideMainPresenter(MainPresenterImpl implementation) {
		return implementation;
	}

}
