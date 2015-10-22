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
package com.esminis.server.php.helper;

import android.app.Activity;

import java.lang.ref.WeakReference;

class ActivityHelper {

	private WeakReference<Activity> activity = new WeakReference<>(null);

	public void onResume(Activity activity) {
		this.activity = new WeakReference<>(activity);
	}

	public void onPause() {
		activity = new WeakReference<>(null);
	}

	protected Activity getActivity() {
		return activity.get();
	}
}