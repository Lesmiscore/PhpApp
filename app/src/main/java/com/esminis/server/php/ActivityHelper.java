package com.esminis.server.php;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;

import java.lang.reflect.Constructor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActivityHelper {

	@Inject
	public ActivityHelper() {}

	public Toolbar createToolbar(@NonNull AppCompatActivity activity) {
		Toolbar toolbar = createToolbar((Toolbar)activity.findViewById(R.id.toolbar));
		if (toolbar != null) {
			toolbar.inflateMenu(R.menu.main);
			TypedValue attribute = new TypedValue();
			activity.getTheme().resolveAttribute(android.R.attr.textColorPrimary, attribute, true);
			if (attribute.resourceId > 0) {
				MenuItem item = toolbar.getMenu().findItem(R.id.menu_about);
				Drawable icon = DrawableCompat.wrap(item.getIcon());
				DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN);
				DrawableCompat.setTint(icon, activity.getResources().getColor(attribute.resourceId));
				item.setIcon(icon);
			}
			activity.setSupportActionBar(toolbar);
		}
		return toolbar;
	}

	public Toolbar createToolbar(@NonNull final Dialog dialog, Activity activity) {
		Toolbar toolbar = createToolbar((Toolbar)dialog.findViewById(R.id.toolbar));
		if (toolbar != null) {
			toolbar.setNavigationIcon(createNavigationIcon(activity));
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
		return toolbar;
	}

	public Toolbar createToolbar(@NonNull Toolbar toolbar) {
		toolbar.setLogo(R.drawable.ic_toolbar);
		return toolbar;
	}

	private Drawable createNavigationIcon(Activity activity) {
		try {
			// @todo when possible remove this dirty hack
			Class classArrow = Class.forName(
				ActionBarDrawerToggle.class.getName() + "$DrawerArrowDrawableToggle"
			);
			Constructor constructor = classArrow.getDeclaredConstructor(Activity.class, Context.class);
			constructor.setAccessible(true);
			Drawable drawable = (Drawable)constructor.newInstance(
				activity, new ContextThemeWrapper(activity, R.style.Toolbar)
			);
			classArrow.getDeclaredMethod("setPosition", float.class).invoke(drawable, 1);
			return drawable;
		} catch (Exception e) {
			TypedArray attribute = activity.obtainStyledAttributes(
				new int[] {android.support.v7.appcompat.R.attr.homeAsUpIndicator}
			);
			if (attribute == null) {
				return null;
			}
			final Drawable drawable = attribute.getDrawable(0);
			attribute.recycle();
			return drawable;
		}
	}

}
