package com.esminis.server.php;

import com.esminis.model.manager.Network;
import com.esminis.server.php.model.manager.Log;
import com.esminis.server.php.model.manager.Preferences;
import com.esminis.server.php.service.server.Php;
import com.esminis.server.php.service.server.PhpStartup;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects = {
	Application.class, MainActivity.class, Php.class, Preferences.class, DrawerFragment.class
})
public class ApplicationModule {

	private Application application;

	public ApplicationModule(Application application) {
		this.application = application;
	}

	@Provides
	@Singleton
	public Php providePhp(
		Network network, com.esminis.model.manager.Process process, PhpStartup startup, Log log,
		Preferences preferences
	) {
		return new Php(network, process, startup, preferences, log, application);
	}

	@Provides
	@Singleton
	public Preferences providePreferences() {
		return new com.esminis.server.php.model.manager.Preferences();
	}

}
