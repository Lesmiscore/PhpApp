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
package com.esminis.server.library.service.server;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ServerStreamReader extends AsyncTask<InputStream, Void, Void> {

	private ServerControl serverControl = null;
	private ServerHandler handler = null;

	public ServerStreamReader(ServerControl serverControl, ServerHandler handler) {
		this.serverControl = serverControl;
		this.handler = handler;
	}

	@Override
	protected Void doInBackground(InputStream... arguments) {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(arguments[0])
		);
		String line;
		List<String> lines = new ArrayList<>();
		for (;;) {
			try {
				line = reader.readLine();
			} catch (IOException ex) {
				break;
			}
			if (line == null) {
				break;
			}
			lines.clear();
			lines.add(line);
			try {
				while (reader.ready()) {
					line = reader.readLine();
					if (line == null) {
						break;
					}
					lines.add(line);
				}
			} catch (IOException ignored) {}
			try {
				Thread.sleep(250);
			} catch (InterruptedException ignored) {}
			handler.sendError(TextUtils.join("\n", lines));
		}
		if (!isCancelled()) {
			serverControl.requestStop();
		}
		return null;
	}
	
}
