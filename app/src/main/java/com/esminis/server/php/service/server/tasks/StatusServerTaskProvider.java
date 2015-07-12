package com.esminis.server.php.service.server.tasks;

import com.esminis.server.php.service.server.Php;

import javax.inject.Inject;

public class StatusServerTaskProvider extends ServerTaskProvider {

	@Inject
	protected Php php;

	@Override
	protected void execute() {
		php.requestStatus();
	}

}
