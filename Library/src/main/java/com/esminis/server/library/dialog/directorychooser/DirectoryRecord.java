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
package com.esminis.server.library.dialog.directorychooser;

import com.esminis.server.library.service.Utils;
import java.io.File;

class DirectoryRecord {

	final File file;
	final CharSequence title;
	final boolean isWritable;

	DirectoryRecord(File file) {
		this(file, file.getName(), Utils.canWriteToDirectory(file));
	}

	DirectoryRecord(File file, CharSequence title, boolean isWritable) {
		this.file = file;
		this.title = title;
		this.isWritable = isWritable;
	}

}
