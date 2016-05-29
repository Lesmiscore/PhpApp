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

import java.io.File;

import rx.Observable;

public class DirectoryChooserPresenterImpl implements DirectoryChooserPresenter {

	private File directory = null;
	private DirectoryChooserView view = null;
	private OnDirectoryChooserListener listener = null;

	@Override
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	@Override
	public File getDirectory() {
		return directory;
	}

	@Override
	public void setOnDirectoryChooserListener(OnDirectoryChooserListener listener) {
		this.listener = listener;
	}

	@Override
	public OnDirectoryChooserListener getOnDirectoryChooserListener() {
		return listener;
	}

	@Override
	public void setView(DirectoryChooserView view) {
		this.view = view;
	}

	@Override
	public void onCreate() {}

	@Override
	public Observable<Void> show() {
		if (view != null) {
			view.showDirectoryChooser();
		}
		return null;
	}

}
