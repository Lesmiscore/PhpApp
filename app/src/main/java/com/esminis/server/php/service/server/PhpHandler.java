/**
 * Copyright 2014 Tautvydas Andrikys
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
package com.esminis.server.php.service.server;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.esminis.server.php.model.manager.Preferences;

@Singleton
public class PhpHandler extends HandlerThread {

	private Handler handler = null;
	private final Context context;
	private final Preferences preferences;
	private final Php php;

	@Inject
	public PhpHandler(Context context, Php php, Preferences preferences) {
		super("PhpServer");
		this.preferences = preferences;
		this.context = context;
		this.php = php;
		start();
	}

	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		if (getLooper() == null) {
			return;
		}
		handler = new Handler(getLooper()) {

			@Override
			public void handleMessage(Message message) {
				php.onHandlerMessage(message);
			}

		};
		php.onHandlerReady();
	}

	public void sendAction(String action) {
		if (handler == null) {
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putString(
			Preferences.DOCUMENT_ROOT, preferences.getString(context, Preferences.DOCUMENT_ROOT)
		);
		bundle.putString(Preferences.PORT, preferences.getString(context, Preferences.PORT));
		send(action, bundle);
	}

	public void sendError(String error) {
		if (handler == null) {
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putString("message", error);
		send("error", bundle);
	}

	private void send(String action, Bundle bundle) {
		if (handler == null || bundle == null) {
			return;
		}
		bundle.putString("action", action);
		Message message = new Message();
		message.setData(bundle);
		handler.sendMessage(message);
	}

	public boolean isReady() {
		return handler != null;
	}

}
