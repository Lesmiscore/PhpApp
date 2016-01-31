package com.esminis.server.library.dialog.install;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.dialog.Dialog;
import com.esminis.server.library.model.InstallPackage;

public class InstallViewImpl extends Dialog<InstallPresenter> implements InstallView {

	private final Activity activity;

	public InstallViewImpl(Activity activity, final InstallPresenter presenter) {
		super(activity, presenter);
		setView(LayoutInflater.from(activity).inflate(R.layout.dialog_install, null));
		if (presenter.getInstalled() == null) {
			this.activity = activity;
			setCancelable(false);
		} else {
			this.activity = null;
		}
	}

	@Override
	public Activity getActivity() {
		return activity;
	}

	@Override
	public void setupOnCreate() {
		final Window window = getWindow();
		final WindowManager.LayoutParams params = window.getAttributes();
		params.width = getContext().getResources().getDimensionPixelSize(R.dimen.install_dialog_width);
		window.setAttributes(params);
	}

	@Override
	public void showList(InstallPackage[] list) {
		final ListView listView = (ListView) findViewById(R.id.list);
		final TextView view = (TextView) findViewById(R.id.title);
		view.setVisibility(View.VISIBLE);
		view.setText(
			Html.fromHtml(
				getContext().getString(
					R.string.select_package_to_install, getContext().getString(R.string.title_server)
				)
			)
		);
		listView.setVisibility(View.VISIBLE);
		listView.setAdapter(new InstallPackagesAdapter(list, presenter.getInstalled()));
		listView.setOnItemClickListener(
			new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					presenter.install(((InstallPackagesAdapter) parent.getAdapter()).getItem(position));
				}
			}
		);
	}

	@Override
	public void showMessage(boolean preloader, @StringRes int message, String... argument) {
		final View button = findViewById(R.id.preloader_button_ok);
		findViewById(R.id.preloader_container).setVisibility(View.VISIBLE);
		findViewById(R.id.preloader).setVisibility(preloader ? View.VISIBLE : View.GONE);
		findViewById(R.id.title).setVisibility(View.GONE);
		findViewById(R.id.list).setVisibility(View.GONE);
		((TextView)findViewById(R.id.preloader_label))
			.setText(Html.fromHtml(getContext().getString(message, argument)));
		button.setVisibility(preloader ? View.GONE : View.VISIBLE);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				presenter.downloadList();
			}
		});
	}

	@Override
	public void hideMessage() {
		findViewById(R.id.preloader_container).setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		if (activity == null) {
			super.onBackPressed();
		} else {
			activity.finish();
		}
	}

	@Override
	public void showMessageInstalling(InstallPackage model) {
		showMessage(true, R.string.installing_package, model.getTitle(getContext()));
	}

	@Override
	public void showInstallFailedMessage(InstallPackage model, Throwable error) {
		showMessage(
			true, R.string.install_package_failed, model.getTitle(getContext()), error.getMessage()
		);
	}
}
