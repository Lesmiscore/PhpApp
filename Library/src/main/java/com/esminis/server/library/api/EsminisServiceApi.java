package com.esminis.server.library.api;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;

public interface EsminisServiceApi {

	@GET("packages")
	Call<JSONObject> get();

}
