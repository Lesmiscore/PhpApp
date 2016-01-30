package com.esminis.server.library.dialog;

public interface DialogPresenter<T> {

	void setView(T view);

	void onCreate();

	void show();

}
