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
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.service.FileUtils;

import java.io.File;

public class DirectoryChooser extends Dialog {

	private File parent = Environment.getExternalStorageDirectory();
	private OnShowListener listenerShow = null;
	private OnDirectoryChooserListener listener = null;
	private boolean firstShow = true;

	private final DirectoryChooserAdapter adapter;
	
	public DirectoryChooser(Context context) {
		super(context);
		setContentView(R.layout.dialog_directory_chooser);
		final ListView listView = (ListView)findViewById(R.id.list);
		findViewById(R.id.button_create_directory).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		findViewById(R.id.button_choose).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener != null) {
						listener.OnDirectoryChosen(parent);
					}
					dismiss();
				}
			}
		);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(
				AdapterView<?> parent, View view, int position, long id
			) {
				setParent(adapter.getItem(position).file);
			}
		});
		listView.setAdapter(adapter = new DirectoryChooserAdapter(context));
		super.setOnShowListener(new OnShowListener() {
			public void onShow(DialogInterface dialog) {
				if (firstShow) {
					firstShow = false;
					initialize();
				}
				if (listenerShow != null) {
					listenerShow.onShow(dialog);
				}
			}
		});
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		((TextView)findViewById(R.id.title)).setText(title);
	}

	private void initialize() {
		setParent(parent);
	}

	public void setParent(File parent) {
		this.parent = parent;
		if (!firstShow) {
			setTitle(
				Html.fromHtml(getContext().getString(R.string.selected_directory, parent.getAbsolutePath()))
			);
			adapter.setParent(parent);
		}
		if (parent != null) {
			final TextView viewError = (TextView)findViewById(R.id.error);
			if (FileUtils.canWriteToDirectory(parent)) {
				viewError.setVisibility(View.GONE);
			} else {
				viewError.setVisibility(View.VISIBLE);
				viewError.setText(R.string.warning_selected_directory_not_writable);
			}
		}
	}

	@Override
	public void setOnShowListener(OnShowListener listener) {
		listenerShow = listener;
	}
	
	public void setOnDirectoryChooserListener(OnDirectoryChooserListener listener) {
		this.listener = listener;
	}
	
}
