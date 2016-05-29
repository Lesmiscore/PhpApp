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
package com.esminis.server.library.preferences;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class PreferencesBackendContentProvider implements PreferencesBackend {

	private final Context context;
	private final Uri uri;

	PreferencesBackendContentProvider(Context context) {
		this.context = context;
		uri = Uri.parse("content://" + context.getPackageName() + ".preferences.ContentProvider");
	}

	@Override
	public String get(String name, String defaultVale) {
		final Cursor cursor = context.getContentResolver().query(uri, null, name, null, null);
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
		final Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
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
		context.getContentResolver().update(uri, contentValues, null, null);
	}

	@Override
	public boolean contains(String name) {
		final Cursor cursor = context.getContentResolver().query(uri, null, name, null, null);
		boolean contains = false;
		if (cursor != null) {
			contains = cursor.moveToNext();
			cursor.close();
		}
		return contains;
	}

}
