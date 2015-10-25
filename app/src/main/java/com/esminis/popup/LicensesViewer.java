package com.esminis.popup;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.esminis.model.ProductLicense;
import com.esminis.model.manager.ProductLicenseManager;
import com.esminis.server.php.Application;
import com.esminis.server.php.R;

import javax.inject.Inject;

import rx.functions.Action1;

public class LicensesViewer extends FrameLayout {

	private final ListView list;
	private final TextView viewer;
	private final View viewerContainer;
	private final Button buttonBack;
	private final View preloader;

	@Inject
	protected ProductLicenseManager productLicenseManager;

	public LicensesViewer(Context context) {
		super(context);
		((Application)context.getApplicationContext()).getObjectGraph().inject(this);
		View.inflate(context, R.layout.view_licenses_viewer, this);
		list = (ListView)findViewById(R.id.list_licenses);
		viewer = (TextView)findViewById(R.id.content_license);
		viewerContainer = findViewById(R.id.content_license_container);
		buttonBack = (Button)findViewById(R.id.button_back);
		preloader = findViewById(R.id.preloader_container);
		viewer.setMovementMethod(new ScrollingMovementMethod());
		DrawerArrowDrawable drawable = new DrawerArrowDrawable(getContext());
		drawable.setProgress(1);
		drawable.setColor(Color.BLACK);
		buttonBack.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
		buttonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.setVisibility(View.VISIBLE);
				buttonBack.setVisibility(View.GONE);
				viewerContainer.setVisibility(View.GONE);
			}
		});
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showLicense((ProductLicense) parent.getAdapter().getItem((Integer) view.getTag()));
			}
		});
		setPreloaderVisible(true);
		productLicenseManager.getLicenses().subscribe(new Action1<ProductLicense[]>() {
			@Override
			public void call(ProductLicense[] productLicenses) {
				setPreloaderVisible(false);
				list.setVisibility(View.VISIBLE);
				setLicenses(productLicenses);
			}
		});
	}

	private void setLicenses(final ProductLicense[] list) {
		this.list.setAdapter(new BaseAdapter() {
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
						.inflate(R.layout.view_licenses_list_item, parent, false);
				}
				convertView.setTag(position);
				((TextView)convertView).setText(getItem(position).getFullTitle());
				return convertView;
			}
		});
	}

	private void setPreloaderVisible(boolean visible) {
		if (visible) {
			list.setVisibility(View.GONE);
			viewerContainer.setVisibility(View.GONE);
			buttonBack.setVisibility(View.GONE);
		}
		preloader.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void showLicense(ProductLicense license) {
		setPreloaderVisible(true);
		productLicenseManager.getProductLicenseContent(license).subscribe(new Action1<String>() {
			@Override
			public void call(String content) {
				setPreloaderVisible(false);
				viewerContainer.setVisibility(View.VISIBLE);
				buttonBack.setVisibility(View.VISIBLE);
				viewer.setText(content);
			}
		});
	}

}
