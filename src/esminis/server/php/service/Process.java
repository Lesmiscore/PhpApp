package esminis.server.php.service;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Process {
	
	protected int find(File command) {		
		File root = new File(File.separator + "proc");
		if (root.isDirectory()) {
			File[] list = root.listFiles();
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
									return pid;
								}
							}							
						} catch (IOException ex) {
						} finally {
							try {
								if (reader != null) {
									reader.close();
								}
							} catch (IOException ex) {}
						}
					} catch (NumberFormatException e) {}
				}
			}
		}
		return -1;
	}
	
	public boolean getIsRunning(File command) {
		return find(command) != -1;
	}
	
	public void killIfFound(File command) {
		int process = find(command);
		if (process != -1) {
			kill(process);	
		}
	}
	
	public void kill(int pid) {
		android.os.Process.killProcess(pid);
	}	
	
}
