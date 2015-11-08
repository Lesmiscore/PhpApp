package com.esminis.server.mariadb.server;

import com.esminis.server.library.service.server.ServerControl;
import dagger.MembersInjector;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class InstallServerMariaDbTaskProvider_MembersInjector implements MembersInjector<InstallServerMariaDbTaskProvider> {
  private final Provider<ServerControl> serverControlProvider;

  public InstallServerMariaDbTaskProvider_MembersInjector(Provider<ServerControl> serverControlProvider) {  
    assert serverControlProvider != null;
    this.serverControlProvider = serverControlProvider;
  }

  @Override
  public void injectMembers(InstallServerMariaDbTaskProvider instance) {  
    if (instance == null) {
      throw new NullPointerException("Cannot inject members into a null reference");
    }
    instance.serverControl = serverControlProvider.get();
  }

  public static MembersInjector<InstallServerMariaDbTaskProvider> create(Provider<ServerControl> serverControlProvider) {  
      return new InstallServerMariaDbTaskProvider_MembersInjector(serverControlProvider);
  }
}

