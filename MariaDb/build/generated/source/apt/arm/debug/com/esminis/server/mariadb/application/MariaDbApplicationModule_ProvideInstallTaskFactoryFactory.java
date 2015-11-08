package com.esminis.server.mariadb.application;

import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.install.InstallServer.InstallTaskFactory;
import dagger.internal.Factory;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class MariaDbApplicationModule_ProvideInstallTaskFactoryFactory implements Factory<InstallTaskFactory> {
  private final MariaDbApplicationModule module;
  private final Provider<Network> networkProvider;
  private final Provider<Preferences> preferencesProvider;
  private final Provider<ServerControl> serverControlProvider;

  public MariaDbApplicationModule_ProvideInstallTaskFactoryFactory(MariaDbApplicationModule module, Provider<Network> networkProvider, Provider<Preferences> preferencesProvider, Provider<ServerControl> serverControlProvider) {  
    assert module != null;
    this.module = module;
    assert networkProvider != null;
    this.networkProvider = networkProvider;
    assert preferencesProvider != null;
    this.preferencesProvider = preferencesProvider;
    assert serverControlProvider != null;
    this.serverControlProvider = serverControlProvider;
  }

  @Override
  public InstallTaskFactory get() {  
    InstallTaskFactory provided = module.provideInstallTaskFactory(networkProvider.get(), preferencesProvider.get(), serverControlProvider.get());
    if (provided == null) {
      throw new NullPointerException("Cannot return null from a non-@Nullable @Provides method");
    }
    return provided;
  }

  public static Factory<InstallTaskFactory> create(MariaDbApplicationModule module, Provider<Network> networkProvider, Provider<Preferences> preferencesProvider, Provider<ServerControl> serverControlProvider) {  
    return new MariaDbApplicationModule_ProvideInstallTaskFactoryFactory(module, networkProvider, preferencesProvider, serverControlProvider);
  }
}

