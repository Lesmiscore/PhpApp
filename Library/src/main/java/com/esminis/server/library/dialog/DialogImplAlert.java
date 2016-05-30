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
package com.esminis.server.library.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Window;

import com.esminis.server.library.R;

import rx.Observable;

public class DialogImplAlert<T extends DialogPresenter> extends AlertDialog implements Dialog {

	protected final T presenter;

	public DialogImplAlert(Context context, @NonNull  T presenter) {
		super(context, R.style.DialogAlertTheme);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.presenter = presenter;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		presenter.onCreate();
	}

	@Override
	public void show() {}

	public Observable<Void> showObserved() {
		super.show();
		return (Observable<Void>)presenter.show();
	}

}
