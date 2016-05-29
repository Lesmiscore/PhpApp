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
package com.esminis.server.library;

import android.content.Context;
import android.support.annotation.StringRes;

import com.squareup.otto.Bus;

public class EventMessage {

	private final int message;
	private final Throwable error;

	private EventMessage(@StringRes int message) {
		this.message = message;
		error = null;
	}

	private EventMessage(Throwable error) {
		this.error = error;
		message = 0;
	}

	public boolean isError() {
		return error != null;
	}

	public String getMessage(Context context) {
		if (!isError()) {
			return context.getString(message);
		}
		if (error instanceof ErrorWithMessage) {
			return ((ErrorWithMessage)error).getMessage(context);
		}
		return context.getString(R.string.error_operation_failed);
	}

	static public void post(Bus bus, Throwable throwable) {
		if (bus != null) {
			bus.post(new EventMessage(throwable));
		}
	}

	static public void post(Bus bus, @StringRes int message) {
		if (bus != null) {
			bus.post(new EventMessage(message));
		}
	}

}
