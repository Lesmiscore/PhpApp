package com.esminis.server.library.activity.external;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.application.LibraryApplication;

public class IntentActivity extends Activity implements IntentView {

	private IntentPresenter presenter = null;
	private TextView viewTitle = null;
	private TextView viewDescription = null;
	private CheckBox viewRemember = null;
	private View viewContainer = null;
	private View viewContainerPreloader = null;
	private TextView viewPreloaderLabel = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.external_intent);
		presenter = ((LibraryApplication)getApplication()).getComponent().getIntentPresenter();
		viewTitle = (TextView)findViewById(R.id.title);
		viewDescription = (TextView)findViewById(R.id.description);
		viewRemember = (CheckBox)findViewById(R.id.checkbox_remember);
		viewContainer = findViewById(R.id.container);
		viewContainerPreloader = findViewById(R.id.preloader_container);
		viewPreloaderLabel = (TextView)findViewById(R.id.preloader_label);
		findViewById(R.id.button_allow).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				presenter.setAllowed(IntentActivity.this, true, viewRemember.isChecked());
			}
		});
		findViewById(R.id.button_deny).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				presenter.setAllowed(IntentActivity.this, false, viewRemember.isChecked());
			}
		});
		presenter.onCreate(this, this, getIntent(), getCallingPackage());
	}

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(0, 0);
	}

	@Override
	public void finish(int result, Bundle data) {
		if (data == null || data.isEmpty()) {
			setResult(result);
		} else {
			final Intent intent = new Intent();
			intent.putExtras(data);
			setResult(result, intent);
		}
		finish();
	}

	@Override
	public void setup(String application, IntentAction action) {
		viewContainer.setVisibility(View.VISIBLE);
		viewContainerPreloader.setVisibility(View.GONE);
		final String serverApplicationTitle = getString(R.string.title);
		viewDescription.setText(
			Html.fromHtml(
				getString(
					R.string.application_requested_server_action, application, serverApplicationTitle,
					action.getTitle(this, false)
				)
			)
		);
		viewTitle.setText(
			getString(R.string.application_requested_server_action_title, serverApplicationTitle)
		);
		viewRemember.setText(
			Html.fromHtml(getString(R.string.remember_choice_for_application, application))
		);
	}

	@Override
	public void showExecutingAction(String application, IntentAction action) {
		viewContainer.setVisibility(View.GONE);
		viewContainerPreloader.setVisibility(View.VISIBLE);
		viewPreloaderLabel.setText(
			Html.fromHtml(
				getString(R.string.application_requested_by, action.getTitle(this, true), application)
			)
		);
	}

}
