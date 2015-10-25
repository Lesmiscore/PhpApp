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
package com.esminis.popup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.esminis.server.php.R;

public class About extends AlertDialog {

	public About(Context context) {
		super(context);
		setView(createView());
		setButton(
			DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.close), (Message) null
		);
	}

	public View createView() {
		final View view = getLayoutInflater().inflate(R.layout.about, null);
		if (view == null) {
			return null;
		}
		TabHost tabhost = (TabHost)view.findViewById(R.id.tabhost);
		tabhost.setup();
		TabHost.TabSpec tab = tabhost.newTabSpec(getContext().getString(R.string.manual));
		tab.setIndicator(tab.getTag());
		tab.setContent(new TabHost.TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return createText(
					(ViewGroup) view.findViewById(android.R.id.tabcontent), R.string.manual_content
				);
			}
		});
		tabhost.addTab(tab);
		tab = tabhost.newTabSpec(getContext().getString(R.string.about));
		tab.setIndicator(tab.getTag());
		tab.setContent(new TabHost.TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return createText(
					(ViewGroup) view.findViewById(android.R.id.tabcontent), R.string.about_content
				);
			}
		});
		tabhost.addTab(tab);
		tab = tabhost.newTabSpec(getContext().getString(R.string.licenses));
		tab.setIndicator(tab.getTag());
		tab.setContent(new TabHost.TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return new LicensesViewer(getContext());
			}
		});
		tabhost.addTab(tab);
		int tabCount = tabhost.getTabWidget() == null ? 0 : tabhost.getTabWidget().getTabCount();
		for (int i = 0; i < tabCount; i++) {
			View viewTab = tabhost.getTabWidget().getChildTabViewAt(i);
			if (viewTab != null) {
				View textView = viewTab.findViewById(android.R.id.title);
				if (textView != null && textView instanceof TextView) {
					((TextView)textView).setGravity(Gravity.CENTER);
				}
				viewTab.setBackgroundResource(R.drawable.tab_indicator);
			}
		}
		return view;
	}

	private View createText(ViewGroup container, int content) {
		TextView view = (TextView)getLayoutInflater().inflate(R.layout.about_main, container, false);
		if (view != null) {
			view.setText(
				Html.fromHtml(getContext().getString(content, getContext().getString(R.string.php_version)))
			);
			view.setMovementMethod(new ScrollingMovementMethod());
		}
		return view;
	}

	@Override
	public void show() {
		super.show();
		final Button button = getButton(DialogInterface.BUTTON_NEGATIVE);
		final ViewGroup.LayoutParams params = button.getLayoutParams();
		button.setTextColor(Color.BLACK);
		button.setGravity(Gravity.CENTER);
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		button.setLayoutParams(params);
	}
}
