package esminis.server.php.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Process {
	
	protected int find(File command) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(
				new InputStreamReader(Runtime.getRuntime().exec("ps").getInputStream())
			);
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(command.getAbsolutePath())) {
					String[] parts = line.split("[ ]+");
					return Integer.parseInt(parts[1]);
				}
			}
			reader.close();
		} catch (IOException ex) {}
		return -1;
	}
	
	public boolean getIsRunning(File command) {
		return find(command) != -1;
	}
	
	public void killIfFound(File command) {
		int process = find(command);
		if (process != -1) {
			android.os.Process.killProcess(process);	
	}
	}
	
}
