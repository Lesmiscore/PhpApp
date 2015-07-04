package com.esminis.server.php;

public class ErrorWithMessage extends Exception {

	public final int messageId;

	public ErrorWithMessage(int messageId) {
		super();
		this.messageId = messageId;
	}

}
