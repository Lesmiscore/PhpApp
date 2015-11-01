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
package com.esminis.server.library.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.esminis.server.library.model.ProductLicense;
import com.esminis.server.library.model.manager.ProductLicenseManager;
import com.esminis.server.library.application.Application;
import com.esminis.server.php.R;
import com.esminis.server.library.widget.ProductLicensesViewer;

import javax.inject.Inject;

public class About extends AlertDialog {

	@Inject
	protected ProductLicenseManager productLicenseManager;

	public About(Context context) {
		super(context);
		((Application)context.getApplicationContext()).getObjectGraph().inject(this);
		setView(createView());
		setButton(
			DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.close), (Message) null
		);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
		params.width = getContext().getResources().getDimensionPixelSize(R.dimen.about_dialog_width);
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		getWindow().setAttributes(params);
	}

	public View createView() {
		final View view = getLayoutInflater().inflate(R.layout.dialog_about, null);
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
				return new ProductLicensesViewer(getContext()).setProvider(
					new ProductLicensesViewer.ProductLicenseProvider() {
						@Override
						public ProductLicense[] getList() {
							return productLicenseManager.getLicenses();
						}

						@Override
						public String getContent(ProductLicense model) {
							return productLicenseManager.getProductLicenseContent(model);
						}
					}
				);
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
		ViewGroup view = (ViewGroup)getLayoutInflater().inflate(R.layout.dialog_about_text, container, false);
		if (view != null) {
			((TextView)view.findViewById(R.id.content)).setText(
				Html.fromHtml(getContext().getString(content, getContext().getString(R.string.version)))
			);
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
