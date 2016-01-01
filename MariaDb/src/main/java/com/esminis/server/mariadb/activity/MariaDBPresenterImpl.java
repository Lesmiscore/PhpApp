package com.esminis.server.mariadb.activity;

import android.content.Context;

import com.esminis.server.library.activity.main.MainPresenterImpl;

import javax.inject.Inject;

public class MariaDBPresenterImpl extends MainPresenterImpl {

	@Inject
	public MariaDBPresenterImpl() {}

	@Override
	public String getServerRunningLabel(Context context, String address) {
		return String.format(
			context.getString(com.esminis.server.library.R.string.server_running),
			"<b>" + address + "</b>"
		);
	}

}
