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

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PhpStreamReader extends AsyncTask<InputStream, Void, Void> {

	private Php php = null;
	private PhpHandler handler = null;

	public PhpStreamReader(Php php, PhpHandler handler) {
		this.php = php;
		this.handler = handler;
	}

	@Override
	protected Void doInBackground(InputStream... arguments) {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(arguments[0])
		);
		String line;
		for (;;) {
			try {
				line = reader.readLine();
			} catch (IOException ex) {
				break;
			}
			if (line == null) {
				break;
			}
			handler.sendError(line);
		}
		if (!isCancelled()) {
			php.requestStop();
		}
		return null;
	}
	
}
