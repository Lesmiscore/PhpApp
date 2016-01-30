package com.esminis.server.library.model;

import org.json.JSONException;
import org.json.JSONObject;

public class InstallPackage {

	public final int id;
	public final String version;
	public final int build;
	public final String uri;
	public final String hash;

	public InstallPackage(int id, String version, int build, String uri, String hash) {
		this.id = id;
		this.version = version;
		this.build = build;
		this.uri = uri;
		this.hash = hash;
	}

	public InstallPackage(JSONObject data) throws JSONException {
		this.id = data.getInt("id");
		this.version = data.getString("version");
		this.build = data.getInt("build");
		this.uri = data.getString("uri");
		this.hash = data.getString("hash");
	}

	public JSONObject toJson() throws JSONException {
		final JSONObject data = new JSONObject();
		data.put("id", id);
		data.put("version", version);
		data.put("build", build);
		data.put("uri", uri);
		data.put("hash", hash);
		return data;
	}

}
