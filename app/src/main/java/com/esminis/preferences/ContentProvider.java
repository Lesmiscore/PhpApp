package com.esminis.preferences;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.Set;

public class ContentProvider extends android.content.ContentProvider {

	static private final String TABLE = "preferences";

	private SQLiteDatabase connection = null;

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		if (context == null) {
			return false;
		}
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
		return true;
	}

	@Nullable
	@Override
	public Cursor query(
		@NonNull Uri uri, String[] projection, String selection, String[] args, String sortOrder
	) {
		return selection == null ? getAll() : getSingle(selection);
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		return null;
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(@NonNull Uri uri, String selection, String[] args) {
		return 0;
	}

	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] args) {
		connection.beginTransaction();
		int modified = 0;
		try {
			final Set<String> keys = values.keySet();
			for (String key : keys) {
				ContentValues valuesSave = new ContentValues();
				valuesSave.put("name", key);
				valuesSave.put("value", values.getAsString(key));
				modified += (int)connection.replace(TABLE, null, valuesSave);
			}
			connection.setTransactionSuccessful();
		} finally {
			connection.endTransaction();
		}
		return modified;
	}

	private Cursor getAll() {
		final MatrixCursor cursorResult = new MatrixCursor(new String[] {"name", "value"});
		connection.beginTransaction();
		try {
			final Cursor cursor = connection.query(TABLE, null, null, null, null, null, null, null);
			final int[] indexes = {cursor.getColumnIndex("name"), cursor.getColumnIndex("value")};
			while (cursor.moveToNext()) {
				cursorResult
					.addRow(new String[]{cursor.getString(indexes[0]), cursor.getString(indexes[1])});
			}
			cursor.close();
			connection.setTransactionSuccessful();
		} finally {
			connection.endTransaction();
		}
		return cursorResult;
	}

	private Cursor getSingle(String name) {
		final MatrixCursor cursorResult = new MatrixCursor(new String[] {"value"});
		connection.beginTransaction();
		try {
			final Cursor cursor = connection.query(
				TABLE, null, "name = ?", new String[] {name}, null, null, null, null
			);
			if (cursor.moveToFirst()) {
				cursorResult.addRow(new String[] {cursor.getString(cursor.getColumnIndex("value"))});
			}
			cursor.close();
			connection.setTransactionSuccessful();
		} finally {
			connection.endTransaction();
		}
		return cursorResult;
	}

}
