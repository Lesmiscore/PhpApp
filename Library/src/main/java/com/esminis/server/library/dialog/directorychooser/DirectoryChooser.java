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
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.esminis.server.library.dialog.dialogpager.DialogPageFactory;
import com.esminis.server.library.dialog.dialogpager.DialogPager;

import java.io.File;

public class DirectoryChooser extends Dialog {

	private OnShowListener listener = null;
	private final DialogPager<File> pager;
	private final DialogPageDirectoryChooser pageDirectoryChooser;
	private final DialogPageCreateDirectory pageCreateDirectory;
	
	public DirectoryChooser(Context context) {
		super(context);
		final FrameLayout layout = new FrameLayout(context);
		setContentView(layout);
		pager = new DialogPager<>(this, layout);
		pageDirectoryChooser = pager.add(
			new DialogPageFactory<DialogPageDirectoryChooser>() {
				@Override
				public DialogPageDirectoryChooser create(ViewGroup container) {
					return new DialogPageDirectoryChooser(DirectoryChooser.this, container);
				}
			}
		);
		pageCreateDirectory = pager.add(
			new DialogPageFactory<DialogPageCreateDirectory>() {
				@Override
				public DialogPageCreateDirectory create(ViewGroup container) {
					return new DialogPageCreateDirectory(DirectoryChooser.this, container);
				}
			}
		);
		setParent(Environment.getExternalStorageDirectory());
		super.setOnShowListener(new DialogInterface.OnShowListener() {
			public void onShow(DialogInterface dialog) {
				showChooser();
				if (listener != null) {
					listener.onShow(dialog);
				}
			}
		});
	}

	public void setParent(File parent) {
		pager.show(pageDirectoryChooser, parent);
	}

	@Override
	public void setOnShowListener(OnShowListener listener) {
		this.listener = listener;
	}
	
	public void setOnDirectoryChooserListener(OnDirectoryChooserListener listener) {
		pageDirectoryChooser.setOnDirectoryChooserListener(listener);
	}

	void showCreateDirectory(File parent) {
		pager.show(pageCreateDirectory, parent);
	}

	void showChooser() {
		pager.show(pageDirectoryChooser, pageDirectoryChooser.getParent());
	}

	@Override
	public void onBackPressed() {
		if (pager.isActive(pageDirectoryChooser)) {
			super.onBackPressed();
		} else {
			showChooser();
		}
	}
}
