package com.esminis.server.php;

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
