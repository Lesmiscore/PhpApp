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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.esminis.model.manager.Manager;
import com.esminis.popup.About;
import com.esminis.popup.DirectoryChooser;
import com.esminis.model.manager.Network;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.install.InstallServer;
import java.io.File;

public class MainActivity extends Activity implements InstallServer.OnInstallListener {
	
	private BroadcastReceiver receiver = null;
	
	private Preferences preferences = null;
	
	private Network network = null;
	
	private boolean requestResultView = false;
	
	private boolean requestResultViewSuccess = false;
	
	private boolean paused = true;

	private Dialog dialog = null;
	
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
		InstallServer.getInstance(this).installIfNeeded(this);
		findViewById(R.id.container).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				view.requestFocus();
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
				return true;
			}
		});
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
	
	private void setLabel(String label) {
		((TextView)findViewById(R.id.label)).setText(label);
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
		Php.getInstance(MainActivity.this).requestStatus();
		findViewById(R.id.container).requestFocus();
	}

	@Override
	protected void onPause() {
		super.onPause();
		paused = true;
		if (receiver != null) {
			unregisterReceiver(receiver);
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
		}
		findViewById(R.id.preloader).setVisibility(View.GONE);
		findViewById(R.id.preloader_container).setVisibility(View.GONE);
		findViewById(R.id.container).setVisibility(View.VISIBLE);
	}
	
	private void startup() {
		network = Manager.get(Network.class);
		
		Spinner spinner = (Spinner)findViewById(R.id.server_interface);
		spinner.setAdapter(
			new ArrayAdapter<com.esminis.model.Network>(
				this, android.R.layout.simple_spinner_dropdown_item, network.get()
			)
		);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				getPreferences().set(MainActivity.this, Preferences.ADDRESS, network.get(position).name);
			}

			public void onNothingSelected(AdapterView<?> parent) {}

		});
		spinner.setSelection(
			network.getPosition(getPreferences().getString(MainActivity.this, Preferences.ADDRESS))
		);

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
							((TextView)findViewById(R.id.server_root)).setText(
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
					((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(text.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
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
					error = false;
				}
				((TextView)findViewById(R.id.server_port))
					.setTextColor(error ? Color.RED : Color.BLACK);
			}
		});
		text.setText(getPreferences().getString(MainActivity.this, Preferences.PORT));
		CheckBox checkbox = (CheckBox)findViewById(R.id.server_start_on_boot);
		checkbox.setOnCheckedChangeListener(
			new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(
					CompoundButton checkox, boolean checked
				) {
					getPreferences().set(MainActivity.this, Preferences.START_ON_BOOT, checked);
				}
			}
		);
		checkbox.setChecked(
			getPreferences().getBoolean(MainActivity.this, Preferences.START_ON_BOOT)
		);

		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() != null && intent.getAction().equals(Php.INTENT_ACTION)) {
					Bundle extras = intent.getExtras();
					if (extras != null && extras.containsKey("errorLine")) {
						TextView text = (TextView)findViewById(R.id.error);
						String message = extras.getString("errorLine");
						Spannable textLine = new Spannable.Factory().newSpannable(message);
						textLine.setSpan(
							new ForegroundColorSpan(
								message.matches("^.+: /[^ ]*$") ? Color.rgb(0, 0x66, 0) : Color.RED
							), 0, message.length(), 0
						);
						text.append(text.getText() != null && text.getText().length() > 0 ? "\n" : "");
						text.append(textLine);
						text.scrollTo(0, (text.getLineHeight() * text.getLineCount()) - text.getHeight());
					} else {
						findViewById(R.id.start).setVisibility(View.GONE);
						findViewById(R.id.stop).setVisibility(View.GONE);
						if (extras != null && extras.getBoolean("running")) {
							findViewById(R.id.stop).setVisibility(View.VISIBLE);
							setLabel(
								String.format(getString(R.string.server_running), extras.getString("address"))
							);
						} else {
							findViewById(R.id.start).setVisibility(View.VISIBLE);
							setLabel(getString(R.string.server_stopped));
						}
					}					
				}
			}

		};

		findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				((TextView)findViewById(R.id.error)).setText("");
				Php.getInstance(MainActivity.this).requestStart();
			}
		});

		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Php.getInstance(MainActivity.this).requestStop();
			}
		});
		
		registerReceiver(receiver, new IntentFilter(Php.INTENT_ACTION));
		Php.getInstance(MainActivity.this).requestStatus();
	}

	@Override
	public void OnInstallNewVersionRequest(final InstallServer installer) {
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setMessage(
				getString(R.string.server_install_new_version_question, getString(R.string.php_build))
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
}
