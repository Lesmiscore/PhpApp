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
package com.esminis.server.php.server.install;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;
import com.esminis.server.library.service.server.install.InstallHelper;
import com.esminis.server.php.R;
import com.esminis.server.php.application.PhpApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class InstallServerPhpTaskProvider implements BackgroundServiceTaskProvider {

	@Inject
	protected Preferences preferences;

	@Inject
	protected ServerControl serverControl;

	@Inject
	protected InstallToDocumentRoot installToDocumentRoot;

	@Override
	public Observable<Void> createTask(final Context context, Bundle data) {
		((PhpApplication)context.getApplicationContext()).getComponent().inject(this);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				try {
					if (!preferences.getIsInstalled(context)) {
						installToDocumentRoot.install(context, true);
					}
					final InstallHelper helper = new InstallHelper();
					final File moduleDirectory = serverControl.getBinary().getParentFile();
					final HashMap<String, String> variables = new HashMap<>();
					helper.fromAssetFiles(moduleDirectory, getInstallPaths(context), context);
					variables.put("moduleDirectory", moduleDirectory.getAbsolutePath());
					helper.preprocessFile(new File(moduleDirectory, "odbcinst.ini"), variables);
					subscriber.onCompleted();
				} catch (Throwable e) {
					subscriber.onError(e);
				}
			}
		});
	}

	public String[] getInstallPaths(Context context) {
		List<String> list = new ArrayList<>();
		Collections.addAll(list, context.getResources().getStringArray(R.array.install_binaries));
		Collections.addAll(list, getInstallModules(context));
		String pathBinaries = "bin/" + (Build.CPU_ABI.toLowerCase().startsWith("x86") ? "x86" : "arm")
			+ "/";
		for (int i = 0; i < list.size(); i++) {
			list.set(i, pathBinaries + list.get(i));
		}
		Collections.addAll(list, context.getResources().getStringArray(R.array.install_files));
		return list.toArray(new String[list.size()]);
	}

	private String[] getInstallModules(Context context) {
		List<String> modules = new ArrayList<>();
		String[] list = context.getResources().getStringArray(R.array.modules);
		for (int i = 0; i < list.length; i += 3) {
			modules.add(list[i] + ".so");
		}
		return modules.toArray(new String[modules.size()]);
	}

}
