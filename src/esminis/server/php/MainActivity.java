package esminis.server.php;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import static android.content.Context.INPUT_METHOD_SERVICE;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import esminis.server.php.service.Network;
import esminis.server.php.service.PhpServer;
import esminis.server.php.service.Preferences;
import esminis.server.php.service.install.InstallServer;
import java.io.File;
import net.rdrei.android.dirchooser.DirectoryChooserActivity;

public class 
	MainActivity extends Activity implements InstallServer.OnInstallListener
{	
	
	static private int REQUEST_DIRECTORY = 1;
	
	private BroadcastReceiver receiver = null;
	
	private Preferences preferences = null;
	
	private Network network = null;
	
	private boolean requestResultView = false;
	
	private boolean requestResultViewSuccess = false;
	
	private boolean paused = true;
	
	private Preferences getPreferences() {
		if (preferences == null) {
			preferences = new Preferences(this);
		}
		return preferences;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		new InstallServer(this).installIfNeeded(this);
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
			registerReceiver(receiver, new IntentFilter(PhpServer.INTENT_ACTION));
		}
		PhpServer.getInstance(MainActivity.this).sendAction("status");
	}

	@Override
	protected void onPause() {
		super.onPause();
		paused = true;
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (
			requestCode == REQUEST_DIRECTORY && 
			resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED
		) {
			File file = new File(
				data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR)
			);
			if (file.isDirectory()) {
				getPreferences().set(
					Preferences.DOCUMENT_ROOT, file.getAbsolutePath()
				);
				((TextView)findViewById(R.id.server_root)).setText(
					getPreferences().getString(Preferences.DOCUMENT_ROOT)
				);
			}
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
		} else {
			error();
		}		
		findViewById(R.id.preloader).setVisibility(View.GONE);
		findViewById(R.id.preloader_container).setVisibility(View.GONE);
		findViewById(R.id.container).setVisibility(View.VISIBLE);
	}
	
	private void error() {
		
	}
	
	private void startup() {
		network = new Network();
		
		Spinner spinner = (Spinner)findViewById(R.id.server_interface);
		spinner.setAdapter(
			new ArrayAdapter(
				this, android.R.layout.simple_spinner_dropdown_item, network.getTitles()
			)
		);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(
				AdapterView<?> parent, View view, int position, long id
			) {
				getPreferences().set(Preferences.ADDRESS, network.getName(position));
			}

			public void onNothingSelected(AdapterView<?> parent) {}

		});
		spinner.setSelection(
			network.getPosition(getPreferences().getString(Preferences.ADDRESS))
		);

		TextView text = (TextView)findViewById(R.id.server_root);		
		text.setText(getPreferences().getString(Preferences.DOCUMENT_ROOT));	
		text.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				final Intent chooserIntent = new Intent(
					MainActivity.this, DirectoryChooserActivity.class
				);
				startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
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
				int port = Integer.parseInt(
					getPreferences().getString(Preferences.PORT)
				);
				try {
					port = Integer.parseInt(text.toString());					
				} catch (NumberFormatException e) {}
				boolean error = true;
				if (port >= 1024 && port <= 65535) {
					getPreferences().set(Preferences.PORT, String.valueOf(port));
					error = false;
				}
				((TextView)findViewById(R.id.server_port))
					.setTextColor(error ? Color.RED : Color.BLACK);
			}
		});
		text.setText(
			getPreferences().getString(Preferences.PORT)
		);
		CheckBox checkbox = (CheckBox)findViewById(R.id.server_start_on_boot);
		checkbox.setOnCheckedChangeListener(
			new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(
					CompoundButton checkox, boolean checked
				) {
					getPreferences().set(Preferences.START_ON_BOOT, checked);
				}
			}
		);
		checkbox.setChecked(
			getPreferences().getBoolean(Preferences.START_ON_BOOT)
		);

		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(PhpServer.INTENT_ACTION)) {
					Bundle extras = intent.getExtras();
					findViewById(R.id.start).setVisibility(View.GONE);
					findViewById(R.id.stop).setVisibility(View.GONE);
					if (extras.getBoolean("running")) {
						findViewById(R.id.stop).setVisibility(View.VISIBLE);
						setLabel(
							String.format(
								getString(R.string.server_running), extras.getString("address")
							)
						);
					} else {
						findViewById(R.id.start).setVisibility(View.VISIBLE);
						setLabel(getString(R.string.server_stopped));
					}
				}
			}

		};

		findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				PhpServer.getInstance(MainActivity.this).sendAction("start");
			}
		});

		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				PhpServer.getInstance(MainActivity.this).sendAction("stop");
			}
		});
		
		registerReceiver(receiver, new IntentFilter(PhpServer.INTENT_ACTION));
		PhpServer.getInstance(MainActivity.this).sendAction("status");
	}
	
	public void OnInstallStart() {}

	public void OnInstallEnd(boolean success) {
		requestResultViewSuccess = success;
		requestResultView();
	}
	
}
