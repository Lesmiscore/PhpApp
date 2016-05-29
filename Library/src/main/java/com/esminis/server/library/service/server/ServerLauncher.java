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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract public class ServerLauncher {

	protected final com.esminis.server.library.model.manager.Process managerProcess;

	public ServerLauncher(com.esminis.server.library.model.manager.Process managerProcess) {
		this.managerProcess = managerProcess;
	}

	protected List<String> getEnvironment() {
		Map<String, String> map = System.getenv();
		List<String> environment = new ArrayList<>();
		for (String key : map.keySet()) {
			environment.add(key + "=" + map.get(key));
		}
		return environment;
	}

	protected Process start(
		File binary, List<String> command, List<String> environment, File workDirectory
	) throws IOException {
		final Process process = Runtime.getRuntime().exec(
			command.toArray(new String[command.size()]),
			environment.toArray(new String[environment.size()]), workDirectory
		);
		int pid = managerProcess.getPid(binary);
		if (pid > 0) {
			Runtime.getRuntime().exec(
				new String[] {
					"/system/bin/sh", "-c",
					"while ls " + binary.getAbsolutePath() + " > /dev/null;do sleep 5;done; kill -9 " + pid
				}
			);
		}
		return process;
	}

}
