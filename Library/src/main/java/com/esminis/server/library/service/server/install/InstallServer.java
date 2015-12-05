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
package com.esminis.server.library.service.server.install;

import com.esminis.server.library.application.LibraryApplicationComponent;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.activity.MainActivity;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InstallServer {

	private final ServerControl serverControl;
	private final Preferences preferences;

	private OnInstallServerListener listener = null;
	private Subscription install = null;
	private final Object lock = new Object();
	private final LibraryApplicationComponent component;

	public InstallServer(
		Preferences preferences, ServerControl serverControl, LibraryApplicationComponent component
	) {
		this.preferences = preferences;
		this.serverControl = serverControl;
		this.component = component;
	}

	public void install(MainActivity activity) {
		synchronized (lock) {
			this.listener = activity;
			if (install != null) {
				return;
			}
		}
		final File file = serverControl.getBinary();
		if (file.isFile() && preferences.getIsInstalled(activity)) {
			if (!preferences.getIsSameBuild(activity)) {
				if (listener != null) {
					listener.OnInstallNewVersionRequest(this);
				}
			} else {
				finish(true);
			}
		} else {
			start();
		}
	}

	public void installNewVersionConfirmed() {
		start();
	}

	public void installFinish() {
		finish(true);
	}

	private void start() {
		synchronized (lock) {
			if (install != null) {
				return;
			}
			install = Observable.create(component.getInstallTask())
				.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(
					new Subscriber<Void>() {
						@Override
						public void onCompleted() {
							finish(true);
						}

						@Override
						public void onError(Throwable e) {
							finish(false);
						}

						@Override
						public void onNext(Void dummy) {}
					}
				);
		}
	}

	private void finish(boolean success) {
		final OnInstallServerListener listener;
		synchronized (lock) {
			if (install != null) {
				install.unsubscribe();
				install = null;
			}
			listener = this.listener;
			this.listener = null;
		}
		if (listener != null) {
			listener.OnInstallEnd(success);
		}
	}
	
}
