package com.esminis.server.library.activity.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.dialog.DirectoryChooser;
import com.esminis.server.library.dialog.about.About;
import com.esminis.server.library.model.Network;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class MainViewImpl implements MainView {

	private final Context context;
	private final MainPresenter presenter;
	private Dialog dialog = null;

	private final TextView viewDocumentRoot;
	private final TextView viewLog;
	private final TextView viewPort;
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
		viewServerStatusLabel = (TextView)activity.findViewById(R.id.label);
		titleDefault = context.getString(R.string.title) + " " + context.getString(R.string.version);
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
	public boolean createMenu(MenuInflater inflater, Menu menu) {
		inflater.inflate(R.menu.main, menu);
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
	public void showAbout() {
		final Activity activity = this.activity.get();
		if (activity != null) {
			showDialog(new About(activity), null);
		}
	}

	@Override
	public void showDocumentRootChooser(File root) {
		DirectoryChooser chooser = new DirectoryChooser(getThemeContext());
		chooser.setParent(root);
		chooser.setOnDirectoryChooserListener(
			new DirectoryChooser.OnDirectoryChooserListener() {
				public void OnDirectoryChosen(File directory) {
					presenter.onDocumentRootChosen(directory);
				}
			}
		);
		chooser.show();
	}

	@Override
	public void showInstallNewVersionRequest(CharSequence message) {
		showDialog(
			new AlertDialog.Builder(getThemeContext())
				.setMessage(message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						((AlertDialog) dialog).setOnDismissListener(null);
						presenter.onInstallNewVersionResponse(true);
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						((AlertDialog) dialog).setOnDismissListener(null);
						presenter.onInstallNewVersionResponse(false);
					}
				}).create(),
			new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					presenter.onInstallNewVersionResponse(false);
				}
			}
		);
	}

	@Override
	public void closeDialog() {
		if (dialog != null) {
			dialog.dismiss();
			dialog.setOnDismissListener(null);
			dialog = null;
		}
	}

	private void showDialog(Dialog dialog, final DialogInterface.OnDismissListener listener) {
		this.dialog = dialog;
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				closeDialog();
				if (listener != null) {
					listener.onDismiss(dialog);
				}
			}
		});
		dialog.show();
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
		setMessage(false, false, false, null);
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
		boolean containerVisible, boolean preloader, boolean button, String message
	) {
		final Activity activity = this.activity.get();
		if (activity == null) {
			return;
		}
		activity.findViewById(R.id.preloader_container)
			.setVisibility(containerVisible ? View.VISIBLE : View.GONE);
		if (containerVisible) {
			activity.findViewById(R.id.preloader).setVisibility(preloader ? View.VISIBLE : View.GONE);
			activity.findViewById(R.id.preloader_button_ok).setVisibility(button ? View.VISIBLE : View.GONE);
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

			public void onNothingSelected(AdapterView<?> parent) {
			}

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
			toolbar.setLogo(R.drawable.ic_toolbar);
			toolbar.inflateMenu(R.menu.main);
			TypedValue attribute = new TypedValue();
			activity.getTheme().resolveAttribute(android.R.attr.textColorPrimary, attribute, true);
			if (attribute.resourceId > 0) {
				MenuItem item = toolbar.getMenu().findItem(R.id.menu_about);
				Drawable icon = DrawableCompat.wrap(item.getIcon());
				DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN);
				DrawableCompat.setTint(icon, ContextCompat.getColor(activity, attribute.resourceId));
				item.setIcon(icon);
			}
			activity.setSupportActionBar(toolbar);
		}
	}

}
