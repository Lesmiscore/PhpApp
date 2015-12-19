package com.esminis.server.library.dialog.about;

import android.content.Context;

import com.esminis.server.library.widget.ProductLicensesViewer;

public interface AboutView {

	void setContentAbout(Context context, int content);

	void setContentManual(Context context, int content);

	void setLicensesProvider(ProductLicensesViewer.ProductLicenseProvider provider);

	void setupOnShow();

	void setupOnCreate();

}
