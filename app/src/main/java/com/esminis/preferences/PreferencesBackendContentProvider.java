package com.esminis.preferences;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class PreferencesBackendContentProvider implements PreferencesBackend {

	private final Context context;
	static private final Uri URI = Uri.parse(
		"content://com.esminis.server.php.preferences.ContentProvider"
	);

	PreferencesBackendContentProvider(Context context) {
		this.context = context;
	}

	@Override
	public String get(String name, String defaultVale) {
		final Cursor cursor = context.getContentResolver().query(URI, null, name, null, null);
		String value = defaultVale;
		if (cursor != null) {
			if (cursor.moveToNext()) {
				value = cursor.getString(cursor.getColumnIndex("value"));
			}
			cursor.close();
		}
		return value;
	}

	@Override
	public Map<String, String> get() {
		final Cursor cursor = context.getContentResolver().query(URI, null, null, null, null);
		final Map<String, String> result = new HashMap<>();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				result.put(
					cursor.getString(cursor.getColumnIndex("name")),
					cursor.getString(cursor.getColumnIndex("value"))
				);
			}
			cursor.close();
		}
		return result;
	}

	@Override
	public void put(Map<String, String> values) {
		final Set<String> keys = values.keySet();
		final ContentValues contentValues = new ContentValues();
		for (String key : keys) {
			contentValues.put(key, values.get(key));
		}
		context.getContentResolver().update(URI, contentValues, null, null);
	}

	@Override
	public boolean contains(String name) {
		final Cursor cursor = context.getContentResolver().query(URI, null, name, null, null);
		boolean contains = false;
		if (cursor != null) {
			contains = cursor.moveToNext();
			cursor.close();
		}
		return contains;
	}

}
