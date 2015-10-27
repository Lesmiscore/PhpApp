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
package com.esminis.server.php;

import com.esminis.model.manager.Network;
import com.esminis.model.manager.ProductLicenseManager;
import com.esminis.dialog.About;
import com.esminis.server.php.model.manager.Log;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.ServerNotificationService;
import com.esminis.server.php.service.background.install.InstallTaskProvider;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.service.server.PhpStartup;
import com.esminis.server.php.service.server.tasks.RestartIfRunningServerTaskProvider;
import com.esminis.server.php.service.server.tasks.RestartServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StartServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StatusServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StopServerTaskProvider;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects = {
	Application.class, MainActivity.class, Php.class, Preferences.class, DrawerFragment.class,
	ServerNotificationService.class, InstallTaskProvider.class,
	StartServerTaskProvider.class, StopServerTaskProvider.class, StatusServerTaskProvider.class,
	RestartIfRunningServerTaskProvider.class, RestartServerTaskProvider.class, About.class
})
public class ApplicationModule {

	private Application application;

	public ApplicationModule(Application application) {
		this.application = application;
	}

	@Provides
	@Singleton
	public Php providePhp(
		Network network, com.esminis.model.manager.Process process, PhpStartup startup, Log log,
		Preferences preferences
	) {
		return new Php(
			network, process, startup, preferences, log, application,
			application.getIsMainApplicationProcess()
		);
	}

	@Provides
	@Singleton
	public Preferences providePreferences() {
		return new com.esminis.server.php.model.manager.Preferences();
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

}
