/**
 * Copyright 2016 Tautvydas Andrikys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esminis.server.library.model;

import android.content.Context;

import com.esminis.server.library.R;

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
		this.uri = data.has("uri") ? data.getString("uri") : null;
		this.hash = data.has("hash") ? data.getString("hash") : null;
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

	public String getTitle(Context context) {
		return context.getString(
			R.string.install_package_title, context.getString(R.string.title_server), version, build
		);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof InstallPackage)) {
			return false;
		}
		final InstallPackage model = (InstallPackage)object;
		return id == model.id ||
			(build == model.build && version != null && version.equals(model.version));
	}
}
