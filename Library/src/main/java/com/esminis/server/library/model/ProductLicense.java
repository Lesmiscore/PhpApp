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
