package com.esminis.server.php.service.server;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamReader extends AsyncTask<Object, Void, Void> {

	@Override
	protected Void doInBackground(Object... arguments) {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader((InputStream)arguments[0])
		);
		String line = null;
		for (;;) {
			try {
				line = reader.readLine();
			} catch (IOException ex) {
				break;
			}
			if (line == null) {
				break;
			}
			((Php)arguments[1]).sendErrorLine(line);
		}
		((Php)arguments[1]).sendAction("stop");
		return null;
	}
	
}
