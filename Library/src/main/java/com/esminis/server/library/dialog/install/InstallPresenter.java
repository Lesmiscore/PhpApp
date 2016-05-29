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
package com.esminis.server.library.dialog.install;

import com.esminis.server.library.dialog.DialogPresenter;
import com.esminis.server.library.model.InstallPackage;

public interface InstallPresenter extends DialogPresenter<InstallView> {

	void downloadList();

	void install(InstallPackage model);

	InstallPackage getInstalled();

	boolean isInstalling();

}
