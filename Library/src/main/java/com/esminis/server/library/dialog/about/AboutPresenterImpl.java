/**
 * Copyright 2016 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esminis.server.library.dialog.about;

import com.esminis.server.library.R;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.model.ProductLicense;
import com.esminis.server.library.model.manager.ProductLicenseManager;
import com.esminis.server.library.widget.ProductLicensesViewer;

import rx.Observable;

public class AboutPresenterImpl implements AboutPresenter {

	private final ProductLicenseManager manager;

	private AboutView view = null;
	private final LibraryApplication application;

	public AboutPresenterImpl(LibraryApplication application) {
		manager = application.getComponent().getProductLicenseManager();
		this.application = application;
	}

	public void setView(AboutView view) {
		this.view = view;
		view.setContentManual(application, R.string.manual_content, R.string.manual_content_parameter);
		view.setContentAbout(application, R.string.about_content, R.string.about_content_parameter);
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
		if (view != null) {
			view.setupOnCreate();
		}
	}

	@Override
	public Observable<Void> show() {
		if (view != null) {
			view.setupOnShow();
		}
		return null;
	}

}
