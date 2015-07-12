package com.esminis.server.php.service.background;

import android.content.Context;

import rx.Observable;

public interface BackgroundServiceTaskProvider {

	Observable<Void> createTask(Context context);

}
