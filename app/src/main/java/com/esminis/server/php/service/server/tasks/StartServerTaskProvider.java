package com.esminis.server.php.service.server.tasks;

import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.service.server.PhpHandler;

import javax.inject.Inject;

public class StartServerTaskProvider extends ServerTaskProvider {

	@Inject
	protected Php php;

	@Override
	protected void execute() {
		php.requestStart();
	}

}
