/**
 * Copyright 2015 Tautvydas Andrikys
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
package com.esminis.model.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.LocalServerSocket;

import java.io.IOException;

public class Preferences {

	private LocalServerSocket lock = null;

	public void set(Context context, String name, boolean value) {
		lock();
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putBoolean(name, value);
		editor.commit();
		unlock();
	}

	public void set(Context context, String name, String value) {
		lock();
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putString(name, value);
		editor.commit();
		unlock();
	}

	public String getString(Context context, String name) {
		lock();
		final String value = getPreferences(context).getString(name, "");
		unlock();
		return value;
	}

	public boolean getBoolean(Context context, String name) {
		lock();
		final boolean value = getPreferences(context).getBoolean(name, false);
		unlock();
		return value;
	}

	public boolean contains(Context context, String name) {
		lock();
		final boolean value = getPreferences(context).contains(name);
		unlock();
		return value;
	}

	private SharedPreferences getPreferences(Context context) {
		return context
			.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
	}

	private void lock() {
		final long timeout = System.currentTimeMillis() + 5000;
		do {
			try {
				lock = new LocalServerSocket("lock");
			} catch (IOException e) {
				try {
					Thread.sleep(25);
				} catch (InterruptedException ignored) {}
				if (System.currentTimeMillis() > timeout) {
					break;
				}
			}
		} while (lock == null);
	}

	public void unlock() {
		if (lock == null) {
			return;
		}
		try {
			lock.close();
		} catch (IOException ignored) {}
		lock = null;
	}
	
}
