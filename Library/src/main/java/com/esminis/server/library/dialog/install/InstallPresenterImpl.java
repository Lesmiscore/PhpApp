package com.esminis.server.library.dialog.install;

import android.os.Bundle;
import android.util.Log;

import com.esminis.server.library.R;
import com.esminis.server.library.model.InstallPackage;
import com.esminis.server.library.model.manager.InstallPackageManager;
import com.esminis.server.library.service.background.BackgroundService;
import com.esminis.server.library.service.server.installpackage.InstallPackageTaskProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class InstallPresenterImpl implements InstallPresenter {

	private InstallView view = null;
	private final InstallPackageManager manager;
	private InstallPackage installingPackage = null;

	@Inject
	public InstallPresenterImpl(InstallPackageManager manager) {
		this.manager = manager;
	}

	@Override
	public void setView(InstallView view) {
		this.view = view;
	}

	@Override
	public void onCreate() {
		if (view != null) {
			view.setupOnCreate();
			if (installingPackage == null) {
				downloadList();
			} else {
				view.showMessageInstalling(installingPackage);
			}
		}
	}

	@Override
	public void show() {}

	@Override
	public void downloadList() {
		final InstallView view = this.view;
		if (view != null) {
			view.showMessage(true, R.string.downloading_packages);
			manager.get()
				.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(
				new Subscriber<InstallPackage[]>() {
					@Override
					public void onCompleted() {}

					@Override
					public void onError(Throwable e) {
						if (view == InstallPresenterImpl.this.view) {
							view.showMessage(false, R.string.downloading_packages_failed, e.getMessage());
						}
					}

					@Override
					public void onNext(InstallPackage[] list) {
						if (view == InstallPresenterImpl.this.view) {
							view.hideMessage();
							view.showList(list);
						}
					}
				}
			);
		}
	}

	@Override
	public void install(final InstallPackage model) {
		if (view != null) {
			view.showMessageInstalling(model);
			installingPackage = model;
			final Bundle data = new Bundle();
			try {
				data.putString("package", model.toJson().toString());
				BackgroundService.execute(
					view.getActivity().getApplication(), InstallPackageTaskProvider.class,
					new Subscriber<Void>() {

						@Override
						public void onCompleted() {
							installingPackage = null;
							Log.d("TEST", "INSTALL COMPLETE");
						}

						@Override
						public void onError(Throwable e) {
							installingPackage = null;
							if (view != null) {
								view.showInstallFailedMessage(model, e);
							}
						}

						@Override
						public void onNext(Void dummy) {}

					}, data
				);
			} catch (Throwable e) {
				installingPackage = null;
				view.showInstallFailedMessage(model, e);
			}
		}
	}

	@Override
	public InstallPackage getInstalled() {
		return manager.getInstalled();
	}

}
