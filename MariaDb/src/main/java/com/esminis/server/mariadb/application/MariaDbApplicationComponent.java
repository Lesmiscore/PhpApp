package com.esminis.server.mariadb.application;

import com.esminis.server.library.application.LibraryApplicationComponent;
import com.esminis.server.library.application.LibraryApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {MariaDbApplicationModule.class, LibraryApplicationModule.class})
public interface MariaDbApplicationComponent extends LibraryApplicationComponent {

}
