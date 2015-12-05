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

import com.esminis.server.library.model.manager.ProductLicenseManager;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallServer;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LibraryApplicationModule {

	protected final LibraryApplication application;

	public LibraryApplicationModule(LibraryApplication application) {
		this.application = application;
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
	public InstallServer provideInstallServer(Preferences preferences, ServerControl serverControl) {
		return new InstallServer(preferences, serverControl, application.getComponent());
	}

}
