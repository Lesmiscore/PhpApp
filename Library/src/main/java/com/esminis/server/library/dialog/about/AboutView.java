package com.esminis.server.library.dialog.about;

import android.content.Context;
import android.support.annotation.StringRes;

import com.esminis.server.library.widget.ProductLicensesViewer;

public interface AboutView {

	void setContentAbout(Context context, @StringRes int content, @StringRes int parameter);

	void setContentManual(Context context, @StringRes int content, @StringRes int parameter);

	void setLicensesProvider(ProductLicensesViewer.ProductLicenseProvider provider);

	void setupOnShow();

	void setupOnCreate();

}
