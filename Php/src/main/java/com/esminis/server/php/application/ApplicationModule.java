package com.esminis.server.php.application;

import com.esminis.server.php.activity.DrawerPhpFragment;
import com.esminis.server.php.server.install.InstallTaskProvider;

import dagger.Module;

@Module(injects = {DrawerPhpFragment.class, InstallTaskProvider.class}, includes = {com.esminis.server.library.application.ApplicationModule.class})
public class ApplicationModule {
}
