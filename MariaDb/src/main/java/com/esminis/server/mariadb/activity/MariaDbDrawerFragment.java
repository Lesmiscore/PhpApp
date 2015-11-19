package com.esminis.server.mariadb.activity;

import android.os.Bundle;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.mariadb.application.MariaDbApplication;

public class MariaDbDrawerFragment extends DrawerFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((MariaDbApplication)getActivity().getApplicationContext()).getComponent().inject(this);
	}

}
