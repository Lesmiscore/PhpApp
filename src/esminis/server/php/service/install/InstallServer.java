package esminis.server.php.service.install;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import esminis.server.php.service.Network;
import esminis.server.php.service.server.Php;
import esminis.server.php.service.Preferences;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class InstallServer extends AsyncTask<Context, Void, Void> {

	public interface OnInstallListener {
		
		public void OnInstallStart();
		
		public void OnInstallEnd(boolean success);
		
	}
	
	private OnInstallListener listener = null;
	
	public InstallServer(OnInstallListener listener) {
		this.listener = listener;
	}
	
	public void installIfNeeded(Context context) {
		if (listener != null) {
			listener.OnInstallStart();
		}
		if (Php.getInstance(context).getPhp().isFile()) {
			if (listener != null) {
				listener.OnInstallEnd(true);
				listener = null;
			}
		} else {
			execute(context);
		}
	}
	
	@Override
	protected Void doInBackground(Context... arguments) {
		Context context = arguments[0];		
		Preferences preferences = new Preferences(context);
		if (!preferences.contains(Preferences.DOCUMENT_ROOT)) {
			File file = new File(
				Environment.getExternalStorageDirectory().getAbsolutePath() + 
					File.separator + "www"
			);
			if (!file.isDirectory()) {
				file.mkdir();
				if (file.isDirectory()) {
					try {
						Install install = new Install();
						install.fromAssetDirectory(file, "www", context);						
						HashMap<String, String> variables = new HashMap<String, String>();
						File tempDirectory = new File(
							context.getExternalFilesDir(null).getAbsolutePath() + 
								File.separator + "tmp"
						);
						if (!tempDirectory.isDirectory()) {
							tempDirectory.mkdir();
						}
						variables.put("tempDirectory", tempDirectory.getAbsolutePath());
						install.preprocessFile(
							new File(file + File.separator + "php.ini"), variables
						);
					} catch (IOException ex) {}
				}
			}
			preferences.set(Preferences.DOCUMENT_ROOT, file.getAbsolutePath());
		}
		if (!preferences.contains(Preferences.PORT)) {
			preferences.set(Preferences.PORT, "8080");
		}
		if (!preferences.contains(Preferences.ADDRESS)) {			
			preferences.set(Preferences.ADDRESS, new Network().getNames().get(0));
		}
		File php = Php.getInstance(context).getPhp();
		try {
			new Install().fromAssetFile(php, "php", context);
			php.setExecutable(true);
		} catch (IOException ex) {}
		return null;
	}

	@Override
	protected void onCancelled() {
		if (listener != null) {
			listener.OnInstallEnd(false);
			listener = null;
		}		
	}

	@Override
	protected void onPostExecute(Void result) {
		if (listener != null) {
			listener.OnInstallEnd(true);
			listener = null;
		}
	}
	
}
