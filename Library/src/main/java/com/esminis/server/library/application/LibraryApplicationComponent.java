package com.esminis.server.library.application;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.activity.DrawerFragmentHelper;
import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.library.dialog.About;
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

	void inject(MainActivity activity);

	void inject(About dialog);

	void inject(DrawerFragment fragment);

	void inject(ServerNotificationService service);

	void inject(ServerTaskProvider taskProvider);

	DrawerFragmentHelper getDrawerFragmentHelper();

	ServerControl getServerControl();

	DrawerFragment getDrawerFragment();

	InstallServerTask getInstallTask();

}

