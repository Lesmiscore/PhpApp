package com.esminis.server.library.model.manager;

import android.content.Context;
import android.os.Build;
import android.os.Debug;

import com.esminis.server.library.api.Api;
import com.esminis.server.library.model.InstallPackage;
import com.esminis.server.library.preferences.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InstallPackageManager {

	private final Api api;
	private final Preferences manager;
	private final Context context;

	public InstallPackageManager(Api api, Preferences manager, Context context) {
		this.api = api;
		this.manager = manager;
		this.context = context.getApplicationContext();
		migrate();
	}

	private void migrate() {
		if (
			!manager.contains(context, Preferences.BUILD) ||
			manager.getString(context, Preferences.BUILD) == null
		) {
			return;
		}
		final String[] parts = manager.getString(context, Preferences.BUILD).split("_");
		try {
			setInstalled(
				new InstallPackage(
					0, parts[0], parts.length > 1 ? Integer.valueOf(parts[1]) : 0, null, null
				)
			);
			manager.set(context, Preferences.BUILD, null);
		} catch (JSONException ignored) {}
	}

	public void setInstalled(InstallPackage model) throws JSONException {
		setPreference(Preferences.INSTALLED_PACKAGE, model);
	}

	public InstallPackage getInstalled() {
		return getPreference(Preferences.INSTALLED_PACKAGE);
	}

	public InstallPackage getNewest() {
		return getPreference(Preferences.NEWEST_PACKAGE);
	}

	public Observable<InstallPackage[]> get() {
		return Observable.create(
			new Observable.OnSubscribe<InstallPackage[]>() {
				@Override
				public void call(Subscriber<? super InstallPackage[]> subscriber) {
					try {
						final String packageName = context.getPackageName().toLowerCase();
						final int versionCode = context.getPackageManager()
							.getPackageInfo(packageName, 0).versionCode;
						final JSONObject data = api.service().get().execute().body();
						final JSONArray list = data.getJSONArray("files");
						final List<InstallPackage> result = new ArrayList<>();
						final String uriFormat = data.getString("uri");
						final Map<InstallPackage, Integer> order = new HashMap<>();
						final String architecture = getArchitecture();
						for (int i = 0; i < list.length(); i++) {
							final JSONObject item = list.getJSONObject(i);
							if (isUsable(item, packageName, architecture, versionCode)) {
								final InstallPackage model = parse(item, uriFormat);
								result.add(model);
								order.put(model, item.getInt("order"));
							}
						}
						if (result.isEmpty()) {
							subscriber.onError(new Exception("No packages available for your device"));
						} else {
							Collections.sort(result, new Comparator<InstallPackage>() {
								@Override
								public int compare(InstallPackage lhs, InstallPackage rhs) {
									return order.get(lhs) - order.get(rhs);
								}
							});
							setPreference(Preferences.NEWEST_PACKAGE, result.get(result.size() - 1));
							subscriber.onNext(result.toArray(new InstallPackage[result.size()]));
							subscriber.onCompleted();
						}
					} catch (Throwable throwable) {
						subscriber.onError(throwable);
					}
				}
			}
		).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
	}

	private InstallPackage parse(JSONObject data, String uriFormat) throws JSONException {
		return new InstallPackage(
			data.getInt("id"), data.getString("version"), data.getInt("build"),
			uriFormat.replace("{$id}", data.getString("id")), data.getString("hash")
		);
	}

	private boolean isUsable(
		JSONObject data, String packageName, String architecture, int versionCode
	) throws JSONException {
		return packageName.equals(data.getString("package").toLowerCase()) &&
			architecture.equals(data.getString("architecture").toLowerCase()) &&
			(data.isNull("minAppVersion") || data.getInt("minAppVersion") <= versionCode) &&
			(data.isNull("maxAppVersion") || data.getInt("maxAppVersion") >= versionCode);
	}

	private String getArchitecture() throws Exception {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			//noinspection deprecation
			return getArchitecture(Build.CPU_ABI);
		}
		for (String abi : Build.SUPPORTED_ABIS) {
			try {
				return getArchitecture(abi);
			} catch (Exception ignored) {}
		}
		throw new Exception("Architecture not supported");
	}

	private String getArchitecture(String abi) throws Exception {
		abi = abi.toLowerCase();
		if (abi.startsWith("arm")) {
			return "arm";
		}
		if (abi.startsWith("x86")) {
			return "x86";
		}
		throw new Exception("Architecture not supported");
	}

	private InstallPackage getPreference(String name) {
		if (manager.contains(context, name)) {
			try {
				final String content = manager.getString(context, name);
				return content == null ? null : new InstallPackage(new JSONObject(content));
			} catch (JSONException ignored) {}
		}
		return null;
	}

	private void setPreference(String name, InstallPackage model) throws JSONException {
		manager.set(context, name, model == null ? null : model.toJson().toString());
	}

}
