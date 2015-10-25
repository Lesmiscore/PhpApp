package com.esminis.server.php.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.esminis.server.php.R;
import com.esminis.server.php.permission.PermissionRequester;

import rx.Observable;
import rx.Subscriber;

public class MainActivityPermissionHelper extends ActivityHelper {

	private String permission = null;
	private boolean permissionMessage = false;
	private final MainActivityHelper helper;
	private final PermissionRequester permissionRequester;
	private Subscriber<? super Void> permissionSubscriber = null;

	public MainActivityPermissionHelper(
		MainActivityHelper helper, PermissionRequester permissionRequester
	) {
		this.helper = helper;
		this.permissionRequester = permissionRequester;
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
		permissionRequester.cleanup();
	}

	public Observable<Void> request(final String permission) {
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				MainActivityPermissionHelper.this.permission = permission;
				permissionSubscriber = subscriber;
				permissionRequest();
			}
		});
	}

	private void permissionRequest() {
		permissionRequester.request(getActivity(), permission).subscribe(new Subscriber<Void>() {
			@Override
			public void onCompleted() {}

			@Override
			public void onError(Throwable e) {
				permissionMessageSetVisible(true);
			}

			@Override
			public void onNext(Void dummy) {
				permissionReceived();
			}
		});
	}

	public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
		permissionRequester.onRequestPermissionsResult(requestCode, grantResults);
	}

	private void permissionMessageSetVisible(boolean show) {
		permissionMessage = show;
		final Activity activity = getActivity();
		if (activity != null && show) {
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
