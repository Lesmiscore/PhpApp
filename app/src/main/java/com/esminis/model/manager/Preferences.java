/**
 * Copyright 2014 Tautvydas Andrikys
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
import android.preference.PreferenceManager;

public class Preferences {

	public void set(Context context, String name, boolean value) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putBoolean(name, value);
		editor.apply();
	}
	
	public void set(Context context, String name, String value) {
		SharedPreferences.Editor editor = getPreferences(context).edit();
		editor.putString(name, value);
		editor.apply();
	}
	
	public String getString(Context context, String name) {
		return getPreferences(context).getString(name, "");
	}
	
	public boolean getBoolean(Context context, String name) {
		return getPreferences(context).getBoolean(name, false);
	}
	
	public boolean contains(Context context, String name) {
		return getPreferences(context).contains(name);
	}

	protected SharedPreferences getPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
}
