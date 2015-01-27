package com.esminis.server.php;

import android.app.Dialog;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActivityHelper {

	@Inject
	public ActivityHelper() {}

	public Toolbar createToolbar(@NonNull ActionBarActivity activity) {
		Toolbar toolbar = createToolbar((Toolbar)activity.findViewById(R.id.toolbar));
		if (toolbar != null) {
			toolbar.inflateMenu(R.menu.main);
			activity.setSupportActionBar(toolbar);
		}
		return toolbar;
	}

	public Toolbar createToolbar(@NonNull final Dialog dialog) {
		Toolbar toolbar = createToolbar((Toolbar)dialog.findViewById(R.id.toolbar));
		if (toolbar != null) {
			TypedArray attribute = dialog.getContext().obtainStyledAttributes(
				new int[] {android.support.v7.appcompat.R.attr.homeAsUpIndicator}
			);
			if (attribute != null) {
				toolbar.setNavigationIcon(attribute.getDrawable(0));
				attribute.recycle();
				toolbar.setNavigationOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		}
		return toolbar;
	}

	public Toolbar createToolbar(@NonNull Toolbar toolbar) {
		toolbar.setLogo(R.drawable.ic_logo);
		return toolbar;
	}

}
