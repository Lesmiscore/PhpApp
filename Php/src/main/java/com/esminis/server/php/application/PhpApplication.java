package com.esminis.server.php.application;

import com.esminis.server.library.application.LibraryApplicationModule;
import com.esminis.server.library.application.LibraryApplication;

public class PhpApplication extends LibraryApplication<PhpApplicationComponent> {

	@Override
	protected PhpApplicationComponent createComponent() {
		return DaggerPhpApplicationComponent.builder()
			.libraryApplicationModule(new LibraryApplicationModule(this))
			.phpApplicationModule(new PhpApplicationModule(this))
			.build();
	}

}
