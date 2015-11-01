package com.esminis.server.library.service.server;

import android.os.Message;

import java.io.File;

public interface ServerControl {

	void requestStatus();

	void requestRestart();

	void requestStop();

	void requestStart();

	void requestRestartIfRunning();

	void onHandlerReady();

	void onHandlerMessage(Message message);

	File getBinary();

}
