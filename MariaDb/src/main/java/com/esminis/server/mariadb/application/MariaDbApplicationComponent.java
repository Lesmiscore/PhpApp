package com.esminis.server.mariadb.application;

import com.esminis.server.library.application.LibraryApplicationComponent;
import com.esminis.server.library.application.LibraryApplicationModule;
import com.esminis.server.mariadb.server.InstallServerMariaDbTaskProvider;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {MariaDbApplicationModule.class, LibraryApplicationModule.class})
public interface MariaDbApplicationComponent extends LibraryApplicationComponent {

	void inject(InstallServerMariaDbTaskProvider taskProvider);

}
