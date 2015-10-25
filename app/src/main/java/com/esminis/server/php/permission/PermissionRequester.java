package com.esminis.server.php.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

@Singleton
public class PermissionRequester {

	static private final int REQUEST_CODE_PERMISSION = 1;

	static public class RequestFailed extends Exception {

		static public final int DENIED = 2;
		static public final int DENIED_EXPLANATION_NEEDED = 3;
		static public final int DENIED_ANOTHER_REQUEST_IN_PROGRESS = 4;
		static public final int ACTIVITY_NOT_AVAILABLE = 5;

		public final int code;

		public RequestFailed(int code) {
			this.code = code;
		}

	}

	private final Object lock = new Object();
	private Subscriber<? super Void> subscriberInProgress = null;
	private boolean shouldShowRequestPermissionRationale = false;

	@Inject
	public PermissionRequester() {}

	public Observable<Void> request(Activity activity, final String permission) {
		final WeakReference<Activity> activityReference = new WeakReference<>(activity);
		return Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				final Activity activity = activityReference.get();
				if (activity == null) {
					subscriber.onError(new RequestFailed(RequestFailed.ACTIVITY_NOT_AVAILABLE));
					subscriber.onCompleted();
				} else if (
					ContextCompat.checkSelfPermission(activity, permission) ==
						PackageManager.PERMISSION_GRANTED
				) {
					subscriber.onNext(null);
					subscriber.onCompleted();
				} else {
					synchronized (lock) {
						if (subscriberInProgress != null) {
							subscriber.onError(
								new RequestFailed(
									RequestFailed.DENIED_ANOTHER_REQUEST_IN_PROGRESS
								)
							);
							return;
						}
						shouldShowRequestPermissionRationale = ActivityCompat
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
		final boolean shouldShowRequestPermissionRationale;
		synchronized (lock) {
			subscriber = subscriberInProgress;
			shouldShowRequestPermissionRationale = this.shouldShowRequestPermissionRationale;
			subscriberInProgress = null;
			this.shouldShowRequestPermissionRationale = false;
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
				new RequestFailed(
					shouldShowRequestPermissionRationale ?
						RequestFailed.DENIED_EXPLANATION_NEEDED : RequestFailed.DENIED
				)
			);
		}
		subscriber.onCompleted();
	}

	public void cleanup() {
		synchronized (lock) {
			subscriberInProgress = null;
			shouldShowRequestPermissionRationale = false;
		}
	}

}
