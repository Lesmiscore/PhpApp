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
package com.esminis.server.library.service.server;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class ServerHandler extends HandlerThread {

	private Handler handler = null;
	private final ServerControl serverControl;

	static private class HandlerLocal extends Handler {

		private ServerControl serverControl;

		private HandlerLocal(Looper looper, ServerControl serverControl) {
			super(looper);
			this.serverControl = serverControl;
			serverControl.onHandlerReady();
		}

		@Override
		public void handleMessage(Message message) {
			serverControl.onHandlerMessage(message);
		}

	}

	public ServerHandler(ServerControl serverControl) {
		super("Server");
		this.serverControl = serverControl;
		start();
	}

	@Override
	protected void onLooperPrepared() {
		super.onLooperPrepared();
		if (getLooper() != null) {
			handler = new HandlerLocal(getLooper(), serverControl);
		}
	}

	public void sendAction(String action, Bundle bundle) {
		if (handler != null) {
			send(action, bundle);
		}
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
