package com.esminis.server.library.dialog;

import rx.Observable;

public interface DialogPresenter<T> {

	void setView(T view);

	void onCreate();

	Observable<Void> show();

}
