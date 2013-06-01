package esminis.server.php;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import static android.content.Context.INPUT_METHOD_SERVICE;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import esminis.server.php.service.Install;
import esminis.server.php.service.PhpServer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.rdrei.android.dirchooser.DirectoryChooserActivity;

public class MainActivity extends Activity {	
	
	static private int REQUEST_DIRECTORY = 1;
	
	private BroadcastReceiver receiver = null;
	
	private SharedPreferences getDefaultSharedPreferences() {
		return PhpServer.getInstance(this).getPreferences();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SharedPreferences preferences = getDefaultSharedPreferences();		
		if (!preferences.contains(PhpServer.PREFERENCES_DOCUMENT_ROOT)) {
			File file = new File(
				Environment.getExternalStorageDirectory().getAbsolutePath() + 
					File.separator + "www"
			);
			if (!file.isDirectory()) {
				file.mkdir();
				if (file.isDirectory()) {
					try {
						new Install().fromAsset(
							new File(file.getAbsolutePath() + File.separator + "index.php"), 
							"www/index.php", this
						);
					} catch (IOException ex) {}
				}
			}
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(
				PhpServer.PREFERENCES_DOCUMENT_ROOT, file.getAbsolutePath()
			);
			editor.commit();
		}
		if (!preferences.contains(PhpServer.PREFERENCES_PORT)) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(PhpServer.PREFERENCES_PORT, "8080");
			editor.commit();
		}				
		
		TextView text = (TextView)findViewById(R.id.server_root);		
		text.setText(
			getDefaultSharedPreferences()
				.getString(PhpServer.PREFERENCES_DOCUMENT_ROOT, "")
		);	
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
					getDefaultSharedPreferences()
						.getString(PhpServer.PREFERENCES_PORT, "")
				);
				try {
					port = Integer.parseInt(text.toString());					
				} catch (NumberFormatException e) {}
				boolean error = true;
				if (port >= 1024 && port <= 65535) {
					SharedPreferences.Editor editor = getDefaultSharedPreferences().edit();
					editor.putString(PhpServer.PREFERENCES_PORT, String.valueOf(port));
					editor.commit();
					error = false;
				}
				((TextView)findViewById(R.id.server_port))
					.setTextColor(error ? Color.RED : Color.BLACK);
			}
		});
		text.setText(
			getDefaultSharedPreferences().getString(PhpServer.PREFERENCES_PORT, "")
		);
		CheckBox checkbox = (CheckBox)findViewById(R.id.server_start_on_boot);
		checkbox.setOnCheckedChangeListener(
			new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(
					CompoundButton checkox, boolean checked
				) {
					SharedPreferences.Editor editor = getDefaultSharedPreferences()
						.edit();
					editor.putBoolean(PhpServer.PREFERENCES_START_ON_BOOT, checked);
					editor.commit();
				}
			}
		);
		checkbox.setChecked(getDefaultSharedPreferences()
			.getBoolean(PhpServer.PREFERENCES_START_ON_BOOT, false));
		
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
		
		PhpServer.getInstance(MainActivity.this);
	}

	private void setLabel(String label) {
		((TextView)findViewById(R.id.label)).setText(label);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(PhpServer.INTENT_ACTION));
		PhpServer.getInstance(MainActivity.this).sendAction("status");
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
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
				SharedPreferences.Editor editor = getDefaultSharedPreferences().edit();
				editor.putString(
					PhpServer.PREFERENCES_DOCUMENT_ROOT, file.getAbsolutePath()
				);
				editor.commit();
				((TextView)findViewById(R.id.server_root)).setText(
					getDefaultSharedPreferences()
						.getString(PhpServer.PREFERENCES_DOCUMENT_ROOT, "")
				);
			}
		}
	}
	
}
