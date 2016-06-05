package com.esminis.server.library.activity.external;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.esminis.server.library.activity.main.MainPresenter;
import com.esminis.server.library.model.manager.InstallPackageManager;
import com.esminis.server.library.permission.PermissionRequester;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;

public class IntentPresenterImpl implements IntentPresenter {

	private IntentView view = null;

	private final Preferences preferences;
	private final ServerControl serverControl;
	private final PermissionRequester permissionRequester;
	private final InstallPackageManager installPackageManager;

	private String preferenceKey = null;
	private IntentAction action = null;
	private String application = null;

	public IntentPresenterImpl(
		Preferences preferences, ServerControl serverControl, PermissionRequester permissionRequester,
		InstallPackageManager installPackageManager
	) {
		this.preferences = preferences;
		this.serverControl = serverControl;
		this.permissionRequester = permissionRequester;
		this.installPackageManager = installPackageManager;
	}

	@Override
	public void onCreate(Context context, IntentView view, Intent intent, String application) {
		this.view = view;
		if (
			intent == null || application == null || !context.getPackageName().equals(intent.getPackage())
		) {
			view.finish(IntentAction.ERROR_INVALID_INTENT, null);
		} else if (
			!permissionRequester.hasPermission(context, MainPresenter.MAIN_PERMISSION)
		) {
			view.finish(IntentAction.ERROR_NO_ANDROID_PERMISSIONS, null);
		} else if (installPackageManager.getInstalled() == null) {
			view.finish(IntentAction.ERROR_SERVER_NOT_INSTALLED, null);
		} else {
			try {
				action = IntentAction.get(intent.getAction());
			} catch (Throwable throwable) {
				view.finish(IntentAction.ERROR_INVALID_ACTION, null);
				return;
			}
			preferenceKey = Preferences.PREFIX_EXTERNAL_INTENT_PERMISSION + application + "_" +
				action.getName();
			view.setup(this.application = application, action);
			final Boolean allow = rememberedAllow(context);
			if (allow != null) {
				setAllowed(context, allow, true);
			}
		}
	}

	private Boolean rememberedAllow(Context context) {
		final String remembered = preferences.getString(context, preferenceKey);
		if ("1".equals(remembered)) {
			return true;
		}
		return "0".equals(remembered) ? false : null;
	}

	@Override
	public void setAllowed(Context context, boolean allowed, boolean remember) {
		if (remember) {
			preferences.set(context, preferenceKey, allowed ? "1" : "0");
		}
		if (!allowed) {
			view.finish(IntentAction.ERROR_USER_DENIED, null);
			return;
		}
		if (action == null) {
			view.finish(IntentAction.ERROR_INVALID_ACTION, null);
			return;
		}
		final Boolean[] result = {null, null, false};
		Observable.create(new Observable.OnSubscribe<Void>() {
			@Override
			public void call(Subscriber<? super Void> subscriber) {
				subscriber.onNext(null);
				subscriber.onCompleted();
			}
		}).delay(2, TimeUnit.SECONDS).subscribe(new Subscriber<Void>() {
			@Override
			public void onCompleted() {}

			@Override
			public void onError(Throwable e) {}

			@Override
			public void onNext(Void dummy) {
				synchronized (result) {
					result[0] = true;
					finished(result);
				}
			}
		});
		final Subscriber<Void> subscriber = new Subscriber<Void>() {

			@Override
			public void onCompleted() {
				synchronized (result) {
					result[1] = true;
					finished(result);
				}
			}

			@Override
			public void onError(Throwable e) {
				synchronized (result) {
					result[1] = false;
					finished(result);
				}
			}

			@Override
			public void onNext(Void dummy) {
				synchronized (result) {
					result[2] = true;
				}
			}
		};
		if (action == IntentAction.START) {
			serverControl.requestStart(subscriber);
		} else if (action == IntentAction.STOP) {
			serverControl.requestStop(subscriber);
		} else if (action == IntentAction.RESTART) {
			serverControl.requestRestart(subscriber);
		} else if (action == IntentAction.GET) {
			serverControl.requestStatus(context, subscriber);
		} else if (action == IntentAction.SET) {
			// @todo execute set, don`t forget to validate all fields
			serverControl.requestRestartIfRunning(subscriber);
		}
		view.showExecutingAction(application, action);
	}

	private void finished(Boolean[] result) {
		if (result[0] != null && result[1] != null) {
			if (result[1]) {
				final Bundle data = new Bundle();
				if (action == IntentAction.GET) {
					data.putBoolean("running", result[2]);
					// @todo all fields: address, port, document_root
				}
				view.finish(Activity.RESULT_OK, data);
			} else {
				view.finish(IntentAction.ERROR_SERVER_ACTION_FAILED, null);
			}
		}
	}

}
