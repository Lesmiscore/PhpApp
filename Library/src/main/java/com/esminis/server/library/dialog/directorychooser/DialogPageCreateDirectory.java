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

import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.dialog.dialogpager.DialogPage;
import com.esminis.server.library.service.Utils;

import java.io.File;

class DialogPageCreateDirectory implements DialogPage<File> {

	private File parent = null;
	private TextView viewTitle;
	private TextView viewError;
	private EditText viewInput;
	private View buttonSave;

	DialogPageCreateDirectory(final DirectoryChooser chooser, ViewGroup container) {
		final ViewGroup layout = (ViewGroup) LayoutInflater.from(chooser.getContext())
			.inflate(R.layout.view_directory_chooser_page_create, container);
		viewTitle = (TextView)layout.findViewById(R.id.title);
		viewError = (TextView)layout.findViewById(R.id.error);
		viewInput = (EditText)layout.findViewById(R.id.input);
		viewInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable text) {
				buttonSave.setEnabled(text.length() > 0);
			}
		});
		layout.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				chooser.showChooser();
			}
		});
		buttonSave = layout.findViewById(R.id.button_save);
		buttonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String name = viewInput.getText().toString();
				final File file = new File(parent, name);
				if (parent == null || name.isEmpty() || file.isDirectory() || !file.mkdirs()) {
					viewError.setVisibility(View.VISIBLE);
					viewError.setText(
						file.isDirectory() ? R.string.error_directory_already_exists :
							R.string.error_cannot_create_directory
					);
				} else {
					viewError.setVisibility(View.GONE);
					chooser.showChooser();
				}
			}
		});
	}

	@Override
	public void onShow(File parent) {
		this.parent = parent;
		viewInput.setText(null);
		Utils.keyboardShow(viewInput);
		viewTitle.setText(
			Html.fromHtml(
				viewTitle.getContext().getString(
					R.string.create_directory_in, parent == null ? "/" : parent.getAbsolutePath()
				)
			)
		);
	}

}
