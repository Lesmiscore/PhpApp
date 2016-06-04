/**
 * Copyright 2016 Tautvydas Andrikys
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
import com.esminis.server.library.activity.external.IntentPresenter;
import com.esminis.server.library.activity.main.MainPresenter;
import com.esminis.server.library.model.manager.InstallPackageManager;
import com.esminis.server.library.model.manager.ProductLicenseManager;
import com.esminis.server.library.service.AutoStart;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.ServerNotificationService;
import com.esminis.server.library.service.server.installpackage.InstallerPackage;
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

	InstallerPackage getInstallerPackage();

	InstallPackageManager getInstallPackageManager();

	ProductLicenseManager getProductLicenseManager();

	MainPresenter getMainPresenter();

	IntentPresenter getIntentPresenter();

}

