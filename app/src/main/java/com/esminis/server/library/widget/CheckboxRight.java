/**
 * Copyright 2015 Tautvydas Andrikys
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
package com.esminis.server.library.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
