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

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.dialog.dialogpager.DialogPage;
import com.esminis.server.library.service.Utils;

import java.io.File;

class DialogPageDirectoryChooser implements DialogPage<File> {

	private final DialogPageDirectoryChooserAdapter adapter;
	private OnDirectoryChooserListener listener = null;
	private File parent;
	private final TextView viewError;
	private final TextView viewTitle;
	private final View buttonCreateDirectory;

	DialogPageDirectoryChooser(final DirectoryChooser chooser, ViewGroup container) {
		final ViewGroup layout = (ViewGroup)LayoutInflater.from(chooser.getContext())
			.inflate(R.layout.view_directory_chooser_page, container);
		final ListView listView = (ListView) layout.findViewById(R.id.list);
		adapter = new DialogPageDirectoryChooserAdapter(chooser.getContext());
		viewError = (TextView) layout.findViewById(R.id.error);
		viewTitle = (TextView) layout.findViewById(R.id.title);
		buttonCreateDirectory = layout.findViewById(R.id.button_create_directory);
		buttonCreateDirectory.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					chooser.showCreateDirectory(parent);
				}
			}
		);
		layout.findViewById(R.id.button_choose).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.OnDirectoryChosen(parent);
					}
					chooser.dismiss();
				}
			}
		);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(
				AdapterView<?> parent, View view, int position, long id
			) {
				onShow(adapter.getItem(position).file);
			}
		});
		listView.setAdapter(adapter);
	}

	public File getParent() {
		return parent;
	}

	@Override
	public void onShow(File file) {
		parent = file;
		if (parent != null) {
			viewTitle.setText(
				Html.fromHtml(
					viewTitle.getContext().getString(R.string.selected_directory, parent.getAbsolutePath())
				)
			);
		}
		adapter.setParent(parent);
		if (parent != null) {
			if (Utils.canWriteToDirectory(parent)) {
				viewError.setVisibility(View.GONE);
			} else {
				viewError.setVisibility(View.VISIBLE);
				viewError.setText(R.string.warning_selected_directory_not_writable);
			}
		}
		buttonCreateDirectory.setEnabled(Utils.canWriteToDirectory(parent));
	}

	void setOnDirectoryChooserListener(OnDirectoryChooserListener listener) {
		this.listener = listener;
	}

}
