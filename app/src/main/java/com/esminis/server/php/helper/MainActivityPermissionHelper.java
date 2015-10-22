package com.esminis.server.php.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.esminis.server.php.R;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivityPermissionHelper extends ActivityHelper {

	private String permission = null;
	private boolean permissionMessage = false;
	private Subscriber<? super Void> permissionSubscriber = null;
	private final MainActivityHelper helper;

	public MainActivityPermissionHelper(MainActivityHelper helper) {
		this.helper = helper;
	}

	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
		if (permissionMessage) {
			permissionMessageSetVisible(true);
		} else {
			permissionRequest();
		}
	}

	public void onDestroy() {
		permissionSubscriber = null;
	}

	public Observable<Void> request(String permission) {
		this.permission = permission;
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				permissionSubscriber = subscriber;
				permissionRequest();
			}
		}).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
	}

	private void permissionRequest() {
		final Activity activity = getActivity();
		if (activity == null || permissionSubscriber == null || permission == null) {
			return;
		}
		if (
			ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
			) {
			permissionReceived();
			return;
		}
		ActivityCompat.requestPermissions(activity, new String[]{permission}, 1);
	}

	public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
		if (requestCode != 1) {
			return;
		}
		if (
			grantResults != null && grantResults.length > 0 &&
			grantResults[0] == PackageManager.PERMISSION_GRANTED
		) {
			permissionMessageSetVisible(false);
			permissionRequest();
		} else {
			permissionMessageSetVisible(true);
		}
	}

	private void permissionMessageSetVisible(boolean show) {
		permissionMessage = show;
		final Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		if (show) {
			activity.findViewById(R.id.preloader_button_ok).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						permissionRequest();
					}
				}
			);
			helper.contentMessage(
				true, false, true,
				activity.getString(R.string.permission_files_needed, activity.getString(R.string.title))
			);
		}
	}

	private void permissionReceived() {
		permissionSubscriber.onNext(null);
		permissionSubscriber.onCompleted();
		permissionSubscriber = null;
		permission = null;
		permissionMessageSetVisible(false);
	}

}
