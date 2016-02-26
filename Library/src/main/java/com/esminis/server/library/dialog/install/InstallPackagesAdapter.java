package com.esminis.server.library.dialog.install;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.model.InstallPackage;

import java.util.ArrayList;
import java.util.List;

class InstallPackagesAdapter extends BaseAdapter {

	private final InstallPackage[] list;
	private final InstallPackage installed;

	InstallPackagesAdapter(InstallPackage[] list, InstallPackage installed) {
		final List<InstallPackage> listTemp = new ArrayList<>();
		if (installed != null) {
			listTemp.add(installed);
		}
		for (int i = list.length - 1; i >= 0; i--) {
			final InstallPackage model = list[i];
			if (installed == null || !installed.equals(model)) {
				listTemp.add(model);
			}
		}
		this.list = listTemp.toArray(new InstallPackage[listTemp.size()]);
		this.installed = installed;
	}

	@Override
	public int getCount() {
		return list.length;
	}

	@Override
	public InstallPackage getItem(int position) {
		return list[position];
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.view_install_package_list_item, parent, false);
		}
		final TextView view = (TextView)convertView;
		final InstallPackage model = getItem(position);
		final String title = model.getTitle(view.getContext());
		view.setText(
			Html.fromHtml(
				model == installed ?
					view.getContext().getString(R.string.install_package_title_currently_installed, title) :
					title
			)
		);
		view.setClickable(installed == model);
		return convertView;
	}

}
