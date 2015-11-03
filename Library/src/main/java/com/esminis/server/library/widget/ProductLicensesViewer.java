package com.esminis.server.library.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.esminis.server.library.model.ProductLicense;
import com.esminis.server.library.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ProductLicensesViewer extends FrameLayout {

	private final ListView viewList;
	private final TextView viewContent;
	private final View viewContentContainer;
	private final Button buttonBack;
	private final View viewPreloader;
	private ProductLicenseProvider provider = null;
	private ProductLicense[] list = new ProductLicense[0];

	public interface ProductLicenseProvider {

		ProductLicense[] getList();

		String getContent(ProductLicense model);

	}

	public ProductLicensesViewer(Context context) {
		super(context);
		View.inflate(context, R.layout.view_product_licenses_viewer, this);
		viewList = (ListView)findViewById(R.id.list_licenses);
		viewContent = (TextView)findViewById(R.id.content_license);
		viewContentContainer = findViewById(R.id.content_license_container);
		buttonBack = (Button)findViewById(R.id.button_back);
		viewPreloader = findViewById(R.id.preloader_container);
		DrawerArrowDrawable drawable = new DrawerArrowDrawable(getContext());
		drawable.setProgress(1);
		drawable.setColor(Color.BLACK);
		buttonBack.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
		buttonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				viewList.setVisibility(View.VISIBLE);
				buttonBack.setVisibility(View.GONE);
				viewContentContainer.setVisibility(View.GONE);
			}
		});
		viewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showLicense((ProductLicense) parent.getAdapter().getItem((Integer) view.getTag()));
			}
		});
		viewList.setAdapter(new BaseAdapter() {

			@Override
			public int getCount() {
				return list.length;
			}

			@Override
			public ProductLicense getItem(int position) {
				return list[position];
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.view_product_licenses_list_item, parent, false);
				}
				convertView.setTag(position);
				((TextView) convertView).setText(getItem(position).getFullTitle());
				return convertView;
			}
		});
		setPreloaderVisible(true);
	}

	public ProductLicensesViewer setProvider(@NonNull ProductLicenseProvider provider) {
		this.provider = provider;
		showList();
		return this;
	}

	private void showList() {
		setPreloaderVisible(true);
		Observable.create(new Observable.OnSubscribe<ProductLicense[]>() {
			@Override
			public void call(Subscriber<? super ProductLicense[]> subscriber) {
				subscriber.onNext(provider.getList());
				subscriber.onCompleted();
			}
		}).delay(500, TimeUnit.MILLISECONDS)
			.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
			.subscribe(
				new Action1<ProductLicense[]>() {
					@Override
					public void call(final ProductLicense[] productLicenses) {
						setPreloaderVisible(false);
						viewList.setVisibility(View.VISIBLE);
						list = productLicenses;
						((BaseAdapter) viewList.getAdapter()).notifyDataSetChanged();
					}
				}
			);
	}

	private void showLicense(final ProductLicense license) {
		setPreloaderVisible(true);
		Observable.create(new Observable.OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				subscriber.onNext(provider.getContent(license));
				subscriber.onCompleted();
			}
		}).delay(1, TimeUnit.SECONDS).subscribeOn(Schedulers.computation())
			.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
			@Override
			public void call(String content) {
				setPreloaderVisible(false);
				viewContentContainer.setVisibility(View.VISIBLE);
				buttonBack.setVisibility(View.VISIBLE);
				viewContent.setText(content);
			}
		});
	}

	private void setPreloaderVisible(boolean visible) {
		if (visible) {
			viewList.setVisibility(View.GONE);
			viewContentContainer.setVisibility(View.GONE);
			buttonBack.setVisibility(View.GONE);
		}
		viewPreloader.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

}
