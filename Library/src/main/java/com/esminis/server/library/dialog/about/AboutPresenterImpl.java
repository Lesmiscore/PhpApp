package com.esminis.server.library.dialog.about;

import com.esminis.server.library.R;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.ProductLicense;
import com.esminis.server.library.model.manager.ProductLicenseManager;
import com.esminis.server.library.widget.ProductLicensesViewer;

public class AboutPresenterImpl implements AboutPresenter {

	private final ProductLicenseManager manager;

	private final AboutView view;

	AboutPresenterImpl(LibraryApplication application, AboutView view) {
		this.view = view;
		manager = application.getComponent().getProductLicenseManager();
		view.setContentManual(application, R.string.manual_content);
		view.setContentAbout(application, R.string.about_content);
		view.setLicensesProvider(
			new ProductLicensesViewer.ProductLicenseProvider() {

				final ProductLicenseManager manager = AboutPresenterImpl.this.manager;

				@Override
				public ProductLicense[] getList() {
					return manager.getLicenses();
				}

				@Override
				public String getContent(ProductLicense model) {
					return manager.getProductLicenseContent(model);
				}
			}
		);
	}

	@Override
	public void onCreate() {
		view.setupOnCreate();
	}

	@Override
	public void show() {
		view.setupOnShow();
	}

}
