package com.esminis.server.library.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.File;

public interface MainPresenter {

	void onCreate(Bundle savedInstanceState, MainView view);

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

	void onInstallNewVersionResponse(boolean confirmed);

}
