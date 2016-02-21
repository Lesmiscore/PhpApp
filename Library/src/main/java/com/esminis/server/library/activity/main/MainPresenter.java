package com.esminis.server.library.activity.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.esminis.server.library.model.InstallPackage;

import java.io.File;

import rx.Observable;

public interface MainPresenter {

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
