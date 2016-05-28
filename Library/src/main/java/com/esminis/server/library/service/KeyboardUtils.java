package com.esminis.server.library.service;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtils {

	private KeyboardUtils() {}

	static public void hide(Dialog dialog) {
		final View focus = dialog.getCurrentFocus();
		if (focus != null) {
			((InputMethodManager)dialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(focus.getApplicationWindowToken(), 0);
		}
	}

	static public void show(View view) {
		view.requestFocus();
		((InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
			.showSoftInput(view, InputMethodManager.SHOW_FORCED);
	}

}
