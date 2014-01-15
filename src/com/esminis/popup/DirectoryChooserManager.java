/**
 * Copyright 2013 Tautvydas Andrikys
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
package com.esminis.popup;

import java.io.File;
import java.util.ArrayList;

public class DirectoryChooserManager {
	
	public ArrayList<File> getSubdirectories(File parent) {
		ArrayList<File> result = new ArrayList<File>();
		File[] list = parent.listFiles();
		if (list != null) {
			for (File file : list) {
				if (file.isDirectory()) {
					result.add(file);
				}
			}
		}
		return result;
	}
	
}