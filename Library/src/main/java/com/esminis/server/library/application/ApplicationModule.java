/**
 * Copyright 2015 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esminis.server.library.application;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.activity.DrawerFragmentHelper;
import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.library.dialog.About;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.model.manager.ProductLicenseManager;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.ServerNotificationService;
import com.esminis.server.library.service.server.install.InstallServer;
import com.esminis.server.library.service.server.tasks.RestartIfRunningServerTaskProvider;
import com.esminis.server.library.service.server.tasks.RestartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StatusServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StopServerTaskProvider;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects = {
	Application.class, MainActivity.class, DrawerFragment.class,
	ServerNotificationService.class,
	StartServerTaskProvider.class, StopServerTaskProvider.class, StatusServerTaskProvider.class,
	RestartIfRunningServerTaskProvider.class, RestartServerTaskProvider.class, About.class,
	ServerControl.class, DrawerFragmentHelper.class
})
public class ApplicationModule {

	private final Application application;
	private final Application.ServerFactory serverFactory;

	public ApplicationModule(Application application) {
		this.application = application;
		this.serverFactory = application.createServerFactory();
	}

	@Provides
	@Singleton
	public Bus provideBus() {
		return new Bus();
	}

	@Provides
	@Singleton
	public ProductLicenseManager provideProductLicenseManager() {
		return new ProductLicenseManager(application);
	}

	@Provides
	@Singleton
	public ServerControl provideServerControl(
		Network network, com.esminis.server.library.model.manager.Process process, Log log,
		Preferences preferences
	) {
		return serverFactory.createControl(network, process, log, preferences);
	}

	@Provides
	@Singleton
	public InstallServer provideInstallServer(
		Network network, Preferences preferences, ServerControl serverControl
	) {
		return serverFactory.createInstall(network, preferences, serverControl);
	}

}
