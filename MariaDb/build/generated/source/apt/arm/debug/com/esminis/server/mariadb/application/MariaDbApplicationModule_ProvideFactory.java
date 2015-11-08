package com.esminis.server.mariadb.application;

import com.esminis.server.library.activity.DrawerFragment;
import dagger.internal.Factory;
import javax.annotation.Generated;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class MariaDbApplicationModule_ProvideFactory implements Factory<DrawerFragment> {
  private final MariaDbApplicationModule module;

  public MariaDbApplicationModule_ProvideFactory(MariaDbApplicationModule module) {  
    assert module != null;
    this.module = module;
  }

  @Override
  public DrawerFragment get() {  
    DrawerFragment provided = module.provide();
    if (provided == null) {
      throw new NullPointerException("Cannot return null from a non-@Nullable @Provides method");
    }
    return provided;
  }

  public static Factory<DrawerFragment> create(MariaDbApplicationModule module) {  
    return new MariaDbApplicationModule_ProvideFactory(module);
  }
}

