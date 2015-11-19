package com.esminis.server.mariadb.activity;

import android.app.Activity;
import android.text.Html;

import com.esminis.server.library.activity.MainActivityHelper;
import com.esminis.server.library.preferences.Preferences;
import com.squareup.otto.Bus;

public class MariaDbMainActivityHelper extends MainActivityHelper {

	public MariaDbMainActivityHelper(Preferences preferences, Bus bus) {
		super(preferences, bus);
	}

	@Override
	public CharSequence getServerRunningLabel(String address) {
		final Activity activity = getActivity();
		if (activity == null) {
			return null;
		}
		return Html.fromHtml(
			String.format(
				activity.getString(com.esminis.server.library.R.string.server_running),
				"<b>" + address + "</b>"
			)
		);
	}
}
