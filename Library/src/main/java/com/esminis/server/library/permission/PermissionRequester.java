package com.esminis.server.library.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

class PermissionRequester {

	static private final int REQUEST_CODE_PERMISSION = 1;

	private final Object lock = new Object();
	private Subscriber<? super Void> subscriberInProgress = null;
	private boolean showExplanation = false;

	@Inject
	public PermissionRequester() {}

	public boolean hasPermission(Activity activity, String permission) {
		return activity != null && ContextCompat.checkSelfPermission(activity, permission) ==
			PackageManager.PERMISSION_GRANTED;
	}

	public Observable<Void> request(Activity activity, final String permission) {
		final WeakReference<Activity> activityReference = new WeakReference<>(activity);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				final Activity activity = activityReference.get();
				if (activity == null) {
					subscriber.onError(
						new PermissionRequestFailed(PermissionRequestFailed.ACTIVITY_NOT_AVAILABLE)
					);
				} else if (permission == null) {
					subscriber.onError(
						new PermissionRequestFailed(PermissionRequestFailed.DENIED_INVALID_PERMISSION)
					);
				} else if (hasPermission(activity, permission)) {
					subscriber.onNext(null);
					subscriber.onCompleted();
				} else {
					synchronized (lock) {
						if (subscriberInProgress != null) {
							subscriber.onError(
								new PermissionRequestFailed(
									PermissionRequestFailed.DENIED_ANOTHER_REQUEST_IN_PROGRESS
								)
							);
							return;
						}
						showExplanation = ActivityCompat
							.shouldShowRequestPermissionRationale(activity, permission);
						subscriberInProgress = subscriber;
					}
					ActivityCompat.requestPermissions(
						activity, new String[] {permission}, REQUEST_CODE_PERMISSION
					);
				}
			}
		}).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
	}

	public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
		if (requestCode != REQUEST_CODE_PERMISSION) {
			return;
		}
		final Subscriber<? super Void> subscriber;
		final boolean showExplanation;
		synchronized (lock) {
			subscriber = subscriberInProgress;
			showExplanation = this.showExplanation;
			subscriberInProgress = null;
			this.showExplanation = false;
		}
		if (subscriber == null) {
			return;
		}
		if (
			grantResults != null && grantResults.length > 0 &&
			grantResults[0] == PackageManager.PERMISSION_GRANTED
		) {
			subscriber.onNext(null);
		} else {
			subscriber.onError(
				new PermissionRequestFailed(
					showExplanation ?
						PermissionRequestFailed.DENIED_EXPLANATION_NEEDED : PermissionRequestFailed.DENIED
				)
			);
		}
		subscriber.onCompleted();
	}

	public void cleanup() {
		synchronized (lock) {
			subscriberInProgress = null;
			showExplanation = false;
		}
	}

}
