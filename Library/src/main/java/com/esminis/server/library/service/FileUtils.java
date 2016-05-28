package com.esminis.server.library.service;

import java.io.File;
import java.io.IOException;

public class FileUtils {

	static public boolean canWriteToDirectory(File directory) {
		if (!directory.canWrite()) {
			return false;
		}
		try {
			//noinspection ResultOfMethodCallIgnored
			File.createTempFile(".temp", "lock", directory).delete();
			return true;
		} catch (IOException ignored) {}
		return false;
	}

}
