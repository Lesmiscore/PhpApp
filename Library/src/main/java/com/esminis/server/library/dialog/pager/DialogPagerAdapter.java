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

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.esminis.server.library.service.Utils;

abstract public class DialogPagerAdapter extends android.support.v4.view.PagerAdapter {

	private final SparseArray<DialogPagerPage> pages = new SparseArray<>();
	private Integer position = null;
	protected final DialogPager pager;

	protected DialogPagerAdapter(DialogPager pager) {
		this.pager = pager;
	}

	@Override
	abstract public int getCount();

	abstract public DialogPagerPage create(ViewGroup container, int position);

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return object instanceof DialogPagerPage && ((DialogPagerPage)object).getLayout() == view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		final DialogPagerPage page = pages.get(position);
		if (page == object) {
			pages.remove(position);
			container.removeView(page.getLayout());
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		final DialogPagerPage page = create(container, position);
		pages.put(position, page);
		return page;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (
			(this.position == null || this.position != position) && object instanceof DialogPagerPage
		) {
			this.position = position;
			DialogPagerPage page = (DialogPagerPage)object;
			Utils.keyboardHide(pager);
			page.onStateChanged();
			page.onShow();
		}
	}

}
