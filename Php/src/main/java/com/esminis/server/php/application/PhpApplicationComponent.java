package com.esminis.server.php.application;

import com.esminis.server.library.application.LibraryApplicationComponent;
import com.esminis.server.library.application.LibraryApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {PhpApplicationModule.class, LibraryApplicationModule.class})
public interface PhpApplicationComponent extends LibraryApplicationComponent {

}
