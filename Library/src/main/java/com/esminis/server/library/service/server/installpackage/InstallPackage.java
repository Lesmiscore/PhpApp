package com.esminis.server.library.service.server.installpackage;

import android.content.Context;
import android.util.Log;

import com.esminis.server.library.service.Crypt;

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

class InstallPackage {

	void install(
		Context context, com.esminis.server.library.model.InstallPackage model
	) throws Throwable {
		final File file = new File(context.getExternalFilesDir(null), "tmp_install");
		final File targetDirectory = context.getFilesDir();
		try {
			download(model, file);
			uninstall(targetDirectory);
			install(file, targetDirectory);
		} finally {
			if (file.isFile()) {
				//noinspection ResultOfMethodCallIgnored
				file.delete();
			}
		}
	}

	private void download(
		com.esminis.server.library.model.InstallPackage model, File fileOutput
	) throws Throwable {
		final HttpURLConnection connection = (HttpURLConnection)new URL(model.uri).openConnection();
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(fileOutput);
			IOUtils.copy(connection.getInputStream(), output);
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
	}

	private void uninstall(File directory) throws Throwable {
		final File list[] = directory.listFiles();
		if (list != null) {
			for (File file : list) {
				if (file.isDirectory()) {
					uninstall(file);
				} else if (!file.delete()) {
					throw new Exception("Cannot uninstall file: " + file);
				}
			}
		}
	}

	private void install(File file, File targetDirectory) throws Throwable {
		InputStream input = null;
		ZipInputStream zip = null;
		try {
			input = new FileInputStream(file);
			zip = new ZipInputStream(input);
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				install(targetDirectory, entry.getName(), zip);
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
		Log.d("TEST", "installed file: " + file.getAbsolutePath() + " " + file.length());
	}

}
