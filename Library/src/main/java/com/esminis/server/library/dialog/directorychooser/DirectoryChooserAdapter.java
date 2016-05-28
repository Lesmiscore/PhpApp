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

import android.content.Context;
import android.os.Environment;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.esminis.server.library.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class DirectoryChooserAdapter extends ArrayAdapter<DirectoryRecord> {

	private final Map<String, File[]> cache = new HashMap<>();
	private final VectorDrawableCompat iconLock;

	DirectoryChooserAdapter(Context context) {
		super(context, 0);
		final File directory = Environment.getExternalStorageDirectory();
		cache.put(directory.getParent(), new File[] {directory});
		iconLock = VectorDrawableCompat.create(getContext().getResources(), R.drawable.icon_lock, null);
		if (iconLock != null) {
			final int iconSize = context.getResources().getDimensionPixelSize(R.dimen.size_icon_small);
			iconLock.mutate().setBounds(0, 0, iconSize, iconSize);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.view_directory_chooser_list_item, parent, false);
			iconLock.setTint(((TextView)convertView).getCurrentTextColor());
		}
		final TextView viewText = (TextView)convertView;
		final DirectoryRecord model = getItem(position);
		viewText.setText(model.title);
		viewText.setCompoundDrawables(null, null, model.isWritable ? null : iconLock, null);
		return convertView;
	}

	void setParent(File parent) {
		File[] list = parent.listFiles();
		final File parentFile = parent.getParentFile();
		if (list == null && cache.containsKey(parent.getAbsolutePath())) {
			list = cache.get(parent.getAbsolutePath());
		}
		if (parentFile == null || parentFile.listFiles() == null) {
			cache.put(parentFile == null ? null : parentFile.getAbsolutePath(), new File[] {parent});
		}
		clear();
		if (parentFile != null) {
			add(
				new DirectoryRecord(
					parentFile, Html.fromHtml(
						String.format(
							"<font color=#000000><b>.. (%1$s)</b></font>",
							getContext().getString(R.string.go_up).toUpperCase()
						)
					), true
				)
			);
		}
		if (list != null) {
			for (File file : list) {
				if (file.isDirectory()) {
					add(new DirectoryRecord(file));
				}
			}
		}
		notifyDataSetChanged();
	}

}
