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
package com.esminis.server.library.activity.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.dialog.about.AboutPresenter;
import com.esminis.server.library.dialog.about.AboutPresenterImpl;
import com.esminis.server.library.dialog.about.AboutViewImpl;
import com.esminis.server.library.dialog.directorychooser.DirectoryChooserPresenter;
import com.esminis.server.library.dialog.directorychooser.DirectoryChooserPresenterImpl;
import com.esminis.server.library.dialog.directorychooser.DirectoryChooserView;
import com.esminis.server.library.dialog.directorychooser.DirectoryChooserViewImpl;
import com.esminis.server.library.dialog.directorychooser.OnDirectoryChooserListener;
import com.esminis.server.library.dialog.install.InstallPresenterImpl;
import com.esminis.server.library.dialog.install.InstallViewImpl;
import com.esminis.server.library.model.InstallPackage;
import com.esminis.server.library.model.Network;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public class MainViewImpl implements MainView {

	private final Context context;
	private final MainPresenter presenter;
	private com.esminis.server.library.dialog.Dialog dialog = null;

	private final TextView viewDocumentRoot;
	private final TextView viewLog;
	private final TextView viewPort;
	private final TextView viewInstalledPackage;
	private final Spinner viewServerInterfaces;
	private final TextView viewServerStatusLabel;
	private final ActionBarDrawerToggle drawerToggle;
	private final DrawerLayout drawerLayout;
	private final View viewContainer;
	private final ActionBar actionBar;
	private final WeakReference<AppCompatActivity> activity;
	private final String titleDefault;
	private boolean drawerEnabled = false;
	private final View buttonStart;
	private final View buttonStop;

	MainViewImpl(AppCompatActivity activity, final MainPresenter presenter) {
		this.context = activity.getApplicationContext();
		this.activity = new WeakReference<>(activity);
		this.presenter = presenter;
		viewServerInterfaces = (Spinner)activity.findViewById(R.id.server_interface);
		viewDocumentRoot = (TextView)activity.findViewById(R.id.server_root);
		viewLog = (TextView)activity.findViewById(R.id.error);
		viewPort = (TextView)activity.findViewById(R.id.server_port);
		viewInstalledPackage = (TextView)activity.findViewById(R.id.server_build);
		viewServerStatusLabel = (TextView)activity.findViewById(R.id.label);
		titleDefault = context.getString(R.string.title);
		viewContainer = activity.findViewById(R.id.container);
		drawerLayout = (DrawerLayout)activity.findViewById(R.id.drawer_layout);
		buttonStart = activity.findViewById(R.id.start);
		buttonStop = activity.findViewById(R.id.stop);
		setupToolbar(activity);
		actionBar = activity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(titleDefault);
		}
		drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, R.string.open, R.string.close) {

			private void invalidateOptionsMenu() {
				final Activity activity = MainViewImpl.this.activity.get();
				if (activity != null) {
					activity.invalidateOptionsMenu();
				}
			}

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				if (actionBar != null && context.getApplicationInfo() != null) {
					actionBar.setTitle(titleDefault);
				}
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (actionBar != null) {
					actionBar.setTitle(R.string.settings);
				}
				invalidateOptionsMenu();
			}
		};
		setupDrawer();
		setupListeners(activity);
		showButton(BUTTON_NONE);
		setStatusLabel(activity.getString(R.string.server_status_updating));
	}

	private void setupDrawer() {
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		}
		drawerToggle.setDrawerIndicatorEnabled(true);
	}

	private void setupListeners(Activity activity) {
		viewDocumentRoot.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				presenter.showDocumentRootChooser();
			}
		});
		viewPort.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					final Activity activity = MainViewImpl.this.activity.get();
					if (activity != null) {
						((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(text.getWindowToken(), 0);
						return true;
					}
				}
				return false;
			}
		});
		viewPort.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			public void afterTextChanged(Editable text) {
				presenter.portModified(text.toString());
			}
		});
		activity.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				presenter.serverStart();
			}
		});
		activity.findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				presenter.serverStop();
			}
		});
		viewContainer.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				removeFocus();
				return true;
			}
		});
		activity.findViewById(R.id.preloader_button_ok).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					presenter.requestPermission();
				}
			}
		);
		buttonStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				presenter.serverStart();
			}
		});
		buttonStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				presenter.serverStop();
			}
		});
		viewInstalledPackage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				presenter.requestPackageInstall();
			}
		});
	}

	@Override
	public void setDocumentRoot(String documentRoot) {
		viewDocumentRoot.setText(documentRoot);
	}

	@Override
	public void setPort(String port, boolean valid) {
		final CharSequence portOld = viewPort.getText().toString();
		if (!portOld.equals(port)) {
			viewPort.setText(port);
		}
		viewPort.setTextColor(valid ? Color.BLACK : Color.RED);
	}

	@Override
	public void setLog(CharSequence log) {
		viewLog.setText(log);
		viewLog.scrollTo(
			0, Math.max((viewLog.getLineHeight() * viewLog.getLineCount()) - viewLog.getHeight(), 0)
		);
	}

	@Override
	public CharSequence getLog() {
		return viewLog.getText();
	}

	@Override
	public void setInstallPackages(InstallPackage installed, InstallPackage newest) {
		if (viewInstalledPackage == null || installed == null) {
			return;
		}
		viewInstalledPackage.setText(
			Html.fromHtml(
				installed.getTitle(viewInstalledPackage.getContext()) + "<small> - " + (
					newest == null || newest.equals(installed) ?
						context.getString(R.string.install_package_newest) : context.getString(
							R.string.install_package_not_newest,
							newest.getTitle(viewInstalledPackage.getContext())
						)
				) + "</small>"
			)
		);
	}

	@Override
	public boolean createMenu(MenuInflater inflater, Menu menu) {
		inflater.inflate(R.menu.main, menu);
		final Activity activity = this.activity.get();
		if (activity != null) {
			menu.findItem(R.id.menu_about).setIcon(
				new InsetDrawable(
					VectorDrawableCompat.create(activity.getResources(), R.drawable.ic_info, null), 0
				)
			);
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(MenuItem item) {
		if (drawerEnabled && drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (item.getItemId() == R.id.menu_about) {
			presenter.showAbout();
			return true;
		}
		return false;
	}

	@Override
	public void showDocumentRootChooser(File root) {
		final Activity activity = this.activity.get();
		if (activity != null) {
			final DirectoryChooserPresenter presenter = new DirectoryChooserPresenterImpl();
			final DirectoryChooserViewImpl dialog = new DirectoryChooserViewImpl(activity, presenter);
			presenter.setView(dialog);
			presenter.setDirectory(root);
			presenter.setOnDirectoryChooserListener(
				new OnDirectoryChooserListener() {
					public void OnDirectoryChosen(File directory) {
						MainViewImpl.this.presenter.onDocumentRootChosen(directory);
					}
				}
			);
			showDialog(dialog);
		}
	}

	@Override
	public void closeDialog() {
		if (dialog != null) {
			dialog.dismiss();
			dialog.setOnDismissListener(null);
			dialog = null;
		}
	}

	private Observable<Void> showDialog(com.esminis.server.library.dialog.Dialog dialog) {
		closeDialog();
		this.dialog = dialog;
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (MainViewImpl.this.dialog == dialog) {
					closeDialog();
				}
			}
		});
		return dialog.showObserved();
	}

	@Override
	public void syncDrawer() {
		if (drawerEnabled) {
			drawerToggle.syncState();
		}
	}

	@Override
	public void showMainContent() {
		drawerEnabled = true;
		drawerLayout.setDrawerListener(drawerToggle);
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		drawerToggle.setDrawerIndicatorEnabled(true);
		syncDrawer();
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		setMessage(false, false, null, null);
		viewContainer.setVisibility(View.VISIBLE);
		removeFocus();
	}

	private void removeFocus() {
		viewContainer.requestFocus();
		((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(viewContainer.getApplicationWindowToken(), 0);
	}

	@Override
	public void setMessage(
		boolean preloaderBackground, boolean preloader, String buttonTitle, String message
	) {
		final Activity activity = this.activity.get();
		if (activity == null) {
			return;
		}
		activity.findViewById(R.id.preloader_container)
			.setVisibility(preloaderBackground ? View.VISIBLE : View.GONE);
		if (preloaderBackground) {
			final Button button = (Button)activity.findViewById(R.id.preloader_button_ok);
			button.setVisibility(buttonTitle != null ? View.VISIBLE : View.GONE);
			button.setText(buttonTitle);
			activity.findViewById(R.id.preloader).setVisibility(preloader ? View.VISIBLE : View.GONE);
			TextView textView = (TextView)activity.findViewById(R.id.preloader_label);
			textView.setMovementMethod(new ScrollingMovementMethod());
			textView.setText(message);
		}
	}

	private Context getThemeContext() {
		final Activity activity = this.activity.get();
		return activity == null ? context : activity;
	}

	@Override
	public void setServerInterfaces(List<Network> list, int selectedPosition) {
		viewServerInterfaces.setAdapter(
			new ArrayAdapter<>(getThemeContext(), android.R.layout.simple_spinner_dropdown_item, list)
		);
		viewServerInterfaces.setOnItemSelectedListener(null);
		viewServerInterfaces.setSelection(selectedPosition);
		viewServerInterfaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				presenter.onServerInterfaceChanged(position);
			}

			public void onNothingSelected(AdapterView<?> parent) {}

		});
	}

	@Override
	public void setStatusLabel(CharSequence label) {
		viewServerStatusLabel.setText(label);
		viewServerStatusLabel.setLinksClickable(true);
		viewServerStatusLabel.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void showButton(int button) {
		buttonStart.setVisibility(button == BUTTON_START ? View.VISIBLE : View.GONE);
		buttonStop.setVisibility(button == BUTTON_STOP ? View.VISIBLE : View.GONE);
	}

	private void setupToolbar(@NonNull AppCompatActivity activity) {
		Toolbar toolbar = (Toolbar)activity.findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setLogo(
				VectorDrawableCompat.create(activity.getResources(), R.drawable.ic_toolbar, null)
			);
			toolbar.inflateMenu(R.menu.main);
			TypedValue attribute = new TypedValue();
			activity.getTheme().resolveAttribute(android.R.attr.textColorPrimary, attribute, true);
			if (attribute.resourceId > 0) {
				final VectorDrawableCompat icon = VectorDrawableCompat.create(
					activity.getResources(), R.drawable.ic_info, null
				);
				if (icon != null) {
					icon.setTint(ContextCompat.getColor(activity, attribute.resourceId));
					toolbar.getMenu().findItem(R.id.menu_about).setIcon(new InsetDrawable(icon, 0));
				}
			}
			activity.setSupportActionBar(toolbar);
		}
	}

	@Override
	public void showInstall(final InstallPresenterImpl presenter) {
		final Activity activity = this.activity.get();
		if (activity != null) {
			setMessage(false, false, null, null);
			final InstallViewImpl dialog = new InstallViewImpl(activity, presenter);
			presenter.setView(dialog);
			showDialog(dialog).subscribe(new Action1<Void>() {
				@Override
				public void call(Void o) {
					MainViewImpl.this.presenter.onInstallComplete();
					dialog.dismiss();
				}
			});
		}
	}

	@Override
	public void showAbout() {
		final Activity activity = this.activity.get();
		if (activity != null) {
			final AboutPresenter presenter =
				new AboutPresenterImpl((LibraryApplication)context.getApplicationContext());
			final AboutViewImpl dialog = new AboutViewImpl(activity, presenter);
			presenter.setView(dialog);
			showDialog(dialog);
		}
	}

}
