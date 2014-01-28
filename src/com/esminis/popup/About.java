package com.esminis.popup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.esminis.server.php.R;

public class About extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		final View view = getLayoutInflater(state).inflate(R.layout.about, null);
		if (view == null) {
			return null;
		}
		setMenuVisibility(true);
		((ViewPager)view.findViewById(R.id.pager)).setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
			@Override
			public int getCount() {
				return 2;
			}

			@Override
			public Fragment getItem(final int i) {
				return i == 0 ? new AboutFragment() : new LicensesFragment();
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return getString(position == 1 ? R.string.licenses : R.string.about);
			}
		});
		return view;
	}


	static public class AboutFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
			TextView view = (TextView)inflater.inflate(R.layout.about_main, container, false);
			if (view != null) {
				view.setText(Html.fromHtml(getString(R.string.about_content)));
				view.setMovementMethod(new ScrollingMovementMethod());
			}
			return view;
		}

	}

	static public class LicensesFragment extends Fragment {

		@SuppressLint("SetJavaScriptEnabled")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
			final View view = inflater.inflate(R.layout.about_licenses, container, false);
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

}
