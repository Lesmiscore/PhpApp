package com.esminis.model.manager;

import android.content.Context;

import com.esminis.model.ProductLicense;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProductLicenseManager {

	private final Context context;
	private ProductLicense[] licenses = null;

	public ProductLicenseManager(Context context) {
		this.context = context;
	}

	public Observable<ProductLicense[]> getLicenses() {
		return Observable.create(new Observable.OnSubscribe<ProductLicense[]>() {
			@Override
			public void call(Subscriber<? super ProductLicense[]> subscriber) {
				if (licenses == null) {
					final List<ProductLicense> licenses = new ArrayList<>();
					try {
						JSONObject json = new JSONObject(
							IOUtils.toString(context.getAssets().open("licenses/licenses.json"))
						);
						Iterator<String> keys = json.keys();
						while (keys.hasNext()) {
							final String file = keys.next();
							final JSONObject licenseData = json.getJSONObject(file);
							final JSONArray items = licenseData.getJSONArray("items");
							for (int i = 0; i < items.length(); i++) {
								licenses.add(
									new ProductLicense(items.getString(i), licenseData.getString("title"), file)
								);
							}
						}
					} catch (IOException | JSONException ignored) {
					}
					ProductLicenseManager.this.licenses =
						licenses.toArray(new ProductLicense[licenses.size()]);
				}
				subscriber.onNext(licenses);
				subscriber.onCompleted();
			}
		}).delay(1, TimeUnit.SECONDS)
			.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
	}

	public Observable<String> getProductLicenseContent(final ProductLicense model) {
		return Observable.create(new Observable.OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				try {
					subscriber.onNext(
						IOUtils.toString(context.getAssets().open("licenses/" + model.name + ".txt"))
					);
				} catch (IOException ignored) {
					subscriber.onNext("");
				}
				subscriber.onCompleted();
			}
		}).delay(1, TimeUnit.SECONDS).subscribeOn(Schedulers.computation())
			.observeOn(AndroidSchedulers.mainThread());
	}

}
