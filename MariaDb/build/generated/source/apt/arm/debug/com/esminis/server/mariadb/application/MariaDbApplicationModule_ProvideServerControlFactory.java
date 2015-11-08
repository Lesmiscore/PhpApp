package com.esminis.server.mariadb.application;

import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.model.manager.Process;
import com.esminis.server.library.preferences.Preferences;
import com.esminis.server.library.service.server.ServerControl;
import dagger.internal.Factory;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class MariaDbApplicationModule_ProvideServerControlFactory implements Factory<ServerControl> {
  private final MariaDbApplicationModule module;
  private final Provider<Network> networkProvider;
  private final Provider<Process> processProvider;
  private final Provider<Log> logProvider;
  private final Provider<Preferences> preferencesProvider;

  public MariaDbApplicationModule_ProvideServerControlFactory(MariaDbApplicationModule module, Provider<Network> networkProvider, Provider<Process> processProvider, Provider<Log> logProvider, Provider<Preferences> preferencesProvider) {  
    assert module != null;
    this.module = module;
    assert networkProvider != null;
    this.networkProvider = networkProvider;
    assert processProvider != null;
    this.processProvider = processProvider;
    assert logProvider != null;
    this.logProvider = logProvider;
    assert preferencesProvider != null;
    this.preferencesProvider = preferencesProvider;
  }

  @Override
  public ServerControl get() {  
    ServerControl provided = module.provideServerControl(networkProvider.get(), processProvider.get(), logProvider.get(), preferencesProvider.get());
    if (provided == null) {
      throw new NullPointerException("Cannot return null from a non-@Nullable @Provides method");
    }
    return provided;
  }

  public static Factory<ServerControl> create(MariaDbApplicationModule module, Provider<Network> networkProvider, Provider<Process> processProvider, Provider<Log> logProvider, Provider<Preferences> preferencesProvider) {  
    return new MariaDbApplicationModule_ProvideServerControlFactory(module, networkProvider, processProvider, logProvider, preferencesProvider);
  }
}

