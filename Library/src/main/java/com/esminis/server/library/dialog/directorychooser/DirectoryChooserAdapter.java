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

import android.view.ViewGroup;

import com.esminis.server.library.dialog.pager.DialogPagerPage;
import com.esminis.server.library.dialog.pager.DialogPagerAdapter;

class DirectoryChooserAdapter extends DialogPagerAdapter {

	static final int PAGE_DIRECTORY_CHOOSER = 0;
	static final int PAGE_DIRECTORY_CREATE = 1;

	private final DirectoryChooserPresenter presenter;
	private final DirectoryChooserView view;

	DirectoryChooserAdapter(
		DirectoryChooserPresenter presenter, DirectoryChooserViewImpl view
	) {
		super(view);
		this.presenter = presenter;
		this.view = view;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public DialogPagerPage create(ViewGroup container, int position) {
		switch (position) {
			case PAGE_DIRECTORY_CHOOSER:
				return new DialogPageDirectoryChooserPage(container, presenter, view);
			case PAGE_DIRECTORY_CREATE:
				return new DialogPageCreateDirectoryPage(container, presenter, view);
		}
		throw new RuntimeException("Unsupporetd page type");
	}

}
