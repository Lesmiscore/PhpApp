package com.esminis.server.library.dialog.directorychooser;

import java.io.File;

interface Page {

	void onShow();

	void setParent(File parent);

}
