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
package com.esminis.server.library.dialog.dialogpager;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.esminis.server.library.service.Utils;

abstract public class DialogPagerAdapter<State> extends android.support.v4.view.PagerAdapter {

	private final SparseArray<DialogPagerPager> pages = new SparseArray<>();
	private Integer position = null;
	protected final State state;
	protected final DialogPager pager;

	protected DialogPagerAdapter(DialogPager pager, State state) {
		this.pager = pager;
		this.state = state;
	}

	@Override
	abstract public int getCount();

	abstract public DialogPagerPager create(ViewGroup container, int position);

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return object instanceof DialogPagerPager && ((DialogPagerPager)object).getLayout() == view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		final DialogPagerPager page = pages.get(position);
		if (page == object) {
			pages.remove(position);
			container.removeView(page.getLayout());
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		final DialogPagerPager page = create(container, position);
		pages.put(position, page);
		return page;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (
			(this.position == null || this.position != position) && object instanceof DialogPagerPager
		) {
			this.position = position;
			DialogPagerPager page = (DialogPagerPager)object;
			Utils.keyboardHide(pager);
			page.onStateChanged();
			page.onShow();
		}
	}

}
