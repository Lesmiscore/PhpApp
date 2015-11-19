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
package com.esminis.server.library.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.esminis.server.library.EventMessage;
import com.esminis.server.library.R;
import com.esminis.server.library.preferences.Preferences;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainActivityHelper {

	private final Bus bus;
	private final Preferences preferences;
	private WeakReference<Activity> activity = new WeakReference<>(null);

	@Inject
	public MainActivityHelper(Preferences preferences, Bus bus) {
		this.bus = bus;
		this.preferences = preferences;
	}

	public String getPort(Context context) {
		return preferences.getString(context, Preferences.PORT);
	}

	public void setPort(Context context, String port) {
		preferences.set(context, Preferences.PORT, port);
	}

	public String getAddress(Context context) {
		return preferences.getString(context, Preferences.ADDRESS);
	}

	public void setAddress(Context context, String address) {
		preferences.set(context, Preferences.ADDRESS, address);
	}

	public String getRootDirectory(Context context) {
		return preferences.getString(context, Preferences.DOCUMENT_ROOT);
	}

	public void setRootDirectory(Context context, String root) {
		preferences.set(context, Preferences.DOCUMENT_ROOT, root);
	}

	public String getMessageNewVersion(Context context) {
		return context.getString(
			R.string.server_install_new_version_question, preferences.getBuild(context)
		);
	}

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
				DrawableCompat.setTint(icon, ContextCompat.getColor(activity, attribute.resourceId));
				item.setIcon(icon);
			}
			activity.setSupportActionBar(toolbar);
		}
		return toolbar;
	}

	public Toolbar createToolbar(@NonNull final Dialog dialog, Activity activity) {
		Toolbar toolbar = createToolbar((Toolbar)dialog.findViewById(R.id.toolbar));
		if (toolbar != null) {
			final DrawerArrowDrawable drawable = new DrawerArrowDrawable(activity);
			drawable.setProgress(1);
			drawable.setColor(Color.BLACK);
			toolbar.setNavigationIcon(drawable);
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

	public void onResume(Activity activity) {
		final Activity activityOld = getActivity();
		if (activityOld == null || activityOld != activity) {
			this.activity = new WeakReference<>(activity);
			bus.register(this);
		}
	}

	public void onPause() {
		activity = new WeakReference<>(null);
		bus.unregister(this);
	}

	@Subscribe
	public void onEventMessage(EventMessage event) {
		final Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		View view = activity.findViewById(R.id.container);
		if (view == null) {
			return;
		}
		final Snackbar snackbar = Snackbar.make(view, event.message, Snackbar.LENGTH_LONG);
		snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				snackbar.dismiss();
			}
		});
		snackbar.setActionTextColor(
			ContextCompat.getColor(view.getContext(), event.error ? R.color.error : R.color.main)
		);
		snackbar.show();
	}

	public void contentMessage(
		boolean containerVisible, boolean preloader, boolean button, String message
	) {
		final Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.findViewById(R.id.preloader_container)
			.setVisibility(containerVisible ? View.VISIBLE : View.GONE);
		if (containerVisible) {
			activity.findViewById(R.id.preloader).setVisibility(preloader ? View.VISIBLE : View.GONE);
			activity.findViewById(R.id.preloader_button_ok)
				.setVisibility(button ? View.VISIBLE : View.GONE);
			((TextView)activity.findViewById(R.id.preloader_label)).setText(message);
		}
	}

	public CharSequence getServerRunningLabel(String address) {
		final Activity activity = getActivity();
		if (activity == null) {
			return null;
		}
		return Html.fromHtml(
			String.format(
				activity.getString(R.string.server_running),
				"<a href=\"http://" + address + "\">" + address + "</a>"
			)
		);
	}

	protected Activity getActivity() {
		return activity.get();
	}

}
