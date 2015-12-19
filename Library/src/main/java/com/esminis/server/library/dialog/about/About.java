/**
 * Copyright 2015 Tautvydas Andrikys
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

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import com.esminis.server.library.application.LibraryApplication;

public class About extends AlertDialog {

	private AboutPresenter presenter = null;

	public About(Context context) {
		super(context);
		presenter = new AboutPresenterImpl(
			(LibraryApplication)context.getApplicationContext(), new AboutViewImpl(this)
		);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		presenter.onCreate();
	}

	@Override
	public void show() {
		super.show();
		presenter.show();
	}

}
