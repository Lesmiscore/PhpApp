package com.esminis.server.php.application;

import android.app.Fragment;

import com.esminis.server.library.model.manager.*;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallServer;
import com.esminis.server.php.activity.DrawerPhpFragment;
import com.esminis.server.php.server.Php;
import com.esminis.server.php.server.install.InstallServerPhp;

public class Application extends com.esminis.server.library.application.Application {

	@Override
	protected ServerFactory createServerFactory() {
		return new ServerFactory() {
			@Override
			public ServerControl createControl(
				Network network, com.esminis.server.library.model.manager.Process process, Log log,
				Preferences preferences
			) {
				return new Php(
					network, process, preferences, log, Application.this, getIsMainApplicationProcess()
				);
			}

			@Override
			public InstallServer createInstall(
				Network network, Preferences preferences, ServerControl serverControl
			) {
				return new InstallServerPhp(network, preferences, serverControl);
			}

		};
	}

	@Override
	protected Object createApplicationModule() {
		return new ApplicationModule();
	}

	@Override
	public Class<? extends Fragment> getMenuFragmentClass() {
		return DrawerPhpFragment.class;
	}

}
