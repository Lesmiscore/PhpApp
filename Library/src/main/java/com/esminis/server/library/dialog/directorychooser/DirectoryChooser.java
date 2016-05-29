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
package com.esminis.server.library.dialog.directorychooser;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.esminis.server.library.service.Utils;

import java.io.File;

public class DirectoryChooser extends Dialog {

	private final Page[] pages;
	private final ViewGroup[] pagesLayouts;
	private int activePage = 0;
	private OnShowListener listener = null;
	
	public DirectoryChooser(Context context) {
		super(context);
		final FrameLayout layout = new FrameLayout(context);
		setContentView(layout);
		pagesLayouts = new ViewGroup[] {new FrameLayout(context), new FrameLayout(context)};
		pages = new Page[] {
			new DirectoryChooserPage(this, pagesLayouts[0]),
			new CreateDirectoryPage(this, pagesLayouts[1])
		};
		for (View view : pagesLayouts) {
			layout.addView(view);
			view.setVisibility(View.GONE);
		}
		showPage(activePage);
		super.setOnShowListener(new DialogInterface.OnShowListener() {
			public void onShow(DialogInterface dialog) {
				showPage(activePage);
				if (listener != null) {
					listener.onShow(dialog);
				}
			}
		});
	}

	public void setParent(File parent) {
		pages[0].setParent(parent);
	}

	private void showPage(int page) {
		Utils.keyboardHide(this);
		pagesLayouts[activePage].setVisibility(View.GONE);
		pagesLayouts[activePage = page].setVisibility(View.VISIBLE);
		pages[activePage].onShow();
	}

	@Override
	public void setOnShowListener(OnShowListener listener) {
		this.listener = listener;
	}
	
	public void setOnDirectoryChooserListener(OnDirectoryChooserListener listener) {
		((DirectoryChooserPage)pages[0]).setOnDirectoryChooserListener(listener);
	}

	void showCreateDirectory(File parent) {
		pages[1].setParent(parent);
		showPage(1);
	}

	void showChooser() {
		showPage(0);
	}

	@Override
	public void onBackPressed() {
		if (activePage == 0) {
			super.onBackPressed();
		} else {
			showPage(0);
		}
	}
}
