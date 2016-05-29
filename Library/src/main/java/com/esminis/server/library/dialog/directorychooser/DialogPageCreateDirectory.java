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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.dialog.dialogpager.DialogPagerPager;
import com.esminis.server.library.dialog.dialogpager.DialogPager;
import com.esminis.server.library.service.Utils;

import java.io.File;

class DialogPageCreateDirectory implements DialogPagerPager {

	private final DirectoryChooserState data;
	private TextView viewTitle;
	private TextView viewError;
	private EditText viewInput;
	private View buttonSave;
	private final ViewGroup layout;

	DialogPageCreateDirectory(
		final DialogPager pager, ViewGroup container, DirectoryChooserState data
	) {
		container.addView(
			layout = (ViewGroup) LayoutInflater.from(pager.getContext())
				.inflate(R.layout.dialog_directory_chooser_page_create, container, false)
		);
		this.data = data;
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
				pager.setCurrentItem(DirectoryChooserAdapter.PAGE_DIRECTORY_CHOOSER);
			}
		});
		buttonSave = layout.findViewById(R.id.button_save);
		buttonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				create(pager);
			}
		});
		viewInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return actionId == EditorInfo.IME_ACTION_DONE && create(pager);
			}
		});
	}

	private boolean create(DialogPager pager) {
		final String name = viewInput.getText().toString().trim();
		final File parent = DialogPageCreateDirectory.this.data.directory;
		final File file = new File(parent, name);
		if (parent == null || name.isEmpty() || file.isDirectory() || !file.mkdirs()) {
			viewError.setVisibility(View.VISIBLE);
			viewError.setText(
				file.isDirectory() ? R.string.error_directory_already_exists :
					R.string.error_cannot_create_directory
			);
			return false;
		}
		viewError.setVisibility(View.GONE);
		pager.setCurrentItem(DirectoryChooserAdapter.PAGE_DIRECTORY_CHOOSER);
		return true;
	}

	@Override
	public void onStateChanged() {
		viewInput.setText(null);
		viewTitle.setText(
			Html.fromHtml(
				viewTitle.getContext().getString(
					R.string.create_directory_in, data.directory == null ? "/" : data.directory.getAbsolutePath()
				)
			)
		);
	}

	@Override
	public void onShow() {
		Utils.keyboardShow(viewInput);
	}

	@Override
	public ViewGroup getLayout() {
		return layout;
	}
}
