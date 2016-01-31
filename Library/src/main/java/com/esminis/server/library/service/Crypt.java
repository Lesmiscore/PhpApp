package com.esminis.server.library.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypt {

	static private final byte[] buffer = new byte[4096];

	static private MessageDigest createDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

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
