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
package com.esminis.server.mariadb.server;

import android.content.Context;

import com.esminis.server.library.service.server.ServerLauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MariaDbServerLauncher extends ServerLauncher {

	MariaDbServerLauncher(com.esminis.server.library.model.manager.Process managerProcess) {
		super(managerProcess);
	}

	private List<String> createCommand(File binary, String address, String root) {
		final List<String> options = new ArrayList<>();
		options.add(binary.getAbsolutePath());
		options.add("-S");
		options.add(address);
		options.add("-t");
		options.add(root);
		return options;
	}

	Process start(
		File binary, String address, String root, File documentRoot, boolean keepRunning,
		Context context
	) throws IOException {
		return start(
			binary, createCommand(binary, address, root), context, getEnvironment(), documentRoot,
			keepRunning
		);
	}

}
