package com.esminis.server.library.activity.external;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class IntentAction {

	static private final List<IntentAction> actions = new ArrayList<>();

	/**
	 * Start server
	 */
	static public final IntentAction START = new IntentAction("START");

	/**
	 * Stop server
	 */
	static public final IntentAction STOP = new IntentAction("STOP");

	/**
	 * Restart server
	 */
	static public final IntentAction RESTART = new IntentAction("RESTART");

	/**
	 * Error - user has not granted permissions to server application, you should show user message
	 * to start server application and grant it permissions
	 */
	static public final int ERROR_NO_ANDROID_PERMISSIONS = Activity.RESULT_FIRST_USER + 1;

	/**
	 * Error - server application is installed, but server build is not, you should show user message
	 * to start server application and install some server build
	 */
	static public final int ERROR_SERVER_NOT_INSTALLED = Activity.RESULT_FIRST_USER + 2;

	/**
	 * Error - missing intent, intent action or intent package name does not match server application
	 */
	static public final int ERROR_INVALID_INTENT = Activity.RESULT_FIRST_USER + 3;

	/**
	 * Error - user denied server action
	 */
	static public final int ERROR_USER_DENIED = Activity.RESULT_FIRST_USER + 4;

	/**
	 * Error - invalid intent action
	 */
	static public final int ERROR_INVALID_ACTION = Activity.RESULT_FIRST_USER + 5;

	/**
	 * Error - server application could failed during execution of action(start, stop, ...)
	 */
	static public final int ERROR_SERVER_ACTION_FAILED = Activity.RESULT_FIRST_USER + 6;

	private final String name;

	private IntentAction(String name) {
		this.name = name;
		actions.add(this);
	}

	public String getName() {
		return "com.esminis.server." + name;
	}

	String getTitle(Context context, boolean progress) {
		final int value = context.getResources().getIdentifier(
			"server_action_title_" + name.toLowerCase() + (progress ? "_progress" : ""), "string",
			context.getPackageName()
		);
		return value == 0 ? "?" : context.getString(value);
	}

	static IntentAction get(String name) {
		for (IntentAction action : actions) {
			if (action.getName().equals(name)) {
				return action;
			}
		}
		throw new RuntimeException("Invalid intent action");
	}

	static public void requestServerAction(
		Activity activity, int requestCode, IntentAction action, String serverPackageName
	) {
		final Intent intent = new Intent(action.getName());
		intent.setPackage(serverPackageName);
		activity.startActivityForResult(intent, requestCode);
	}

	static public boolean isResultSuccess(int resultCode) {
		return resultCode == Activity.RESULT_OK;
	}

}
