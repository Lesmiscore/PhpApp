/**
 * Copyright 2016 Tautvydas Andrikys
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

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.File;

public interface MainPresenter {

	String MAIN_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

	void onCreate(AppCompatActivity activity, Bundle savedInstanceState, MainView view);

	void onDestroy();

	void onResume();

	void onPause();

	void stop();

	void onPostCreate();

	void onSaveInstanceState(Bundle outState);

	void requestPermission();

	void requestPermissionsResult(int requestCode, @NonNull int[] grantResults);

	void serverStart();

	void serverStop();

	void showAbout();

	void showDocumentRootChooser();

	void onDocumentRootChosen(File documentRoot);

	void onServerInterfaceChanged(int position);

	void portModified(String newValue);

	boolean onCreateOptionsMenu(MenuInflater inflater, Menu menu);

	boolean onMenuItemSelected(MenuItem item);

	void onInstallComplete();

	void requestPackageInstall();

}
