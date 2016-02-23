package com.esminis.server.library.dialog.install;

import android.annotation.SuppressLint;
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

import org.json.JSONException;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLException;

public class InstallViewImpl extends Dialog<InstallPresenter> implements InstallView {

	private final Activity activity;
	private final boolean isCancelable;

	@SuppressLint("InflateParams")
	public InstallViewImpl(Activity activity, final InstallPresenter presenter) {
		super(activity, presenter);
		setView(LayoutInflater.from(activity).inflate(R.layout.dialog_install, null));
		this.activity = activity;
		setCancelable(isCancelable = presenter.getInstalled() != null);
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
		setCancelable(isCancelable);
	}

	@Override
	public void onBackPressed() {
		if (isCancelable && !presenter.isInstalling()) {
			super.onBackPressed();
		} else {
			activity.finish();
		}
	}

	@Override
	public void showMessageInstall(
		InstallPackage model, @StringRes int message, String... arguments
	) {
		List<String> argumentsLocal = new ArrayList<>();
		argumentsLocal.add(model.getTitle(getContext()));
		Collections.addAll(argumentsLocal, arguments);
		showMessage(true, message, argumentsLocal.toArray(new String[argumentsLocal.size()]));
		setCancelable(false);
	}

	@Override
	public void showMessageInstallFailed(InstallPackage model, Throwable error) {
		showMessageError(R.string.install_package_failed, error, model.getTitle(getContext()));
		setCancelable(isCancelable);
	}

	@Override
	public void showMessageError(@StringRes int message, Throwable error, String... arguments) {
		final List<String> argumentsLocal = new ArrayList<>();
		final String errorMessage;
		if (error instanceof JSONException) {
			errorMessage = getContext().getString(R.string.error_server_response);
		} else if (
			error instanceof ConnectException || error instanceof NoRouteToHostException ||
			error instanceof SSLException || error instanceof SocketTimeoutException ||
			error instanceof SocketException || error instanceof UnknownHostException ||
			error instanceof MalformedURLException
		) {
			errorMessage = getContext().getString(R.string.error_network);
		} else {
			errorMessage = error.getClass().getSimpleName() +
				(error.getMessage() == null ? "" : ", " + error.getMessage());
		}
		argumentsLocal.add(errorMessage);
		Collections.addAll(argumentsLocal, arguments);
		showMessage(false, message, argumentsLocal.toArray(new String[argumentsLocal.size()]));
	}

}
