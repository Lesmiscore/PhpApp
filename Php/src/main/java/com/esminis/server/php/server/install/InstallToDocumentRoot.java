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
import android.os.Environment;

import com.esminis.server.library.ErrorWithMessage;
import com.esminis.server.php.R;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class InstallToDocumentRoot {

	private final InstallHelper helper = new InstallHelper();
	private final Object lock = new Object();
	private boolean installInProgress = false;

	@Inject
	public InstallToDocumentRoot() {}

	void install(Context context, boolean ifNoDirectory) throws Exception {
		File file = new File(Environment.getExternalStorageDirectory(), "www");
		File tempDirectory = new File(context.getExternalFilesDir(null), "tmp");
		if (!tempDirectory.isDirectory() && !tempDirectory.mkdir()) {
			tempDirectory = file;
		}
		if (ifNoDirectory && file.isDirectory()) {
			return;
		}
		if (!file.isDirectory() && !file.mkdir()) {
			throw new ErrorWithMessage(R.string.error_cannot_create_directory);
		}
		helper.fromAssetDirectory(file, "www", context);
		HashMap<String, String> variables = new HashMap<>();
		variables.put("tempDirectory", tempDirectory.getAbsolutePath());
		helper.preprocessFile(new File(file, "php.ini"), variables);
	}

	public Observable<Void> installOnBackground(final Context context) {
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				final boolean wait;
				synchronized (lock) {
					wait = installInProgress;
					installInProgress = true;
				}
				if (wait) {
					installWait();
					final boolean installComplete;
					synchronized (lock) {
						installComplete = !installInProgress;
					}
					if (!installComplete) {
						subscriber.onError(new ErrorWithMessage(R.string.error_operation_failed));
						return;
					}
				} else {
					try {
						install(context, false);
					} catch (Exception e) {
						subscriber.onError(e);
						return;
					} finally {
						synchronized (lock) {
							installInProgress = false;
						}
					}
				}
				subscriber.onCompleted();
			}
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
	}

	private void installWait() {
		for (int i = 0; i < 100; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {}
		}
	}

}
