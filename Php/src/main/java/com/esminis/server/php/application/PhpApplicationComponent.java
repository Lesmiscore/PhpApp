package com.esminis.server.php.application;

import com.esminis.server.library.application.ApplicationComponent;
import com.esminis.server.php.activity.DrawerPhpFragment;
import com.esminis.server.php.server.install.InstallTaskProvider;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {PhpApplicationModule.class})
public interface PhpApplicationComponent extends ApplicationComponent {

	void inject(DrawerPhpFragment fragment);

	void inject(InstallTaskProvider provider);

}
