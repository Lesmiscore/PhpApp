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
package com.esminis.server.library.dialog.about;

import android.content.Context;
import android.support.annotation.StringRes;

import com.esminis.server.library.widget.ProductLicensesViewer;

public interface AboutView {

	void setContentAbout(Context context, @StringRes int content, @StringRes int parameter);

	void setContentManual(Context context, @StringRes int content, @StringRes int parameter);

	void setLicensesProvider(ProductLicensesViewer.ProductLicenseProvider provider);

	void setupOnShow();

	void setupOnCreate();

}
