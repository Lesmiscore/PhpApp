package com.esminis.server.php.application;

import com.esminis.server.library.application.LibraryApplication;

public class PhpApplication extends LibraryApplication<PhpApplicationComponent> {

	@Override
	protected PhpApplicationComponent createComponent() {
		return DaggerPhpApplicationComponent.builder()
			.phpApplicationModule(new PhpApplicationModule(this))
			.build();
	}

}
