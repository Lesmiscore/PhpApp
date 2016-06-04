package com.esminis.server.library.activity.external;

import android.content.Context;
import android.content.Intent;

public interface IntentPresenter {

	void onCreate(Context context, IntentView view, Intent intent, String application);

	void setAllowed(Context context, boolean allowed, boolean remember);

}
