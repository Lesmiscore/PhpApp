package esminis.server.php;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import esminis.server.php.service.PhpServer;

public class MainActivity extends Activity {

	private BroadcastReceiver receiver = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
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
				sendAction("start");
			}
		});

		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				sendAction("stop");
			}
		});
		
		PhpServer.getInstance(MainActivity.this);
	}
	
	private void sendAction(String action) {
		Handler handler = PhpServer.getInstance(MainActivity.this).getHandler();
		if (handler != null) {
			Bundle bundle = new Bundle();
			bundle.putString("action", action);
			Message message = new Message();
			message.setData(bundle);
			handler.sendMessage(message);
		}
	}

	private void setLabel(String label) {
		((TextView)findViewById(R.id.label)).setText(label);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(PhpServer.INTENT_ACTION));
		sendAction("status");
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
}
