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
package com.esminis.model.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class Process extends com.esminis.model.manager.Manager<Void> {
	
	protected String[] find(File command) {
		File root = new File(File.separator + "proc");
		if (root.isDirectory()) {
			File[] list = root.listFiles();
			if (list == null) {
				return null;
			}
			for (File file : list) {
				if (file.isDirectory()) {
					try {
						int pid = Integer.parseInt(file.getName());
						File fileCommandLine = new File(
							file.getAbsolutePath() + File.separator + "cmdline"
						);
						if (!fileCommandLine.isFile() || !fileCommandLine.canRead()) {
							continue;
						}
						BufferedReader reader = null;
						try {														
							reader = new BufferedReader(
								new InputStreamReader(new FileInputStream(fileCommandLine))
							);
							String line;
							while ((line = reader.readLine()) != null) {
								if (line.contains(command.getAbsolutePath())) {
									return new String[] {String.valueOf(pid), line};
								}
							}							
						} catch (IOException ignored) {
						} finally {
							try {
								if (reader != null) {
									reader.close();
								}
							} catch (IOException ignored) {}
						}
					} catch (NumberFormatException ignored) {}
				}
			}
		}
		return null;
	}
	
	public String[] getCommandLine(File command) {
		String[] commandLine = find(command);
		if (commandLine == null) {
			return null;
		}
		String part = "";
		List<String> parts = new LinkedList<String>();
		int length = commandLine[1].length();
		for (int i = 0; i < length; i++) {
			char letter = commandLine[1].charAt(i);
			if (i == length - 1 || letter == 0) {
				parts.add(part);
				part = "";
			} else {
				part += letter;
			}
		}
		return parts.toArray(new String[parts.size()]);
	}
	
	public void kill(File command) {
		String[] commandLine = find(command);
		if (commandLine != null) {
			kill(Integer.parseInt(commandLine[0]));	
		}
	}
	
	public void kill(int pid) {
		android.os.Process.killProcess(pid);
	}	
	
}
