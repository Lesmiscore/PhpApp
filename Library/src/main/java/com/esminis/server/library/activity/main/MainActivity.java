/**
 * Copyright 2015 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esminis.server.library.activity.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.esminis.server.library.R;
import com.esminis.server.library.application.LibraryApplication;

public class MainActivity extends AppCompatActivity {

	private MainPresenter presenter = null;

	static public String getIntentActionServerStatus(Context context) {
		return getIntentActionInternal(context, "SERVER_STATUS");
	}

	static public String getIntentActionInstallPackage(Context context) {
		return getIntentActionInternal(context, "INSTALL_PACKAGE");
	}

	static private String getIntentActionInternal(Context context, String prefix) {
		return context.getPackageName() + "_" + prefix;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		presenter = ((LibraryApplication)getApplication()).getComponent().getMainPresenter();
		presenter.onCreate(this, savedInstanceState, new MainViewImpl(this, presenter));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		presenter.onPostCreate();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		presenter.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		presenter.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		presenter.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		presenter.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return presenter.onCreateOptionsMenu(getMenuInflater(), menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return presenter.onMenuItemSelected(item) || super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();
		presenter.stop();
	}

	@Override
	public void onRequestPermissionsResult(
		int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
	) {
		presenter.requestPermissionsResult(requestCode, grantResults);
	}

}
