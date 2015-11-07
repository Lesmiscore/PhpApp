package com.esminis.server.library.activity;

import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class DrawerFragmentHelper {

	@Inject
	Preferences preferences;

	@Inject
	ServerControl serverControl;

	@Inject
	MainActivityHelper activityHelper;

	@Inject
	Bus bus;

	@Inject
	public DrawerFragmentHelper() {}

}
