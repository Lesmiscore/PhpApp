package com.esminis.server.library.activity.external;

import android.os.Bundle;

interface IntentView {

	void finish(int result, Bundle data);

	void setup(String application, IntentAction action);

	void showExecutingAction(String application, IntentAction action);

}
