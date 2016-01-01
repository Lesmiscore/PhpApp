package com.esminis.server.library.application;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.activity.main.MainPresenter;
import com.esminis.server.library.model.manager.ProductLicenseManager;
import com.esminis.server.library.service.AutoStart;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.ServerNotificationService;
import com.esminis.server.library.service.server.install.InstallServerTask;
import com.esminis.server.library.service.server.tasks.ServerTaskProvider;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {LibraryApplicationModule.class, LibraryApplicationServerModule.class})
public interface LibraryApplicationComponent {

	void inject(AutoStart receiver);

	void inject(DrawerFragment fragment);

	void inject(ServerNotificationService service);

	void inject(ServerTaskProvider taskProvider);

	ServerControl getServerControl();

	DrawerFragment getDrawerFragment();

	InstallServerTask getInstallTask();

	ProductLicenseManager getProductLicenseManager();

	MainPresenter getMainPresenter();

}

