package com.esminis.server.library.dialog.dialogpager;

import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.esminis.server.library.service.Utils;
import java.util.ArrayList;
import java.util.List;

public class DialogPager<Data> {

	private final List<DialogPage<Data>> pages = new ArrayList<>();
	private final List<ViewGroup> layouts = new ArrayList<>();
	private final Dialog dialog;
	private final ViewGroup container;
	private int activePage = 0;

	public DialogPager(Dialog dialog, ViewGroup container) {
		this.dialog = dialog;
		this.container = container;
	}

	public <Page extends DialogPage<Data>> Page add(DialogPageFactory<Page> factory) {
		final FrameLayout layout = new FrameLayout(container.getContext());
		layouts.add(layout);
		container.addView(layout);
		layout.setVisibility(View.GONE);
		final Page page = factory.create(layout);
		pages.add(page);
		return page;
	}

	public void show(DialogPage<Data> page, Data data) {
		for (int i = 0; i < pages.size(); i++) {
			if (pages.get(i) == page) {
				Utils.keyboardHide(dialog);
				layouts.get(activePage).setVisibility(View.GONE);
				page.onShow(data);
				layouts.get(activePage = i).setVisibility(View.VISIBLE);
				break;
			}
		}
	}

	public boolean isActive(DialogPage<Data> page) {
		return layouts.get(activePage) == page;
	}

}
