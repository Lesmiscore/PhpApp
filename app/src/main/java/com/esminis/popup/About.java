package com.esminis.popup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TabHost;
import android.widget.TextView;

import com.esminis.server.php.R;

public class About extends AlertDialog {

	public About(Context context) {
		super(context);
		setView(createView());
		setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.close), (Message)null);
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
					(ViewGroup)view.findViewById(android.R.id.tabcontent), R.string.manual_content
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
					(ViewGroup)view.findViewById(android.R.id.tabcontent), R.string.about_content
				);
			}
		});
		tabhost.addTab(tab);
		tab = tabhost.newTabSpec(getContext().getString(R.string.licenses));
		tab.setIndicator(tab.getTag());
		tab.setContent(new TabHost.TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return createLicenses((ViewGroup) view.findViewById(android.R.id.tabcontent));
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

	@SuppressLint("SetJavaScriptEnabled")
	private View createLicenses(ViewGroup container) {
		final View view = getLayoutInflater().inflate(R.layout.about_licenses, container, false);
		if (view == null) {
			return null;
		}
		WebView htmlView = (WebView)view.findViewById(R.id.text);
		if (htmlView == null) {
			return null;
		}
		final Handler handler = new Handler();
		htmlView.getSettings().setJavaScriptEnabled(true);
		htmlView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView v, String url) {
				handler.postDelayed(
					new Runnable() {
							public void run() {
							view.findViewById(R.id.text).setVisibility(View.VISIBLE);
							view.findViewById(R.id.preloader).setVisibility(View.GONE);
							view.findViewById(R.id.preloader_container).setVisibility(View.GONE);
						}
						}, 1500
				);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView v, String url) {
				if (v == null || v.getContext() == null) {
					return false;
				}
				v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				return true;
			}
		});
		htmlView.loadUrl("file:///android_asset/Licenses.html");
		return view;
	}

}