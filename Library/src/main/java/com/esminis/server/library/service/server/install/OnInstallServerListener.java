package com.esminis.server.library.service.server.install;

public interface OnInstallServerListener {

	void OnInstallNewVersionRequest(InstallServer installer);

	void OnInstallEnd(Throwable error);

}
