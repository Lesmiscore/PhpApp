package com.esminis.model.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class PreferencesBackendSqlite implements Preferences.PreferencesBackend {

	static private final String TABLE = "preferences";

	private final SQLiteDatabase connection;

	PreferencesBackendSqlite(Context context) {
		connection = SQLiteDatabase.openOrCreateDatabase(
			new File(context.getFilesDir(), "preferences.sqlite"), null
		);
		connection.beginTransaction();
		try {
			connection.execSQL(
				"CREATE TABLE IF NOT EXISTS " + TABLE + " (name TEXT PRIMARY KEY, value TEXT)"
			);
			connection.setTransactionSuccessful();
		} finally {
			connection.endTransaction();
		}
	}

	@Override
	public void put(Map<String, String> values) {
		connection.beginTransaction();
		try {
			final Set<String> keys = values.keySet();
			for (String key : keys) {
				ContentValues contentValues = new ContentValues();
				contentValues.put("name", key);
				contentValues.put("value", values.get(key));
				connection.replace(TABLE, null, contentValues);
			}
			connection.setTransactionSuccessful();
		} finally {
			connection.endTransaction();
		}
	}

	@Override
	public String get(String name, String defaultVale) {
		connection.beginTransaction();
		try {
			Cursor cursor = connection.query(
				TABLE, null, "name = ?", new String[]{name}, null, null, null, null
			);
			String value = defaultVale;
			if (cursor.moveToFirst()) {
				value = cursor.getString(cursor.getColumnIndex("value"));
			}
			cursor.close();
			connection.setTransactionSuccessful();
			return value;
		} finally {
			connection.endTransaction();
		}
	}

	@Override
	public Map<String, String> get() {
		connection.beginTransaction();
		try {
			Cursor cursor = connection.query(TABLE, null, null, null, null, null, null, null);
			final Map<String, String> result = new HashMap<>();
			final int[] indexes = {cursor.getColumnIndex("name"), cursor.getColumnIndex("value")};
			while (cursor.moveToNext()) {
				result.put(cursor.getString(indexes[0]), cursor.getString(indexes[1]));
			}
			cursor.close();
			connection.setTransactionSuccessful();
			return result;
		} finally {
			connection.endTransaction();
		}
	}

	@Override
	public boolean contains(String name) {
		connection.beginTransaction();
		try {
			Cursor cursor = connection.query(
				TABLE, null, "name = ?", new String[]{name}, null, null, null, null
			);
			final boolean found = cursor.getCount() > 0;
			cursor.close();
			connection.setTransactionSuccessful();
			return found;
		} finally {
			connection.endTransaction();
		}
	}


}
