package com.esminis.server.library.model;

public class ProductLicense {

	public final String product;
	public final String title;
	public final String name;

	public ProductLicense(String product, String title, String name) {
		this.product = product;
		this.title = title;
		this.name = name;
	}

	public String getFullTitle() {
		return product.equals(title) ? product : product + " - " + title;
	}

}
