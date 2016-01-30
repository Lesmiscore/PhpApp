package com.esminis.server.library.dialog.install;

import com.esminis.server.library.R;
import com.esminis.server.library.model.InstallPackage;
import com.esminis.server.library.model.manager.InstallPackageManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class InstallPresenterImpl implements InstallPresenter {

	private InstallView view = null;
	private final InstallPackageManager manager;

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
			downloadList();
		}
	}

	@Override
	public void show() {}

	@Override
	public void downloadList() {
		final InstallView view = this.view;
		if (view != null) {
			view.showMessage(true, R.string.downloading_packages, null);
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
	public void install(InstallPackage model) {
		final InstallView view = this.view;
		if (view != null) {
			view.showMessageInstalling(model);
		}
	}

	@Override
	public InstallPackage getInstalled() {
		return manager.getInstalled();
	}

}
