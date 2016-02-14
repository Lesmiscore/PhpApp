package com.esminis.server.library.service.server.installpackage;

import android.content.Context;
import android.content.Intent;

import com.esminis.server.library.activity.main.MainActivity;
import com.esminis.server.library.service.Crypt;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InstallerPackage {

	static public final int STATE_DOWNLOAD = 1;
	static public final int STATE_UNINSTALL = 2;
	static public final int STATE_INSTALL = 3;

	void install(
		Context context, com.esminis.server.library.model.InstallPackage model
	) throws Throwable {
		final File file = new File(context.getExternalFilesDir(null), "tmp_install");
		final File targetDirectory = context.getFilesDir();
		try {
			download(context, model, file);
			sendBroadcast(context, STATE_UNINSTALL, 0);
			uninstall(targetDirectory);
			sendBroadcast(context, STATE_UNINSTALL, 1);
			install(context, file, targetDirectory);
		} finally {
			if (file.isFile()) {
				//noinspection ResultOfMethodCallIgnored
				file.delete();
			}
		}
	}

	private void sendBroadcast(Context context, int state, float progress) {
		final Intent intent = new Intent(MainActivity.getIntentActionInstallPackage(context));
		intent.putExtra("state", state);
		intent.putExtra("progress", progress);
		context.sendBroadcast(intent);
	}

	private void download(
		Context context, com.esminis.server.library.model.InstallPackage model, File fileOutput
	) throws Throwable {
		sendBroadcast(context, STATE_DOWNLOAD, 0);
		final HttpURLConnection connection = (HttpURLConnection)new URL(model.uri).openConnection();
		connection.setConnectTimeout(20000);
		connection.setReadTimeout(10000);
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(fileOutput);
			InputStream inputStream = connection.getInputStream();
			final float fileSize = (float)connection.getContentLength();
			byte[] buffer = new byte[1024 * 128];
			long count = 0;
			int n;
			long time = System.currentTimeMillis();
			while (-1 != (n = inputStream.read(buffer))) {
				count += n;
				output.write(buffer, 0, n);
				if (System.currentTimeMillis() - time >= 1000) {
					sendBroadcast(
						context, STATE_DOWNLOAD, (((float)count) / fileSize) * 0.99f
					);
					time = System.currentTimeMillis();t
				}
			}
			sendBroadcast(context, STATE_DOWNLOAD, 0.99f);
		} finally {
			connection.disconnect();
			if (output != null) {
				try {
					output.close();
				} catch (IOException ignored) {}
			}
		}
		if (model.hash != null && !model.hash.equals(Crypt.hash(fileOutput))) {
			throw new Exception("Downloaded file was corrupted: " + model.uri);
		}
		sendBroadcast(context, STATE_DOWNLOAD, 1);
	}

	private void uninstall(File directory) throws Throwable {
		final File list[] = directory.listFiles();
		if (list != null) {
			for (File file : list) {
				if (file.isDirectory()) {
					uninstall(file);
				} else if (!FilenameUtils.isExtension(file.getAbsolutePath(), "sqlite") && !file.delete()) {
					throw new Exception("Cannot uninstall file: " + file);
				}
			}
		}
	}

	private void install(Context context, File file, File targetDirectory) throws Throwable {
		sendBroadcast(context, STATE_INSTALL, 0);
		InputStream input = null;
		ZipInputStream zip = null;
		try {
			zip = new ZipInputStream(input = new FileInputStream(file));
			ZipEntry entry;
			int totalEntries = 0;
			while (zip.getNextEntry() != null) {
				totalEntries++;
			}
			zip.close();
			input.close();
			zip = new ZipInputStream(input = new FileInputStream(file));
			int position = 0;
			while ((entry = zip.getNextEntry()) != null) {
				install(targetDirectory, entry.getName(), zip);
				sendBroadcast(context, STATE_INSTALL, ((float)++position / (float)totalEntries) * 0.99f);
			}
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException ignored) {}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException ignored) {}
			}
		}
		sendBroadcast(context, STATE_INSTALL, 1);
	}

	private void install(File targetDirectory, String filename, InputStream input) throws Throwable {
		final File file = new File(targetDirectory, filename);
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
			IOUtils.copy(input, output);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException ignored) {}
			}
		}
	}

}
