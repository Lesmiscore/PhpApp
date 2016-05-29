/**
 * Copyright 2016 Tautvydas Andrikys
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
package com.esminis.server.library.dialog.pager;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.esminis.server.library.R;
import com.esminis.server.library.dialog.DialogImpl;
import com.esminis.server.library.dialog.DialogPresenter;

abstract public class DialogPager
	<View, Presenter extends DialogPresenter<View>> extends DialogImpl<Presenter>
{

	private OnShowListener listener = null;

	private final ViewPager pager;

	protected DialogPager(Context context, Presenter presenter) {
		super(context, presenter);
		final FrameLayout layout = new FrameLayout(context);
		layout.addView(
			pager = (ViewPager)LayoutInflater.from(context).inflate(R.layout.dialog_pager, layout, false)
		);
		setContentView(layout);

		super.setOnShowListener(new OnShowListener() {
			public void onShow(DialogInterface dialog) {
				if (listener != null) {
					listener.onShow(dialog);
				}
			}
		});
	}

	protected void setAdapter(DialogPagerAdapter adapter) {
		pager.setAdapter(adapter);
	}

	@Override
	public void setOnShowListener(OnShowListener listener) {
		this.listener = listener;
	}

	protected int getCurrentItem() {
		return pager.getCurrentItem();
	}

	protected void setCurrentItem(int position) {
		pager.setCurrentItem(position);
	}

}
