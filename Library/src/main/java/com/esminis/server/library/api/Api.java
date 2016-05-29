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
package com.esminis.server.library.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Singleton;

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
