package com.esminis.server.library.dialog.about;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Message;
import android.support.annotation.StringRes;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.esminis.server.library.R;
import com.esminis.server.library.dialog.Dialog;
import com.esminis.server.library.widget.ProductLicensesViewer;

public class AboutViewImpl extends Dialog<AboutPresenter> implements AboutView {

	private final View viewTextAbout;
	private final View viewTextManual;
	private final ProductLicensesViewer viewLicenses;

	public AboutViewImpl(Context context, AboutPresenter presenter) {
		super(context, presenter);
		final LayoutInflater inflater = LayoutInflater.from(context);
		final ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.dialog_about, null);
		final TabHost tabhost = (TabHost)layout.findViewById(R.id.tabhost);
		tabhost.setup();
		addTab(tabhost, context, R.string.manual, viewTextManual = createText(inflater, layout));
		addTab(tabhost, context, R.string.about, viewTextAbout = createText(inflater, layout));
		addTab(tabhost, context, R.string.licenses, viewLicenses = new ProductLicensesViewer(context));
		setupTabTitles(tabhost);
		setView(layout);
		setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.close), (Message)null);
	}

	@Override
	public void setContentAbout(Context context, int content) {
		setContentText(viewTextAbout, context, content);
	}

	@Override
	public void setContentManual(Context context, int content) {
		setContentText(viewTextManual, context, content);
	}

	@Override
	public void setLicensesProvider(ProductLicensesViewer.ProductLicenseProvider provider) {
		viewLicenses.setProvider(provider);
	}

	@Override
	public void setupOnCreate() {
		final Window window = getWindow();
		final WindowManager.LayoutParams params = window.getAttributes();
		params.width = getContext().getResources().getDimensionPixelSize(R.dimen.about_dialog_width);
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		window.setAttributes(params);
	}

	@Override
	public void setupOnShow() {
		final Button button = getButton(DialogInterface.BUTTON_NEGATIVE);
		final ViewGroup.LayoutParams params = button.getLayoutParams();
		button.setTextColor(Color.BLACK);
		button.setGravity(Gravity.CENTER);
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		button.setLayoutParams(params);
	}

	private void addTab(TabHost tabhost, Context context, @StringRes int title, final View view) {
		TabHost.TabSpec tab = tabhost.newTabSpec(context.getString(title));
		tab.setIndicator(tab.getTag());
		tab.setContent(new TabHost.TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return view;
			}
		});
		tabhost.addTab(tab);
	}

	private View createText(LayoutInflater inflater, ViewGroup viewLayout) {
		return inflater.inflate(
			R.layout.dialog_about_text, (ViewGroup) viewLayout.findViewById(android.R.id.tabcontent),
			false
		);
	}

	private void setupTabTitles(TabHost tabhost) {
		final int tabCount = tabhost.getTabWidget() == null ? 0 : tabhost.getTabWidget().getTabCount();
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
	}

	private void setContentText(View view, Context context, int content) {
		((TextView)view.findViewById(R.id.content)).setText(Html.fromHtml(context.getString(content)));
	}

}
