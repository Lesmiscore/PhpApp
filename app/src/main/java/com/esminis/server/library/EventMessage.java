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
package com.esminis.server.library;

import com.esminis.server.php.R;
import com.squareup.otto.Bus;

public class EventMessage {

	public final int message;
	public final boolean error;

	private EventMessage(int message, boolean error) {
		this.message = message;
		this.error = error;
	}

	static public void post(Bus bus, Throwable throwable) {
		if (bus != null) {
			bus.post(
				new EventMessage(
					throwable instanceof ErrorWithMessage ?
						((ErrorWithMessage) throwable).messageId : R.string.error_operation_failed, true
				)
			);
		}
	}

	static public void post(Bus bus, int message) {
		if (bus != null) {
			bus.post(new EventMessage(message, false));
		}
	}

}
