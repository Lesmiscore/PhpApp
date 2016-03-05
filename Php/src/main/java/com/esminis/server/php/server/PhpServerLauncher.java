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
package com.esminis.server.php.server;

import android.content.Context;

import com.esminis.server.library.service.server.ServerLauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Process;
import java.util.ArrayList;
import java.util.List;

class PhpServerLauncher extends ServerLauncher {

	PhpServerLauncher(com.esminis.server.library.model.manager.Process managerProcess) {
		super(managerProcess);
	}

	private List<String> getIniModules(File iniDirectory) {
		List<String> list = new ArrayList<>();
		File[] files = iniDirectory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().endsWith(".ini")) {
					list.addAll(getIniModulesFromFile(file));
				}
			}
		}
		return list;
	}

	private List<String> getIniModulesFromFile(File file) {
		List<String> list = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("#") || line.contains(";")) {
					continue;
				}
				if (line.contains("extension")) {
					File fileTemp = new File(
						line.replaceAll("^[^#]*(zend_extension|extension).*=(.+\\.so).*$", "$2").trim()
					);
					list.add(fileTemp.getName().toLowerCase());
				}
			}
			reader.close();
		} catch (IOException ignored) {
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ignored) {}
		}
		return list;
	}

	private void addStartupModules(List<String> options, File iniDirectory, File[] modules) {
		final List<String> iniModules = getIniModules(iniDirectory);
		final List<String> list = new ArrayList<>();
		for (File module : modules) {
			final boolean zend = module.getName().startsWith("zend_");
			if (!iniModules.contains(module.getName().toLowerCase())) {
				list.add((zend ? "zend_" : "") + "extension=" + module.getAbsolutePath());
			}
		}
		for (String row : list) {
			options.add("-d");
			options.add(row);
		}
	}

	private List<String> createCommand(
		File php, String address, String root, File iniDirectory, boolean indexPhpRouter, File[] modules
	) {
		final List<String> options = new ArrayList<>();
		options.add(php.getAbsolutePath());
		options.add("-S");
		options.add(address);
		options.add("-t");
		options.add(root);
		addStartupModules(options, iniDirectory, modules);
		if (indexPhpRouter) {
			options.add("index.php");
		}
		return options;
	}

	private List<String> getEnvironment(File moduleDirectory) {
		List<String> environment = getEnvironment();
		environment.add("ODBCSYSINI=" + moduleDirectory.getAbsolutePath());
		return environment;
	}

	Process start(
		File php, String address, String root, File moduleDirectory, File documentRoot, boolean indexPhpRouter, File[] modules
	) throws IOException {
		return start(
			php, createCommand(
				php, address, root, documentRoot, indexPhpRouter, modules
			), getEnvironment(moduleDirectory), documentRoot
		);
	}

}
