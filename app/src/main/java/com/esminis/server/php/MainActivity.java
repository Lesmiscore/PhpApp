/**
 * Copyright 2014 Tautvydas Andrikys
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
package com.esminis.server.php;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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

import com.esminis.model.manager.Network;
import com.esminis.popup.About;
import com.esminis.popup.DirectoryChooser;
import com.esminis.server.php.model.manager.Log;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.ServerNotification;
import com.esminis.server.php.service.background.BackgroundService;
import com.esminis.server.php.service.background.install.InstallServer;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.service.server.tasks.RestartIfRunningServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StartServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StatusServerTaskProvider;
import com.esminis.server.php.service.server.tasks.StopServerTaskProvider;

import java.io.File;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements InstallServer.OnInstallListener {
	
	private BroadcastReceiver receiver = null;
	private BroadcastReceiver receiverNetwork = null;

	@Inject
	protected Preferences preferences = null;

	@Inject
	protected Network network = null;

	@Inject
	protected InstallServer installServer = null;

	@Inject
	protected Log log = null;

	@Inject
	protected ActivityHelper activityHelper;

	@Inject
	protected ServerNotification serverNotification;

	private boolean requestResultView = false;
	
	private boolean requestResultViewSuccess = false;
	
	private boolean paused = true;

	private Dialog dialog = null;

	private ActionBarDrawerToggle drawerToggle = null;

	private boolean syncedDrawerState = false;

	private String titleDefault = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		android.app.Application application = getApplication();
		if (application instanceof Application) {
			((Application)application).getObjectGraph().inject(this);
		}
		setContentView(R.layout.main);		
		if (savedInstanceState != null) {
			TextView text = (TextView)findViewById(R.id.error);
			text.setText(savedInstanceState.getCharSequence("errors"));
		}
		titleDefault = getString(R.string.title) + " " + getString(R.string.php_version);
		activityHelper.createToolbar(this);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(titleDefault);
		}
		installServer.installIfNeeded(this, this);
		findViewById(R.id.container).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				removeFocus();
				return true;
			}
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (drawerToggle != null) {
			drawerToggle.syncState();
			syncedDrawerState = true;
		}
	}

	private void removeFocus() {
		View view = findViewById(R.id.container);
		view.requestFocus();
		((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		TextView view = (TextView)findViewById(R.id.error);
		if (view != null && view.getText() != null) {
			outState.putCharSequence("errors", view.getText());
		}
	}
	
	private void setLabel(CharSequence label) {
		TextView view = (TextView)findViewById(R.id.label);
		view.setText(label);
		view.setLinksClickable(true);
		view.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		activityHelper.onResume(this);
		paused = false;
		if (requestResultView) {
			requestResultView = false;
			resultView();
		}
		if (receiver != null) {
			registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		}
		if (receiverNetwork != null) {
			registerReceiver(receiverNetwork, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
		resetNetwork();
		BackgroundService.execute(getApplication(), StatusServerTaskProvider.class).subscribe();
		resetLog();
		removeFocus();
	}

	@Override
	protected void onPause() {
		super.onPause();
		activityHelper.onPause();
		paused = true;
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		if (receiverNetwork != null) {
			unregisterReceiver(receiverNetwork);
		}
	}

	private void requestResultView() {
		if (paused) {
			requestResultView = true;
		} else {
			resultView();
		}
	}
	
	private void resultView() {
		if (requestResultViewSuccess) {
			startup();
			findViewById(R.id.preloader_container).setVisibility(View.GONE);
			findViewById(R.id.container).setVisibility(View.VISIBLE);
			removeFocus();
		} else {
			((TextView)findViewById(R.id.preloader_label)).setText(R.string.server_installation_failed);
		}
		findViewById(R.id.preloader).setVisibility(View.GONE);
	}
	
	private void startup() {
		DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		}
		drawerLayout.setDrawerListener(
			drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) {

				public void onDrawerClosed(View view) {
					super.onDrawerClosed(view);
					ActionBar bar = getSupportActionBar();
					ApplicationInfo info = getApplicationInfo();
					if (bar != null && info != null) {
						bar.setTitle(titleDefault);
					}
					invalidateOptionsMenu();
				}

				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					ActionBar bar = getSupportActionBar();
					if (bar != null) {
						bar.setTitle(R.string.settings);
					}
					invalidateOptionsMenu();
				}
			}
		);
		drawerToggle.setDrawerIndicatorEnabled(true);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		if (!syncedDrawerState) {
			drawerToggle.syncState();
			syncedDrawerState = true;
		}

		resetNetwork();

		TextView text = (TextView)findViewById(R.id.server_root);		
		text.setText(preferences.getString(MainActivity.this, Preferences.DOCUMENT_ROOT));
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				DirectoryChooser chooser = new DirectoryChooser(MainActivity.this);
				chooser.setParent(
					new File(preferences.getString(MainActivity.this, Preferences.DOCUMENT_ROOT))
				);
				chooser.setOnDirectoryChooserListener(
					new DirectoryChooser.OnDirectoryChooserListener() {
						public void OnDirectoryChosen(File directory) {
							preferences.set(
								MainActivity.this, Preferences.DOCUMENT_ROOT, directory.getAbsolutePath()
							);
							BackgroundService.execute(getApplication(), RestartIfRunningServerTaskProvider.class).subscribe();
							((TextView) findViewById(R.id.server_root)).setText(
								preferences.getString(MainActivity.this, Preferences.DOCUMENT_ROOT)
							);
						}
					}
				);
				chooser.show();
			}
		});
		text = (TextView)findViewById(R.id.server_port);
		text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(text.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		text.setText(preferences.getString(MainActivity.this, Preferences.PORT));
		text.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			public void afterTextChanged(Editable text) {
				String portPreference = preferences.getString(MainActivity.this, Preferences.PORT);
				int port = portPreference.isEmpty() ? 
					8080 : Integer.parseInt(portPreference);
				try {
					port = Integer.parseInt(text.toString());					
				} catch (NumberFormatException ignored) {}
				boolean error = true;
				if (port >= 1024 && port <= 65535) {
					preferences.set(MainActivity.this, Preferences.PORT, String.valueOf(port));
					BackgroundService.execute(getApplication(), RestartIfRunningServerTaskProvider.class).subscribe();
					error = false;
				}
				((TextView)findViewById(R.id.server_port))
					.setTextColor(error ? Color.RED : Color.BLACK);
			}
		});

		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (Php.INTENT_ACTION.equals(intent.getAction())) {
					Bundle extras = intent.getExtras();
					if (extras != null && extras.containsKey("errorLine")) {
						resetLog();
					} else {
						findViewById(R.id.start).setVisibility(View.GONE);
						findViewById(R.id.stop).setVisibility(View.GONE);
						if (extras != null && extras.getBoolean("running")) {
							findViewById(R.id.stop).setVisibility(View.VISIBLE);
							Spanned title = Html.fromHtml(
								String.format(getString(R.string.server_running), extras.getString("address"))
							);
							setLabel(title);
							serverNotification.show(
								MainActivity.this, title.toString(), getString(R.string.server_running_public)
							);
						} else {
							findViewById(R.id.start).setVisibility(View.VISIBLE);
							setLabel(getString(R.string.server_stopped));
							serverNotification.hide(MainActivity.this);
						}
					}					
				}
			}

		};
		receiverNetwork = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				resetNetwork();
			}
		};

		findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				((TextView)findViewById(R.id.error)).setText("");
				log.clear(view.getContext());
				resetLog();
				BackgroundService.execute(getApplication(), StartServerTaskProvider.class).subscribe();
			}
		});

		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				BackgroundService.execute(getApplication(), StopServerTaskProvider.class).subscribe();
				resetLog();
			}
		});
		
		registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		registerReceiver(receiverNetwork, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		BackgroundService.execute(getApplication(), StatusServerTaskProvider.class).subscribe();
	}

	private void resetLog() {
		TextView text = (TextView)findViewById(R.id.error);
		text.setText(log.get());
		text.scrollTo(0, Math.max((text.getLineHeight() * text.getLineCount()) - text.getHeight(), 0));
	}

	@Override
	public void OnInstallNewVersionRequest(final InstallServer installer) {
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setMessage(
				getString(R.string.server_install_new_version_question, preferences.getPhpBuild(this))
			)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((AlertDialog) dialog).setOnDismissListener(null);
					installer.continueInstall(MainActivity.this, true);
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((AlertDialog)dialog).setOnDismissListener(null);
					installer.continueInstall(MainActivity.this, false);
				}
			}).show();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				installer.continueInstall(MainActivity.this, false);
			}
		});
	}

	public void OnInstallEnd(boolean success) {
		requestResultViewSuccess = success;
		requestResultView();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		if (item.getItemId() == R.id.menu_about) {
			dialog = new About(this);
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					MainActivity.this.dialog = null;
				}
			});
			dialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	private void resetNetwork() {
		Spinner spinner = (Spinner)findViewById(R.id.server_interface);
		if (spinner == null || network == null) {
			return;
		}
		boolean changed = network.refresh();
		spinner.setAdapter(
			new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, network.get())
		);
		spinner.setOnItemSelectedListener(null);
		spinner.setSelection(
			network.getPosition(preferences.getString(MainActivity.this, Preferences.ADDRESS))
		);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String value = preferences.getString(MainActivity.this, Preferences.ADDRESS);
				String newValue = network.get(position).name;
				if (value.equals(newValue)) {
					return;
				}
				preferences.set(MainActivity.this, Preferences.ADDRESS, newValue);
				BackgroundService.execute(getApplication(), RestartIfRunningServerTaskProvider.class).subscribe();

			}

			public void onNothingSelected(AdapterView<?> parent) {}

		});
		if (changed) {
			BackgroundService.execute(getApplication(), RestartIfRunningServerTaskProvider.class).subscribe();
		}
	}

}
