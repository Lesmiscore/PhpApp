package com.esminis.server.mariadb.application;

import com.esminis.server.library.application.LibraryApplication;
import com.esminis.server.library.application.LibraryApplicationModule;

public class MariaDbApplication extends LibraryApplication<MariaDbApplicationComponent> {

	@Override
	protected MariaDbApplicationComponent createComponent() {
		return DaggerMariaDbApplicationComponent.builder()
			.libraryApplicationModule(new LibraryApplicationModule(this))
			.mariaDbApplicationModule(new MariaDbApplicationModule(this))
			.build();
	}

}
