package com.esminis.server.php.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esminis.server.php.R;

public class CheckboxRight extends LinearLayout {

	private CheckBox checkbox = null;

	public CheckboxRight(Context context) {
		super(context);
		initialize(context);
	}

	public CheckboxRight(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public CheckboxRight(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(context);
	}

	private void initialize(Context context) {
		View.inflate(context, R.layout.view_checkbox_right, this);
		checkbox = (CheckBox)findViewById(R.id.checkbox);
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				toggleChecked();
			}
		});
	}

	public CheckboxRight setTitle(int title) {
		((TextView)findViewById(R.id.title)).setText(title);
		return this;
	}

	public CheckboxRight setChecked(boolean checked) {
		((CheckBox)findViewById(R.id.checkbox)).setChecked(checked);
		return this;
	}

	private void toggleChecked() {
		setChecked(!((CheckBox) findViewById(R.id.checkbox)).isChecked());
	}

	public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
		checkbox.setOnCheckedChangeListener(listener);
	}

}
