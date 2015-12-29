package com.esminis.server.library.application;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.activity.DrawerFragmentHelper;
import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.library.activity.MainPresenterImpl;
import com.esminis.server.library.dialog.about.AboutPresenter;
import com.esminis.server.library.dialog.about.AboutPresenterImpl;
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

	void inject(MainPresenterImpl mainPresenter);

	void inject(DrawerFragment fragment);

	void inject(ServerNotificationService service);

	void inject(ServerTaskProvider taskProvider);

	DrawerFragmentHelper getDrawerFragmentHelper();

	ServerControl getServerControl();

	DrawerFragment getDrawerFragment();

	InstallServerTask getInstallTask();

	ProductLicenseManager getProductLicenseManager();

}

