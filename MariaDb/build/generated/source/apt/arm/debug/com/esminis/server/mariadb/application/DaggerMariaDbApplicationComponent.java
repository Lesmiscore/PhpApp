package com.esminis.server.mariadb.application;

import com.esminis.server.library.activity.DrawerFragment;
import com.esminis.server.library.activity.DrawerFragmentHelper;
import com.esminis.server.library.activity.DrawerFragmentHelper_Factory;
import com.esminis.server.library.activity.DrawerFragmentHelper_MembersInjector;
import com.esminis.server.library.activity.MainActivity;
import com.esminis.server.library.activity.MainActivityHelper;
import com.esminis.server.library.activity.MainActivityHelper_Factory;
import com.esminis.server.library.activity.MainActivityHelper_MembersInjector;
import com.esminis.server.library.activity.MainActivity_MembersInjector;
import com.esminis.server.library.application.LibraryApplicationModule;
import com.esminis.server.library.application.LibraryApplicationModule_ProvideBusFactory;
import com.esminis.server.library.application.LibraryApplicationModule_ProvideInstallServerFactory;
import com.esminis.server.library.application.LibraryApplicationModule_ProvideProductLicenseManagerFactory;
import com.esminis.server.library.dialog.About;
import com.esminis.server.library.dialog.About_MembersInjector;
import com.esminis.server.library.model.manager.Log;
import com.esminis.server.library.model.manager.Log_Factory;
import com.esminis.server.library.model.manager.Log_MembersInjector;
import com.esminis.server.library.model.manager.Network;
import com.esminis.server.library.model.manager.Network_Factory;
import com.esminis.server.library.model.manager.Process;
import com.esminis.server.library.model.manager.Process_Factory;
import com.esminis.server.library.model.manager.ProductLicenseManager;
import com.esminis.server.library.permission.PermissionActivityHelper;
import com.esminis.server.library.permission.PermissionActivityHelper_Factory;
import com.esminis.server.library.permission.PermissionActivityHelper_MembersInjector;
import com.esminis.server.library.permission.PermissionRequester_Factory;
import com.esminis.server.library.preferences.Preferences_Factory;
import com.esminis.server.library.service.AutoStart;
import com.esminis.server.library.service.AutoStart_MembersInjector;
import com.esminis.server.library.service.server.ServerControl;
import com.esminis.server.library.service.server.ServerNotification;
import com.esminis.server.library.service.server.ServerNotificationService;
import com.esminis.server.library.service.server.ServerNotificationService_MembersInjector;
import com.esminis.server.library.service.server.ServerNotification_Factory;
import com.esminis.server.library.service.server.ServerNotification_MembersInjector;
import com.esminis.server.library.service.server.install.InstallServer;
import com.esminis.server.library.service.server.install.InstallServer.InstallTaskFactory;
import com.esminis.server.library.service.server.tasks.ServerTaskProvider;
import com.esminis.server.library.service.server.tasks.ServerTaskProvider_MembersInjector;
import com.esminis.server.mariadb.server.InstallServerMariaDbTaskProvider;
import com.esminis.server.mariadb.server.InstallServerMariaDbTaskProvider_MembersInjector;
import com.squareup.otto.Bus;
import dagger.MembersInjector;
import dagger.internal.MembersInjectors;
import dagger.internal.ScopedProvider;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class DaggerMariaDbApplicationComponent implements MariaDbApplicationComponent {
  private Provider<Network> networkProvider;
  private Provider<Process> processProvider;
  private MembersInjector<Log> logMembersInjector;
  private Provider<Log> logProvider;
  private Provider<ServerControl> provideServerControlProvider;
  private MembersInjector<AutoStart> autoStartMembersInjector;
  private Provider<Bus> provideBusProvider;
  private MembersInjector<MainActivityHelper> mainActivityHelperMembersInjector;
  private Provider<MainActivityHelper> mainActivityHelperProvider;
  private MembersInjector<ServerNotification> serverNotificationMembersInjector;
  private Provider<ServerNotification> serverNotificationProvider;
  private MembersInjector<PermissionActivityHelper> permissionActivityHelperMembersInjector;
  private Provider<PermissionActivityHelper> permissionActivityHelperProvider;
  private Provider<InstallTaskFactory> provideInstallTaskFactoryProvider;
  private Provider<InstallServer> provideInstallServerProvider;
  private MembersInjector<MainActivity> mainActivityMembersInjector;
  private Provider<ProductLicenseManager> provideProductLicenseManagerProvider;
  private MembersInjector<About> aboutMembersInjector;
  private MembersInjector<ServerNotificationService> serverNotificationServiceMembersInjector;
  private MembersInjector<ServerTaskProvider> serverTaskProviderMembersInjector;
  private MembersInjector<DrawerFragmentHelper> drawerFragmentHelperMembersInjector;
  private Provider<DrawerFragmentHelper> drawerFragmentHelperProvider;
  private Provider<DrawerFragment> provideProvider;
  private MembersInjector<InstallServerMariaDbTaskProvider> installServerMariaDbTaskProviderMembersInjector;

  private DaggerMariaDbApplicationComponent(Builder builder) {  
    assert builder != null;
    initialize(builder);
  }

  public static Builder builder() {  
    return new Builder();
  }

  private void initialize(final Builder builder) {  
    this.networkProvider = ScopedProvider.create(Network_Factory.create());
    this.processProvider = ScopedProvider.create(Process_Factory.create());
    this.logMembersInjector = Log_MembersInjector.create(Preferences_Factory.create());
    this.logProvider = ScopedProvider.create(Log_Factory.create(logMembersInjector));
    this.provideServerControlProvider = ScopedProvider.create(MariaDbApplicationModule_ProvideServerControlFactory.create(builder.mariaDbApplicationModule, networkProvider, processProvider, logProvider, Preferences_Factory.create()));
    this.autoStartMembersInjector = AutoStart_MembersInjector.create((MembersInjector) MembersInjectors.noOp(), provideServerControlProvider, Preferences_Factory.create());
    this.provideBusProvider = ScopedProvider.create(LibraryApplicationModule_ProvideBusFactory.create(builder.libraryApplicationModule));
    this.mainActivityHelperMembersInjector = MainActivityHelper_MembersInjector.create(provideBusProvider, Preferences_Factory.create());
    this.mainActivityHelperProvider = ScopedProvider.create(MainActivityHelper_Factory.create(mainActivityHelperMembersInjector));
    this.serverNotificationMembersInjector = ServerNotification_MembersInjector.create(Preferences_Factory.create());
    this.serverNotificationProvider = ScopedProvider.create(ServerNotification_Factory.create(serverNotificationMembersInjector));
    this.permissionActivityHelperMembersInjector = PermissionActivityHelper_MembersInjector.create(PermissionRequester_Factory.create());
    this.permissionActivityHelperProvider = PermissionActivityHelper_Factory.create(permissionActivityHelperMembersInjector);
    this.provideInstallTaskFactoryProvider = ScopedProvider.create(MariaDbApplicationModule_ProvideInstallTaskFactoryFactory.create(builder.mariaDbApplicationModule, networkProvider, Preferences_Factory.create(), provideServerControlProvider));
    this.provideInstallServerProvider = ScopedProvider.create(LibraryApplicationModule_ProvideInstallServerFactory.create(builder.libraryApplicationModule, Preferences_Factory.create(), provideServerControlProvider, provideInstallTaskFactoryProvider));
    this.mainActivityMembersInjector = MainActivity_MembersInjector.create((MembersInjector) MembersInjectors.noOp(), networkProvider, logProvider, mainActivityHelperProvider, serverNotificationProvider, permissionActivityHelperProvider, provideInstallServerProvider);
    this.provideProductLicenseManagerProvider = ScopedProvider.create(LibraryApplicationModule_ProvideProductLicenseManagerFactory.create(builder.libraryApplicationModule));
    this.aboutMembersInjector = About_MembersInjector.create((MembersInjector) MembersInjectors.noOp(), provideProductLicenseManagerProvider);
    this.serverNotificationServiceMembersInjector = ServerNotificationService_MembersInjector.create((MembersInjector) MembersInjectors.noOp(), serverNotificationProvider, provideServerControlProvider);
    this.serverTaskProviderMembersInjector = ServerTaskProvider_MembersInjector.create(provideServerControlProvider);
    this.drawerFragmentHelperMembersInjector = DrawerFragmentHelper_MembersInjector.create(Preferences_Factory.create(), provideServerControlProvider, mainActivityHelperProvider, provideBusProvider);
    this.drawerFragmentHelperProvider = DrawerFragmentHelper_Factory.create(drawerFragmentHelperMembersInjector);
    this.provideProvider = MariaDbApplicationModule_ProvideFactory.create(builder.mariaDbApplicationModule);
    this.installServerMariaDbTaskProviderMembersInjector = InstallServerMariaDbTaskProvider_MembersInjector.create(provideServerControlProvider);
  }

  @Override
  public void inject(AutoStart arg0) {  
    autoStartMembersInjector.injectMembers(arg0);
  }

  @Override
  public void inject(MainActivity arg0) {  
    mainActivityMembersInjector.injectMembers(arg0);
  }

  @Override
  public void inject(About arg0) {  
    aboutMembersInjector.injectMembers(arg0);
  }

  @Override
  public void inject(DrawerFragment arg0) {  
    MembersInjectors.noOp().injectMembers(arg0);
  }

  @Override
  public void inject(ServerNotificationService arg0) {  
    serverNotificationServiceMembersInjector.injectMembers(arg0);
  }

  @Override
  public void inject(ServerTaskProvider arg0) {  
    serverTaskProviderMembersInjector.injectMembers(arg0);
  }

  @Override
  public DrawerFragmentHelper getDrawerFragmentHelper() {  
    return drawerFragmentHelperProvider.get();
  }

  @Override
  public ServerControl getServerControl() {  
    return provideServerControlProvider.get();
  }

  @Override
  public DrawerFragment getDrawerFragment() {  
    return provideProvider.get();
  }

  @Override
  public void inject(InstallServerMariaDbTaskProvider taskProvider) {  
    installServerMariaDbTaskProviderMembersInjector.injectMembers(taskProvider);
  }

  public static final class Builder {
    private MariaDbApplicationModule mariaDbApplicationModule;
    private LibraryApplicationModule libraryApplicationModule;
  
    private Builder() {  
    }
  
    public MariaDbApplicationComponent build() {  
      if (mariaDbApplicationModule == null) {
        throw new IllegalStateException("mariaDbApplicationModule must be set");
      }
      if (libraryApplicationModule == null) {
        throw new IllegalStateException("libraryApplicationModule must be set");
      }
      return new DaggerMariaDbApplicationComponent(this);
    }
  
    public Builder mariaDbApplicationModule(MariaDbApplicationModule mariaDbApplicationModule) {  
      if (mariaDbApplicationModule == null) {
        throw new NullPointerException("mariaDbApplicationModule");
      }
      this.mariaDbApplicationModule = mariaDbApplicationModule;
      return this;
    }
  
    public Builder libraryApplicationModule(LibraryApplicationModule libraryApplicationModule) {  
      if (libraryApplicationModule == null) {
        throw new NullPointerException("libraryApplicationModule");
      }
      this.libraryApplicationModule = libraryApplicationModule;
      return this;
    }
  }
}

