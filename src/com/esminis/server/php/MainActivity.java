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

import android.app.ActionBar;
import android.app.Activity;
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
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
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

import com.esminis.model.manager.Manager;
import com.esminis.popup.About;
import com.esminis.popup.DirectoryChooser;
import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Log;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.install.InstallServer;
import java.io.File;

public class MainActivity extends Activity implements InstallServer.OnInstallListener {
	
	private BroadcastReceiver receiver = null;
	private BroadcastReceiver receiverNetwork = null;
	
	private Preferences preferences = null;
	
	private Network network = null;
	
	private boolean requestResultView = false;
	
	private boolean requestResultViewSuccess = false;
	
	private boolean paused = true;

	private Dialog dialog = null;

	private ActionBarDrawerToggle drawerToggle = null;

	private boolean syncedDrawerState = false;

	private String titleDefault = null;
	
	private Preferences getPreferences() {
		if (preferences == null) {
			preferences = Manager.get(Preferences.class);
		}
		return preferences;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		if (savedInstanceState != null) {
			TextView text = (TextView)findViewById(R.id.error);
			text.setText(savedInstanceState.getCharSequence("errors"));
		}
		titleDefault = getString(R.string.title_with_version, getString(R.string.php_version));
		getActionBar().setTitle(titleDefault);
		InstallServer.getInstance(this).installIfNeeded(this);
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
		Php.getInstance(MainActivity.this).requestStatus();
		resetLog();
		removeFocus();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
		drawerLayout.setDrawerListener(
			drawerToggle = new ActionBarDrawerToggle(
				this, drawerLayout, R.drawable.ic_navigation_drawer, R.string.open, R.string.close
			) {

				public void onDrawerClosed(View view) {
					super.onDrawerClosed(view);
					ActionBar bar = getActionBar();
					ApplicationInfo info = getApplicationInfo();
					if (bar != null && info != null) {
						bar.setTitle(titleDefault);
					}
					invalidateOptionsMenu();
				}

				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					ActionBar bar = getActionBar();
					if (bar != null) {
						bar.setTitle(R.string.settings);
					}
					invalidateOptionsMenu();
				}
			}
		);
		drawerToggle.setDrawerIndicatorEnabled(true);

		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		if (!syncedDrawerState) {
			drawerToggle.syncState();
			syncedDrawerState = true;
		}
		network = Manager.get(Network.class);

		resetNetwork();

		TextView text = (TextView)findViewById(R.id.server_root);		
		text.setText(getPreferences().getString(MainActivity.this, Preferences.DOCUMENT_ROOT));
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				DirectoryChooser chooser = new DirectoryChooser(MainActivity.this);
				chooser.setParent(
					new File(getPreferences().getString(MainActivity.this, Preferences.DOCUMENT_ROOT))
				);
				chooser.setOnDirectoryChooserListener(
					new DirectoryChooser.OnDirectoryChooserListener() {
						public void OnDirectoryChosen(File directory) {
							getPreferences().set(
								MainActivity.this, Preferences.DOCUMENT_ROOT, directory.getAbsolutePath()
							);
							Php.getInstance(MainActivity.this).requestRestartIfRunning();
							((TextView) findViewById(R.id.server_root)).setText(
								getPreferences().getString(MainActivity.this, Preferences.DOCUMENT_ROOT)
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
		text.setText(getPreferences().getString(MainActivity.this, Preferences.PORT));
		text.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			public void afterTextChanged(Editable text) {
				String portPreference = getPreferences().getString(MainActivity.this, Preferences.PORT);
				int port = portPreference.isEmpty() ? 
					8080 : Integer.parseInt(portPreference);
				try {
					port = Integer.parseInt(text.toString());					
				} catch (NumberFormatException ignored) {}
				boolean error = true;
				if (port >= 1024 && port <= 65535) {
					getPreferences().set(MainActivity.this, Preferences.PORT, String.valueOf(port));
					Php.getInstance(MainActivity.this).requestRestartIfRunning();
					error = false;
				}
				((TextView)findViewById(R.id.server_port))
					.setTextColor(error ? Color.RED : Color.BLACK);
			}
		});

		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() != null && intent.getAction().equals(Php.INTENT_ACTION)) {
					Bundle extras = intent.getExtras();
					if (extras != null && extras.containsKey("errorLine")) {
						resetLog();
					} else {
						findViewById(R.id.start).setVisibility(View.GONE);
						findViewById(R.id.stop).setVisibility(View.GONE);
						if (extras != null && extras.getBoolean("running")) {
							findViewById(R.id.stop).setVisibility(View.VISIBLE);
							setLabel(
								Html.fromHtml(
									String.format(getString(R.string.server_running), extras.getString("address"))
								)
							);
						} else {
							findViewById(R.id.start).setVisibility(View.VISIBLE);
							setLabel(getString(R.string.server_stopped));
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
				Manager.get(Log.class).clear(view.getContext());
				resetLog();
				Php.getInstance(MainActivity.this).requestStart();
			}
		});

		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Php.getInstance(MainActivity.this).requestStop();
				resetLog();
			}
		});
		
		registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		registerReceiver(receiverNetwork, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		Php.getInstance(MainActivity.this).requestStatus();
	}

	private void resetLog() {
		TextView text = (TextView)findViewById(R.id.error);
		text.setText(Manager.get(Log.class).get());
		text.scrollTo(0, Math.max((text.getLineHeight() * text.getLineCount()) - text.getHeight(), 0));
	}

	@Override
	public void OnInstallNewVersionRequest(final InstallServer installer) {
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setMessage(
				getString(
					R.string.server_install_new_version_question, Manager.get(Preferences.class)
						.getPhpBuild(this)
				)
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
			new ArrayAdapter<com.esminis.model.Network>(
				this, android.R.layout.simple_spinner_dropdown_item, network.get()
			)
		);
		spinner.setOnItemSelectedListener(null);
		spinner.setSelection(
			network.getPosition(getPreferences().getString(MainActivity.this, Preferences.ADDRESS))
		);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String value = getPreferences().getString(MainActivity.this, Preferences.ADDRESS);
				String newValue = network.get(position).name;
				if (value.equals(newValue)) {
					return;
				}
				getPreferences().set(MainActivity.this, Preferences.ADDRESS, newValue);
				Php.getInstance(MainActivity.this).requestRestartIfRunning();
			}

			public void onNothingSelected(AdapterView<?> parent) {}

		});
		if (changed) {
			Php.getInstance(MainActivity.this).requestRestartIfRunning();
		}
	}

}
