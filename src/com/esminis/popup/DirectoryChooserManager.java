package com.esminis.popup;

import java.io.File;
import java.util.ArrayList;

public class DirectoryChooserManager {
	
	public ArrayList<File> getSubdicrectories(File parent) {
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
