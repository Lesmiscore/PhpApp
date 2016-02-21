package com.esminis.server.library.application;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.activity.main.MainPresenter;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.installpackage.InstallerPackage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LibraryApplicationServerModule {

	@Singleton
	@Provides
	public ServerControl provideServerControl() {
		throw new RuntimeException("Not implemented");
	}

	@Provides
	public InstallerPackage provideInstallerPackage() {
		throw new RuntimeException("Not implemented");
	}

	@Provides
	public DrawerFragment provideDrawerFragment() {
		throw new RuntimeException("Not implemented");
	}

	@Provides
	public MainPresenter provideMainPresenter() {
		throw new RuntimeException("Not implemented");
	}

}
