package com.esminis.server.library.activity.external;

interface IntentView {

	void finish(int result);

	void setup(String application, IntentAction action);

	void showExecutingAction(String application, IntentAction action);

}
