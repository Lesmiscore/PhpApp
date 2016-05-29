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
package com.esminis.server.library.service;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

	static private final byte[] buffer = new byte[4096];

	static public String hash(File file) {
		final MessageDigest digest = createDigest("MD5");
		try {
			final InputStream inputStream = new FileInputStream(file);
			final DigestInputStream dis = new DigestInputStream(inputStream, digest);
			synchronized (buffer) {
				//noinspection StatementWithEmptyBody
				while (dis.read(buffer) != -1);
			}
			final String result = bytesToHex(digest.digest());
			dis.close();
			inputStream.close();
			return result.toLowerCase();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	static public void keyboardHide(Dialog dialog) {
		final View focus = dialog.getCurrentFocus();
		if (focus != null) {
			((InputMethodManager)dialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(focus.getApplicationWindowToken(), 0);
		}
	}

	static public void keyboardShow(View view) {
		view.requestFocus();
		((InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
			.showSoftInput(view, InputMethodManager.SHOW_FORCED);
	}

	static public boolean canWriteToDirectory(File directory) {
		if (directory == null || !directory.canWrite()) {
			return false;
		}
		try {
			//noinspection ResultOfMethodCallIgnored
			File.createTempFile(".temp", "lock", directory).delete();
			return true;
		} catch (IOException ignored) {}
		return false;
	}

	static private MessageDigest createDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	static private String bytesToHex(byte[] content) {
		StringBuilder hex = new StringBuilder();
		for (byte value : content) {
			String h = Integer.toHexString(0xFF & value);
			while (h.length() < 2) {
				h = "0" + h;
			}
			hex.append(h);
		}
		return hex.toString();
	}

}
