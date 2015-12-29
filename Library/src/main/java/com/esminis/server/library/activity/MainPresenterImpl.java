package com.esminis.server.library.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.esminis.server.library.R;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.permission.PermissionActivityHelper;
import com.esminis.server.library.permission.PermissionListener;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.server.ServerNotification;
import com.esminis.server.library.service.server.install.InstallServer;
import com.esminis.server.library.service.server.install.OnInstallServerListener;
import com.esminis.server.library.service.server.tasks.RestartIfRunningServerTaskProvider;
import com.esminis.server.library.service.server.tasks.ServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StartServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StatusServerTaskProvider;
import com.esminis.server.library.service.server.tasks.StopServerTaskProvider;

import java.io.File;

import javax.inject.Inject;

public class MainPresenterImpl implements MainPresenter {

	@Inject
	protected PermissionActivityHelper permissionHelper;

	@Inject
	protected InstallServer installServer;

	@Inject
	protected Network network;

	@Inject
	protected Log log;

	@Inject
	protected ServerNotification serverNotification;

	@Inject
	protected MainActivityHelper activityHelper;

	private final ReceiverManager receiverManager = new ReceiverManager();

	private MainView view = null;
	private Throwable installError = null;
	private boolean showInstallFinishedOnResume = false;
	private boolean paused = false;
	private final AppCompatActivity activity;

	static private final String KEY_ERROR = "errors";

	MainPresenterImpl(AppCompatActivity activity) {
		((LibraryApplication)activity.getApplication()).getComponent().inject(this);
		this.activity = activity;
		activityHelper.createToolbar(activity);
	}

	@Override
	public void onDestroy() {
		if (view != null) {
			stop();
			view = null;
		}
		receiverManager.cleanup();
		permissionHelper.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState, MainView view) {
		this.view = view;
		activityHelper.onResume(activity);
		permissionHelper.onResume(activity);
		final LibraryApplication application = (LibraryApplication)activity.getApplication();
		if (savedInstanceState != null) {
			view.setLog(savedInstanceState.getCharSequence(KEY_ERROR));
		}
		view.setMessage(
			true, false, true,
			activity.getString(R.string.permission_files_needed, activity.getString(R.string.title))
		);
		if (savedInstanceState == null) {
			try {
				activity.getFragmentManager().beginTransaction()
					.replace(R.id.drawer, application.getComponent().getDrawerFragment()).commit();
			} catch (Exception ignored) {}
		}
		requestPermission();
	}

	@Override
	public void onResume() {
		paused = false;
		activityHelper.onResume(activity);
		permissionHelper.onResume(activity);
		if (showInstallFinishedOnResume) {
			showInstallFinishedOnResume = false;
			showInstallFinished(activity);
		}
		receiverManager.onResume(activity);
		resetNetwork();
		serverStatus();
		resetLog();
	}

	@Override
	public void onPause() {
		paused = true;
		activityHelper.onPause();
		permissionHelper.onPause();
		receiverManager.onPause();
	}

	@Override
	public void stop() {
		if (view != null) {
			view.closeDialog();
		}
	}

	@Override
	public void onPostCreate() {
		if (view != null) {
			view.syncDrawer();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (view != null) {
			final CharSequence log = view.getLog();
			if (log != null) {
				outState.putCharSequence(KEY_ERROR, log);
			}
		}
	}

	@Override
	public void requestPermission() {
		permissionHelper.request(
			Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionListener() {

				@Override
				public void onGranted() {
					view.setMessage(true, true, false, activity.getString(R.string.server_installing));
					installServer.install(activity, new OnInstallServerListener() {
						@Override
						public void OnInstallNewVersionRequest(InstallServer installer) {
							if (view != null) {
								view.showInstallNewVersionRequest(activityHelper.getMessageNewVersion(activity));
							}
						}

						@Override
						public void OnInstallEnd(Throwable error) {
							installError = error;
							if (paused) {
								showInstallFinishedOnResume = true;
							} else {
								showInstallFinished(activity);
							}
						}
					});
				}

				@Override
				public void onDenied() {}

			}
		);
	}

	private void showInstallFinished(Context context) {
		if (view == null) {
			return;
		}
		if (installError != null) {
			view.setMessage(
				true, false, false,
				context.getString(R.string.server_installation_failed, installError.getMessage())
			);
			return;
		}
		view.showMainContent();
		view.setDocumentRoot(activityHelper.getRootDirectory(activity));
		view.setPort(activityHelper.getPort(activity), true);
		receiverManager.add(
			context, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					resetNetwork();
				}
			}
		);
		receiverManager.add(
			context, new IntentFilter(MainActivity.getIntentAction(context)), new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					if (view != null && MainActivity.getIntentAction(context).equals(intent.getAction())) {
						Bundle extras = intent.getExtras();
						if (extras != null && extras.containsKey("errorLine")) {
							resetLog();
						} else {
							if (extras != null && extras.getBoolean("running")) {
								view.showButton(MainView.BUTTON_STOP);
								final CharSequence title = activityHelper
									.getServerRunningLabel(extras.getString("address"));
								view.setStatusLabel(title);
								serverNotification.show(
									activity, title.toString(), activity.getString(R.string.server_running_public)
								);
							} else {
								view.showButton(MainView.BUTTON_START);
								view.setStatusLabel(activity.getString(R.string.server_stopped));
								serverNotification.hide(activity);
							}
						}
					}
				}

			}
		);
		resetNetwork();
		serverStatus();
	}

	@Override
	public void requestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
		permissionHelper.onRequestPermissionsResult(requestCode, grantResults);
	}

	@Override
	public void serverStart() {
		log.clear(activity);
		serverTask(StartServerTaskProvider.class);
		resetLog();
	}

	@Override
	public void serverStop() {
		serverTask(StopServerTaskProvider.class);
		resetLog();
	}

	@Override
	public void showAbout() {
		view.showAbout();
	}

	@Override
	public void showDocumentRootChooser() {
		view.showDocumentRootChooser(new File(activityHelper.getRootDirectory(activity)));
	}

	@Override
	public void onDocumentRootChosen(File documentRoot) {
		activityHelper.setRootDirectory(activity, documentRoot.getAbsolutePath());
		view.setDocumentRoot(activityHelper.getRootDirectory(activity));
		serverRestartIfRunning();
	}

	@Override
	public void portModified(String newValue) {
		String portPreference = activityHelper.getPort(activity);
		if (portPreference == null || portPreference.isEmpty()) {
			portPreference = activity.getString(R.string.default_port);
		}
		int port = Integer.parseInt(portPreference);
		try {
			port = Integer.parseInt(newValue);
		} catch (NumberFormatException ignored) {}
		if (port >= 1024 && port <= 65535) {
			activityHelper.setPort(activity, String.valueOf(port));
			serverRestartIfRunning();
			view.setPort(String.valueOf(port), true);
		} else {
			view.setPort(newValue, false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
		return view != null && view.createMenu(inflater, menu);
	}

	@Override
	public boolean onMenuItemSelected(MenuItem item) {
		return view != null && view.onMenuItemSelected(item);
	}

	@Override
	public void onInstallNewVersionResponse(boolean confirmed) {
		if (confirmed) {
			installServer.installNewVersionConfirmed();
		} else {
			installServer.installFinish();
		}
	}

	@Override
	public void onServerInterfaceChanged(int position) {
		final String value = activityHelper.getAddress(activity);
		final String newValue = network.get(position).name;
		if (!value.equals(newValue)) {
			activityHelper.setAddress(activity, newValue);
			serverRestartIfRunning();
		}
	}

	private void serverRestartIfRunning() {
		serverTask(RestartIfRunningServerTaskProvider.class);
	}

	private void serverStatus() {
		serverTask(StatusServerTaskProvider.class);
	}

	private void serverTask(Class<? extends ServerTaskProvider> taskClass) {
		BackgroundService.execute(activity.getApplication(), taskClass);
	}

	private void resetLog() {
		if (view != null) {
			view.setLog(log.get(activity));
		}
	}

	private void resetNetwork() {
		if (view != null) {
			boolean changed = network.refresh();
			view.setServerInterfaces(
				network.get(), network.getPosition(activityHelper.getAddress(activity))
			);
			if (changed) {
				serverRestartIfRunning();
			}
		}
	}

}
