package com.esminis.server.php.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v7.app.NotificationCompat;

import com.esminis.server.php.MainActivity;
import com.esminis.server.php.R;
import com.esminis.server.php.model.manager.Preferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServerNotification {

	static private final int NOTIFICATION_ID = 1;

	private Notification notification = null;

	@Inject
	protected Preferences preferences;

	private NotificationManager getManager(Context context) {
		return (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void hide(Context context) {
		getManager(context).cancel(NOTIFICATION_ID);
		notification = null;
		context.stopService(new Intent(context, ServerNotificationService.class));
	}

	public void show(Context context, CharSequence title, CharSequence titlePublic) {
		if (!preferences.getBoolean(context, Preferences.SHOW_NOTIFICATION_SERVER)) {
			hide(context);
			return;
		}
		context.startService(new Intent(context, ServerNotificationService.class));
		if (notification == null) {
			Builder builder = setupNotificationBuilder(context, title)
				.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
				.setPublicVersion(setupNotificationBuilder(context, titlePublic).build());
			Drawable drawable = context.getResources().getDrawable(R.drawable.ic_notification_large);
			if (drawable != null && drawable instanceof BitmapDrawable) {
				builder.setLargeIcon(((BitmapDrawable) drawable).getBitmap());
			}
			Intent intent = new Intent(context, MainActivity.class);
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			builder.setContentIntent(
				PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
			);
			notification = builder.build();
		}
		getManager(context).notify(NOTIFICATION_ID, notification);
	}

	private Builder setupNotificationBuilder(Context context, CharSequence title) {
		Builder builder = new android.support.v7.app.NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.ic_notification_small)
			.setContentTitle(context.getString(R.string.title)).setContentText(title)
			.setOnlyAlertOnce(true).setOngoing(true).setAutoCancel(false).setShowWhen(false);
		Drawable drawable = context.getResources().getDrawable(R.drawable.ic_notification_large);
		if (drawable != null && drawable instanceof BitmapDrawable) {
			builder.setLargeIcon(((BitmapDrawable) drawable).getBitmap());
		}
		return builder;
	}

}
