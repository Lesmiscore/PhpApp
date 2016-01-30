package com.esminis.server.library.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

@Singleton
public class Api {

	private final EsminisServiceApi api;

	@Inject
	public Api() {
		api = new Retrofit.Builder()
			.baseUrl("https://esminiscdn.com")
			.addConverterFactory(new Converter.Factory() {

				@Override
				public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
					return new Converter<ResponseBody, JSONObject>() {

						@Override
						public JSONObject convert(ResponseBody value) throws IOException {
							try {
								return new JSONObject(value.string());
							} catch(JSONException e) {
								throw new IOException(e);
							}
						}

					};
				}
			})
			.build().create(EsminisServiceApi.class);
	}

	public EsminisServiceApi service() {
		return api;
	}

}
