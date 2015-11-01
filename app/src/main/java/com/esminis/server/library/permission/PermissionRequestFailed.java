package com.esminis.server.library.permission;

class PermissionRequestFailed extends Exception {

	static final int DENIED = 1;
	static final int DENIED_EXPLANATION_NEEDED = 2;
	static final int DENIED_ANOTHER_REQUEST_IN_PROGRESS = 3;
	static final int DENIED_INVALID_PERMISSION = 4;
	static final int ACTIVITY_NOT_AVAILABLE = 0;

	public final int code;

	public PermissionRequestFailed(int code) {
		this.code = code;
	}

}
