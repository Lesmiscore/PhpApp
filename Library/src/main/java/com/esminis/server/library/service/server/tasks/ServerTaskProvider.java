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
package com.esminis.server.library.service.server.tasks;

import android.content.Context;

import com.esminis.server.library.application.Application;
import com.esminis.server.library.service.background.BackgroundServiceTaskProvider;

import rx.Observable;
import rx.Subscriber;

abstract class ServerTaskProvider implements BackgroundServiceTaskProvider {

	@Override
	public Observable<Void> createTask(Context context) {
		((Application)context.getApplicationContext()).getObjectGraph().inject(this);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				execute();
				subscriber.onCompleted();
			}
		});
	}

	abstract protected void execute();

}
