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
package com.esminis.server.php.service.server.install;

import android.content.Context;

import com.esminis.server.php.Application;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.background.BackgroundServiceTaskProvider;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class InstallTaskProvider implements BackgroundServiceTaskProvider {

	@Inject
	protected Preferences preferences;

	@Inject
	protected ServerControl serverControl;

	@Inject
	protected InstallToDocumentRoot installToDocumentRoot;

	@Override
	public Observable<Void> createTask(final Context context) {
		((Application)context.getApplicationContext()).getObjectGraph().inject(this);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				InstallHelper helper = new InstallHelper();
				if (!preferences.contains(context, Preferences.DOCUMENT_ROOT)) {
					try {
						installToDocumentRoot.install(context, true);
					} catch (Exception ignored) {}
				}
				String[] list = preferences.getInstallPaths(context);
				final File moduleDirectory = serverControl.getBinary().getParentFile();
				if (!helper.fromAssetFiles(moduleDirectory, list, context)) {
					subscriber.onError(new Exception("Install failed"));
					return;
				}
				HashMap<String, String> variables = new HashMap<>();
				variables.put("moduleDirectory", moduleDirectory.getAbsolutePath());
				helper.preprocessFile(new File(moduleDirectory, "odbcinst.ini"), variables);
				subscriber.onCompleted();
			}
		});
	}

}